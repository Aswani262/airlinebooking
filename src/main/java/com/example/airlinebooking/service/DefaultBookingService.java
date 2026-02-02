package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.Booking;
import com.example.airlinebooking.domain.BookingStatus;
import com.example.airlinebooking.domain.Passenger;
import com.example.airlinebooking.domain.SeatLock;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.repository.BookingRepository;
import com.example.airlinebooking.repository.FlightRepository;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Default booking service implementation using repository interfaces.
 */
@Service
public class DefaultBookingService implements BookingService {
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final SeatLockService seatLockService;
    private final SeatJdbcRepository seatJdbcRepository;

    public DefaultBookingService(BookingRepository bookingRepository, FlightRepository flightRepository, SeatLockService seatLockService,
                                 SeatJdbcRepository seatJdbcRepository) {
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
        this.seatLockService = seatLockService;
        this.seatJdbcRepository = seatJdbcRepository;
    }

    @Override
    @Transactional
    public Booking book(String flightId, Passenger passenger, List<String> seatIds) {
        SeatLock lock = seatLockService.lockSeats(flightId, seatIds);
        flightRepository.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        int updated = seatJdbcRepository.updateStatusIfCurrent(flightId, seatIds, SeatStatus.LOCKED, SeatStatus.BOOKED);
        if (updated != seatIds.size()) {
            throw new IllegalStateException("Seat lock expired before booking confirmation");
        }
        Booking booking = new Booking(UUID.randomUUID().toString(), flightId, passenger, seatIds, BookingStatus.CONFIRMED, Instant.now());
        bookingRepository.save(booking);
        seatLockService.finalizeLock(lock);
        return booking;
    }

    @Override
    @Transactional
    public Booking cancel(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return booking;
        }
        flightRepository.findById(booking.getFlightId())
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        seatJdbcRepository.updateStatus(booking.getFlightId(), booking.getSeatIds(), SeatStatus.AVAILABLE);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        return booking;
    }
}
