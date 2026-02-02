package com.example.airlinebooking.api;

import java.util.List;

/**
 * Returns available seat IDs for a given fare class to support seat selection flows.
 */
public record SeatAvailabilityResponse(
        String flightId,
        String fareClass,
        List<String> availableSeats
) {
}
