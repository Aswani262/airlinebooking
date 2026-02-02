package com.example.airlinebooking.domain;

/**
 * Captures booking lifecycle states to keep cancellation and ticketing logic explicit.
 */
public enum BookingStatus {
    PENDING_PAYMENT,
    CONFIRMED,
    CANCELLED,
    REFUNDED,
    RESCHEDULED
}
