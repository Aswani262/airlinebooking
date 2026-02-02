package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.Flight;
import com.example.airlinebooking.repository.FlightRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default flight search implementation backed by the flight repository interface.
 */
@Service
public class DefaultFlightSearchService implements FlightSearchService {
    private final FlightRepository flightRepository;

    public DefaultFlightSearchService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Override
    public List<Flight> search(String origin, String destination) {
        return flightRepository.findByRoute(origin, destination);
    }

    @Override
    public Flight getById(String flightId) {
        return flightRepository.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
    }
}
