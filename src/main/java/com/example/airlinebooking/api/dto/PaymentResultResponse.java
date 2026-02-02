package com.example.airlinebooking.api.dto;

/**
 * Returns the outcome of a payment event callback.
 */
public record PaymentResultResponse(
        String transactionId,
        String bookingId,
        String status
) {
}
