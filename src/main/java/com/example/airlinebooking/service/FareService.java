package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.FareClass;
/**
 * Centralizes fare rules and baggage policy lookups so API responses stay consistent.
 */
public interface FareService {
    String fareRules(FareClass fareClass);

    String baggagePolicy(FareClass fareClass);
}
