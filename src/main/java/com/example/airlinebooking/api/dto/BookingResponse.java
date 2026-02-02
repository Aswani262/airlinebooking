package com.example.airlinebooking.api.dto;

import java.time.Instant;
import java.util.List;

/**
 * Response snapshot for booking confirmations to keep API output stable.
 */
public record BookingResponse(
        String bookingId,
        String flightId,
        String passengerName,
        String status,
        List<String> seatIds,
        Instant createdAt
) {
}
