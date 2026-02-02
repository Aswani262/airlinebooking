package com.example.airlinebooking.repository.jdbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Stores seat lock headers to coordinate temporary holds across multiple seats.
 */
@Table("seat_locks")
public class SeatLockEntity {
    @Id
    private String id;
    private String flightId;
    private Instant expiresAt;

    public SeatLockEntity() {
    }

    public SeatLockEntity(String id, String flightId, Instant expiresAt) {
        this.id = id;
        this.flightId = flightId;
        this.expiresAt = expiresAt;
    }

    public String getId() {
        return id;
    }

    public String getFlightId() {
        return flightId;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
