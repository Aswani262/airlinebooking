package com.example.airlinebooking.api;

/**
 * Reports how many pending payments were expired and released.
 */
public record PaymentExpiryResponse(
        int releasedCount
) {
}
