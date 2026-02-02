package com.example.airlinebooking.api.dto;

/**
 * Exposes fare rules and baggage policies together to reduce API round-trips.
 */
public record FareRulesResponse(
        String flightId,
        String fareClass,
        String fareRules,
        String baggagePolicy
) {
}
