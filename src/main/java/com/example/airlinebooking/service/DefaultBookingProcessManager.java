package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.Booking;
import com.example.airlinebooking.domain.BookingStatus;
import com.example.airlinebooking.domain.Passenger;
import com.example.airlinebooking.domain.PaymentStatus;
import com.example.airlinebooking.domain.PaymentTransaction;
import com.example.airlinebooking.domain.SeatLock;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.integration.AirlineItService;
import com.example.airlinebooking.integration.PaymentService;
import com.example.airlinebooking.repository.BookingRepository;
import com.example.airlinebooking.repository.FlightRepository;
import com.example.airlinebooking.repository.PaymentTransactionRepository;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Default booking process manager that coordinates seat locks with payment transactions.
 */
@Service
public class DefaultBookingProcessManager implements BookingProcessManager {
    private final SeatLockService seatLockService;
    private final PaymentService paymentService;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final SeatJdbcRepository seatJdbcRepository;
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final AirlineItService airlineItService;

    public DefaultBookingProcessManager(SeatLockService seatLockService, PaymentService paymentService,
                                        PaymentTransactionRepository paymentTransactionRepository, SeatJdbcRepository seatJdbcRepository,
                                        BookingRepository bookingRepository, FlightRepository flightRepository,
                                        AirlineItService airlineItService) {
        this.seatLockService = seatLockService;
        this.paymentService = paymentService;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.seatJdbcRepository = seatJdbcRepository;
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
        this.airlineItService = airlineItService;
    }

    @Override
    @Transactional
    public PaymentTransaction startBooking(String flightId, Passenger passenger, List<String> seatIds, int amountCents) {
        flightRepository.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        SeatLock lock = seatLockService.lockSeats(flightId, seatIds);
        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(bookingId, flightId, passenger, seatIds, BookingStatus.PENDING_PAYMENT, Instant.now());
        bookingRepository.save(booking);
        PaymentTransaction transaction = paymentService.createTransaction(bookingId, flightId, seatIds, amountCents, "Initial charge");
        seatJdbcRepository.updateStatusIfCurrent(flightId, seatIds, SeatStatus.LOCKED, SeatStatus.PAYMENT_PENDING);
        seatLockService.finalizeLock(lock);
        return transaction;
    }

    @Override
    @Transactional
    public Booking handlePaymentSuccess(String transactionId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found"));
        if (transaction.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not pending");
        }
        int updated = seatJdbcRepository.updateStatusIfCurrent(transaction.getFlightId(), transaction.getSeatIds(),
                SeatStatus.PAYMENT_PENDING, SeatStatus.BOOKED);
        if (updated != transaction.getSeatIds().size()) {
            throw new IllegalStateException("Seat state mismatch while confirming payment");
        }
        Booking booking = bookingRepository.findById(transaction.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
        transaction.setStatus(PaymentStatus.SUCCEEDED);
        paymentTransactionRepository.save(transaction);
        airlineItService.issueTicket(booking);
        return booking;
    }

    @Override
    @Transactional
    public void handlePaymentFailure(String transactionId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found"));
        seatJdbcRepository.updateStatusIfCurrent(transaction.getFlightId(), transaction.getSeatIds(),
                SeatStatus.PAYMENT_PENDING, SeatStatus.AVAILABLE);
        bookingRepository.findById(transaction.getBookingId()).ifPresent(booking -> {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        });
        transaction.setStatus(PaymentStatus.FAILED);
        paymentTransactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public int releaseExpiredPayments() {
        List<PaymentTransaction> expired = paymentTransactionRepository.findExpiredPending(Instant.now());
        for (PaymentTransaction transaction : expired) {
            seatJdbcRepository.updateStatusIfCurrent(transaction.getFlightId(), transaction.getSeatIds(),
                    SeatStatus.PAYMENT_PENDING, SeatStatus.AVAILABLE);
            bookingRepository.findById(transaction.getBookingId()).ifPresent(booking -> {
                booking.setStatus(BookingStatus.CANCELLED);
                bookingRepository.save(booking);
            });
            transaction.setStatus(PaymentStatus.EXPIRED);
            paymentTransactionRepository.save(transaction);
        }
        return expired.size();
    }
}
