package com.example.airlinebooking.domain;

/**
 * Tracks payment transaction lifecycle to coordinate seat state transitions.
 */
public enum PaymentStatus {
    PENDING,
    SUCCEEDED,
    FAILED,
    EXPIRED
}
