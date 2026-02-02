package com.example.airlinebooking.repository.jdbc;

import com.example.airlinebooking.domain.PaymentStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Stores payment transactions for booking flow coordination.
 */
@Table("payment_transactions")
public class PaymentTransactionEntity {
    @Id
    private String id;
    private String bookingId;
    private String flightId;
    private int amountCents;
    private PaymentStatus status;
    private Instant createdAt;
    private Instant expiresAt;

    public PaymentTransactionEntity() {
    }

    public PaymentTransactionEntity(String id, String bookingId, String flightId, int amountCents, PaymentStatus status,
                                    Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.flightId = flightId;
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

    public int getAmountCents() {
        return amountCents;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
