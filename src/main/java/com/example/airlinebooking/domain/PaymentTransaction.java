package com.example.airlinebooking.domain;

import java.time.Instant;
import java.util.List;

/**
 * Payment transaction aggregate used by the booking process manager to track payment progress.
 */
public class PaymentTransaction {
    private final String id;
    private final String bookingId;
    private final String flightId;
    private final List<String> seatIds;
    private final int amountCents;
    private PaymentStatus status;
    private final Instant createdAt;
    private final Instant expiresAt;

    public PaymentTransaction(String id, String bookingId, String flightId, List<String> seatIds, int amountCents, PaymentStatus status,
                              Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.flightId = flightId;
        this.seatIds = seatIds;
        this.amountCents = amountCents;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getId() {
        return id;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getFlightId() {
        return flightId;
    }

    public List<String> getSeatIds() {
        return seatIds;
    }

    public int getAmountCents() {
        return amountCents;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
