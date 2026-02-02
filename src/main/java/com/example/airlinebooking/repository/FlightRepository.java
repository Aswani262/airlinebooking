package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.Flight;

import java.util.List;
import java.util.Optional;

/**
 * Flight repository interface to decouple services from JDBC persistence details.
 */
public interface FlightRepository {
    List<Flight> findByRoute(String origin, String destination);

    Optional<Flight> findById(String id);
}
