package com.example.airlinebooking.domain;

/**
 * Tracks seat inventory states so availability can be derived consistently across locks and bookings.
 */
public enum SeatStatus {
    AVAILABLE,
    LOCKED,
    BOOKED
}
