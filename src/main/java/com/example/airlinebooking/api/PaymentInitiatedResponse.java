package com.example.airlinebooking.api;

import java.time.Instant;

/**
 * Response returned when a payment transaction is initiated for a booking.
 */
public record PaymentInitiatedResponse(
        String bookingId,
        String transactionId,
        String status,
        Instant expiresAt
) {
}
