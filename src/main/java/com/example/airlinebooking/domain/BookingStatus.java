package com.example.airlinebooking.domain;

/**
 * Captures booking lifecycle states to keep cancellation and ticketing logic explicit.
 */
public enum BookingStatus {
    CONFIRMED,
    CANCELLED,
    FAILED,
    PENDING,
    EXPIRED
}
