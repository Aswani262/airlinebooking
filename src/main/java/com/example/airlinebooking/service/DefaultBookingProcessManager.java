package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.Booking;
import com.example.airlinebooking.domain.BookingStatus;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    public PaymentTransaction startBooking(String flightId, double amount ,Map<String,String> passagnerSeatMap) {
        flightRepository.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));


        List<String> seatIds = passagnerSeatMap.keySet().stream().toList();
        // Check seat availability and lock seats
        SeatLock lock = seatLockService.lockSeats(flightId, seatIds);

        String bookingId = UUID.randomUUID().toString();

        // Create booking record with PENDING_PAYMENT status
        Booking booking = new Booking(bookingId, flightId, seatIds, BookingStatus.PENDING, amount,Instant.now());

        bookingRepository.insert(booking,passagnerSeatMap);

        // Create payment transaction with PENDING status which hold information about booking and locked seats with
        // 3rd party payment gateway
        return paymentService.createTransaction(bookingId, amount, "Seat looked");
    }

    @Override
    @Transactional
    public Booking handlePaymentSuccess(String transactionId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found"));

        if (transaction.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not pending");
        }

        Booking booking = bookingRepository.findById(transaction.getBookingId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));


        if((booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.FAILED) && transaction.getStatus() == PaymentStatus.REFUND_IN_PROGRESS){

            transaction.setStatus(PaymentStatus.REFUNDED);

        } else {

            transaction.setStatus(PaymentStatus.SUCCEEDED);
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.update(booking, Collections.emptyMap());

            seatJdbcRepository.updateStatusIfCurrent(booking.getFlightId(), booking.getSeatIds(),
                    SeatStatus.LOCKED, SeatStatus.BOOKED);
        }

        paymentTransactionRepository.update(transaction);

        seatLockService.releaseLock(booking.getFlightId());

        airlineItService.issueTicket(booking);

        return booking;
    }

    @Override
    @Transactional
    public void handlePaymentFailure(String transactionId) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Payment transaction not found"));

        Booking booking = bookingRepository.findById(transaction.getBookingId()).get();

        seatJdbcRepository.updateStatusIfCurrent(booking.getFlightId(), booking.getSeatIds(),
                SeatStatus.LOCKED, SeatStatus.AVAILABLE);

        seatLockService.releaseLock(booking.getFlightId());

        if(booking.getStatus() == BookingStatus.CANCELLED && transaction.getStatus() == PaymentStatus.REFUND_IN_PROGRESS){

            transaction.setStatus(PaymentStatus.REFUND_FAILED);

        } else {

            transaction.setStatus(PaymentStatus.FAILED);
            booking.setStatus(BookingStatus.FAILED);
            bookingRepository.update(booking,Collections.emptyMap());
        }
        paymentTransactionRepository.update(transaction);
    }


    @Override
    public void handlePayment(String bookingId, String paymentGatewayTransactionId, String transactionId, String rawPayload, String status) {
        if(status.equals("SUCCESS")){
            handlePaymentSuccess(transactionId);
        } else if(status.equals("FAILED")){
            handlePaymentFailure(transactionId);
        }
    }
}
