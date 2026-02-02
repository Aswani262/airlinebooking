package com.example.airlinebooking.api;

import java.time.LocalDateTime;

/**
 * Lightweight view of a flight returned by search to avoid leaking full inventory details.
 */
public record FlightSummary(
        String id,
        String flightNumber,
        String origin,
        String destination,
        LocalDateTime departureTime
) {
}
