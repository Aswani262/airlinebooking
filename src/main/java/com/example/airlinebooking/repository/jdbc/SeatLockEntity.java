package com.example.airlinebooking.repository.jdbc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Stores seat lock headers to coordinate temporary holds across multiple seats.
 */
@Data
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

}
