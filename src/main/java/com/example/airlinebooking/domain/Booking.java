package com.example.airlinebooking.domain;

import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Represents a booking record with mutable status to support cancellations and refund workflows.
 */
@Data
public class Booking {
    private final String id;
    private final String flightId;
    private final List<String> seatIds;
    private BookingStatus status;
    private final double amount;
    private final Instant createdAt;

    public Booking(String id, String flightId, List<String> seatIds, BookingStatus status, double amount, Instant createdAt) {
        this.id = id;
        this.flightId = flightId;
        this.seatIds = seatIds;
        this.status = status;
        this.amount = amount;
        this.createdAt = createdAt;
    }
}
