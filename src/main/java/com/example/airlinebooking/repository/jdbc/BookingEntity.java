package com.example.airlinebooking.repository.jdbc;

import com.example.airlinebooking.domain.BookingStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Stores booking facts in a flat structure so seat assignments can be stored separately.
 */
@Table("bookings")
public class BookingEntity {
    @Id
    private String id;
    private String flightId;
    private String passengerId;
    private String passengerName;
    private String passengerEmail;
    private BookingStatus status;
    private Instant createdAt;

    public BookingEntity() {
    }

    public BookingEntity(String id, String flightId, String passengerId, String passengerName, String passengerEmail, BookingStatus status,
                         Instant createdAt) {
        this.id = id;
        this.flightId = flightId;
        this.passengerId = passengerId;
        this.passengerName = passengerName;
        this.passengerEmail = passengerEmail;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getFlightId() {
        return flightId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
