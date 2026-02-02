package com.example.airlinebooking.domain;

import java.time.Instant;
import java.util.List;

/**
 * Represents a booking record with mutable status to support cancellations and refund workflows.
 */
public class Booking {
    private final String id;
    private final String flightId;
    private final Passenger passenger;
    private final List<String> seatIds;
    private BookingStatus status;
    private final Instant createdAt;

    public Booking(String id, String flightId, Passenger passenger, List<String> seatIds, BookingStatus status, Instant createdAt) {
        this.id = id;
        this.flightId = flightId;
        this.passenger = passenger;
        this.seatIds = seatIds;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getFlightId() {
        return flightId;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public List<String> getSeatIds() {
        return seatIds;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
