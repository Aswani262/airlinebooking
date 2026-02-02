package com.example.airlinebooking.domain;

import java.util.List;

/**
 * Represents a physical aircraft as an immutable record so seat layouts stay consistent for a flight snapshot.
 * This keeps seat composition centralized and avoids partial updates during booking flows.
 */
public record Aircraft(String id, String model, List<Seat> seats) {
}
