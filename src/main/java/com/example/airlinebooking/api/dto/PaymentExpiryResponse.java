package com.example.airlinebooking.api.dto;

/**
 * Reports how many pending payments were expired and released.
 */
public record PaymentExpiryResponse(
        int releasedCount
) {
}
