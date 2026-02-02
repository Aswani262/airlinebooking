package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.Booking;
import com.example.airlinebooking.domain.Passenger;

import java.util.List;

/**
 * Handles booking writes to keep seat mutations centralized and transactional per flight.
 */
public interface BookingService {
    Booking book(String flightId, Passenger passenger, List<String> seatIds);

    Booking cancel(String bookingId);
}
