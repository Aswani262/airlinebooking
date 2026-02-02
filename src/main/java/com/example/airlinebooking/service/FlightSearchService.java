package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.Flight;

import java.util.List;

/**
 * Provides flight search/query capabilities separated from booking writes to keep read flows simple.
 */
public interface FlightSearchService {
    List<Flight> search(String origin, String destination);

    Flight getById(String flightId);
}
