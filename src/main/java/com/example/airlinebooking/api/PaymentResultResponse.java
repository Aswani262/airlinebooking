package com.example.airlinebooking.api;

/**
 * Returns the outcome of a payment event callback.
 */
public record PaymentResultResponse(
        String transactionId,
        String bookingId,
        String status
) {
}
