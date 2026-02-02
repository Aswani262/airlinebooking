package com.example.airlinebooking.repository.jdbc;

import com.example.airlinebooking.domain.BookingStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Stores booking facts in a flat structure so seat assignments can be stored separately.
 */
@Table("bookings")
@Data
public class BookingEntity {
    @Id
    private String id;
    private String flightId;
    private BookingStatus status;
    private double amount;
    private Instant createdAt;

    public BookingEntity() {
    }

    public BookingEntity(String id, String flightId, BookingStatus status,double amount,
                         Instant createdAt) {
        this.id = id;
        this.flightId = flightId;
        this.status = status;
        this.createdAt = createdAt;
        this.amount = amount;
    }

}
