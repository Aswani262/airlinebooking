package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.Booking;
import com.example.airlinebooking.domain.BookingStatus;
import com.example.airlinebooking.domain.SeatLock;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.integration.AirlineItService;
import com.example.airlinebooking.integration.PaymentService;
import com.example.airlinebooking.repository.BookingRepository;
import com.example.airlinebooking.repository.FlightRepository;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Default booking service implementation using repository interfaces.
 */
@Service
public class DefaultBookingService implements BookingService {
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final SeatLockService seatLockService;
    private final SeatJdbcRepository seatJdbcRepository;
    private final PaymentService paymentService;
    private final AirlineItService airlineItService;

    public DefaultBookingService(BookingRepository bookingRepository, FlightRepository flightRepository, SeatLockService seatLockService,
                                 SeatJdbcRepository seatJdbcRepository, PaymentService paymentService, AirlineItService airlineItService) {
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
        this.seatLockService = seatLockService;
        this.seatJdbcRepository = seatJdbcRepository;
        this.paymentService = paymentService;
        this.airlineItService = airlineItService;
    }

    @Override
    @Transactional
    public Booking cancel(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.EXPIRED || booking.getStatus() == BookingStatus.FAILED) {
            return booking;
        }
        flightRepository.findById(booking.getFlightId())
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        seatJdbcRepository.updateStatus(booking.getFlightId(), booking.getSeatIds(), SeatStatus.AVAILABLE);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.update(booking, Collections.emptyMap());
        seatLockService.releaseLock(
                booking.getFlightId())  ;
        paymentService.initateRefund(booking.getId(), booking.getAmount(), "Customer cancellation");
        airlineItService.notifyCancellation(booking);
        return booking;
    }

//    @Override
//    @Transactional
//    public Booking reschedule(String bookingId, String newFlightId, List<String> newSeatIds) {
//        Booking existingBooking = bookingRepository.findById(bookingId)
//                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
//        if (existingBooking.getStatus() != BookingStatus.CONFIRMED) {
//            throw new IllegalStateException("Only confirmed bookings can be rescheduled");
//        }
//        flightRepository.findById(existingBooking.getFlightId())
//                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
//        seatJdbcRepository.updateStatus(existingBooking.getFlightId(), existingBooking.getSeatIds(), SeatStatus.AVAILABLE);
//        existingBooking.setStatus(BookingStatus.RESCHEDULED);
//        bookingRepository.save(existingBooking,Collections.emptyMap());
//
//        paymentService.collectChangeFee(existingBooking.getId(), existingBooking.getFlightId(), 0, "Schedule change");
//       // Booking newBooking = book(newFlightId, newSeatIds);
//        airlineItService.notifyReschedule(existingBooking, newBooking);
//        return newBooking;
//    }
}