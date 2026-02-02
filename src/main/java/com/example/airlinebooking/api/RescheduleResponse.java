package com.example.airlinebooking.api;

import java.util.List;

/**
 * Returns booking identifiers after a reschedule is completed.
 */
public record RescheduleResponse(
        String originalBookingId,
        String newBookingId,
        String status,
        String newFlightId,
        List<String> seatIds
) {
}
