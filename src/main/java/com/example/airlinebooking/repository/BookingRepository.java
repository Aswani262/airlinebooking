package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.Booking;

import java.util.Optional;

/**
 * Booking repository interface to keep services persistence-agnostic.
 */
public interface BookingRepository {
    Booking save(Booking booking);

    Optional<Booking> findById(String id);
}
