package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.Booking;

import java.util.Map;
import java.util.Optional;

/**
 * Booking repository interface to keep services persistence-agnostic.
 */
public interface BookingRepository {
    Booking update(Booking booking, Map<String,String> passengerSeatMap);

    Optional<Booking> findById(String id);

    void insert(Booking booking, Map<String, String> passagnerSeatMap);
}
