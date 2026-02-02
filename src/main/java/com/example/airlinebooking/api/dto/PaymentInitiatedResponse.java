package com.example.airlinebooking.api.dto;

import java.time.Instant;

/**
 * Response returned when a payment transaction is initiated for a booking.
 */
public record PaymentInitiatedResponse(
        String bookingId,
        //This is going to send as metadata in payment gateway
        String transactionId,
        String status
) {
}
