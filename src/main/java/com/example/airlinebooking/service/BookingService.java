package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.Booking;
import com.example.airlinebooking.domain.BookingStatus;
import com.example.airlinebooking.domain.Flight;
import com.example.airlinebooking.domain.Passenger;
import com.example.airlinebooking.domain.Seat;
import com.example.airlinebooking.domain.SeatLock;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.repository.BookingRepository;
import com.example.airlinebooking.repository.FlightRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Handles booking writes to keep seat mutations centralized and transactional per flight.
 */
@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final SeatLockService seatLockService;

    public BookingService(BookingRepository bookingRepository, FlightRepository flightRepository, SeatLockService seatLockService) {
        this.bookingRepository = bookingRepository;
        this.flightRepository = flightRepository;
        this.seatLockService = seatLockService;
    }

    public Booking book(String flightId, Passenger passenger, List<String> seatIds) {
        SeatLock lock = seatLockService.lockSeats(flightId, seatIds);
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        synchronized (flight) {
            List<Seat> seatsToBook = flight.getAircraft().seats().stream()
                    .filter(seat -> seatIds.contains(seat.getId()))
                    .toList();
            seatsToBook.forEach(seat -> seat.setStatus(SeatStatus.BOOKED));
            Booking booking = new Booking(UUID.randomUUID().toString(), flightId, passenger, seatIds, BookingStatus.CONFIRMED, Instant.now());
            bookingRepository.save(booking);
            seatLockService.finalizeLock(lock);
            return booking;
        }
    }

    public Booking cancel(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return booking;
        }
        Flight flight = flightRepository.findById(booking.getFlightId())
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        synchronized (flight) {
            flight.getAircraft().seats().stream()
                    .filter(seat -> booking.getSeatIds().contains(seat.getId()))
                    .forEach(seat -> seat.setStatus(SeatStatus.AVAILABLE));
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            return booking;
        }
    }
}
