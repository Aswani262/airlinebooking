package com.example.airlinebooking.domain;

/**
 * Immutable passenger snapshot to keep booking requests stable and auditable.
 */
public record Passenger(String id, String fullName, String email) {
}
