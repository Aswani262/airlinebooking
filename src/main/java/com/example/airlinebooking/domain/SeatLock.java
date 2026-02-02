package com.example.airlinebooking.domain;

import java.time.Instant;
import java.util.List;

/**
 * Immutable seat lock record so lock expiry and ownership are consistent across the booking flow.
 */
public record SeatLock(String id, String flightId, List<String> seatIds, Instant expiresAt) {
}
