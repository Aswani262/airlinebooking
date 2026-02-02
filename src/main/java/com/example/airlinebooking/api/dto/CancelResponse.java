package com.example.airlinebooking.api.dto;

/**
 * Simple cancellation acknowledgment to keep cancel flow lightweight.
 */
public record CancelResponse(
        String bookingId,
        String status,
        String refundStatus
) {
}
