package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.Flight;
import com.example.airlinebooking.repository.FlightRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Provides flight search/query capabilities separated from booking writes to keep read flows simple.
 */
@Service
public class FlightSearchService {
    private final FlightRepository flightRepository;

    public FlightSearchService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    public List<Flight> search(String origin, String destination) {
        return flightRepository.findByRoute(origin, destination);
    }

    public Flight getById(String flightId) {
        return flightRepository.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
    }
}
