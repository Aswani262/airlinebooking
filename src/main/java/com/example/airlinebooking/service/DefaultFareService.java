package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.FareClass;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Default fare policy provider so fare logic stays centralized and consistent.
 */
@Service
public class DefaultFareService implements FareService {
    private static final Map<FareClass, String> FARE_RULES = Map.of(
            FareClass.ECONOMY, "Changes allowed with fee. Refundable with 24h notice.",
            FareClass.PREMIUM_ECONOMY, "Changes allowed with reduced fee. Refundable with 12h notice.",
            FareClass.BUSINESS, "Changes free. Refundable until 2h before departure.",
            FareClass.FIRST, "Changes free. Fully refundable."
    );

    private static final Map<FareClass, String> BAGGAGE_POLICY = Map.of(
            FareClass.ECONOMY, "1 cabin bag, 1 checked bag.",
            FareClass.PREMIUM_ECONOMY, "1 cabin bag, 2 checked bags.",
            FareClass.BUSINESS, "2 cabin bags, 2 checked bags.",
            FareClass.FIRST, "2 cabin bags, 3 checked bags."
    );

    @Override
    public String fareRules(FareClass fareClass) {
        return FARE_RULES.getOrDefault(fareClass, "Standard fare rules apply.");
    }

    @Override
    public String baggagePolicy(FareClass fareClass) {
        return BAGGAGE_POLICY.getOrDefault(fareClass, "Standard baggage policy applies.");
    }
}
