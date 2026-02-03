package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.FareClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultFareServiceTest {

    private DefaultFareService fareService;

    @BeforeEach
    void setUp() {
        fareService = new DefaultFareService();
    }

    // -------------------------
    // fareRules()
    // -------------------------

    @Test
    void fareRules_shouldReturnEconomyRules() {
        String rules = fareService.fareRules(FareClass.ECONOMY);

        assertEquals(
                "Changes allowed with fee. Refundable with 24h notice.",
                rules
        );
    }

    @Test
    void fareRules_shouldReturnPremiumEconomyRules() {
        String rules = fareService.fareRules(FareClass.PREMIUM_ECONOMY);

        assertEquals(
                "Changes allowed with reduced fee. Refundable with 12h notice.",
                rules
        );
    }

    @Test
    void fareRules_shouldReturnBusinessRules() {
        String rules = fareService.fareRules(FareClass.BUSINESS);

        assertEquals(
                "Changes free. Refundable until 2h before departure.",
                rules
        );
    }

    @Test
    void fareRules_shouldReturnFirstClassRules() {
        String rules = fareService.fareRules(FareClass.FIRST);

        assertEquals(
                "Changes free. Fully refundable.",
                rules
        );
    }



    // -------------------------
    // baggagePolicy()
    // -------------------------

    @Test
    void baggagePolicy_shouldReturnEconomyPolicy() {
        String policy = fareService.baggagePolicy(FareClass.ECONOMY);

        assertEquals(
                "1 cabin bag, 1 checked bag.",
                policy
        );
    }

    @Test
    void baggagePolicy_shouldReturnPremiumEconomyPolicy() {
        String policy = fareService.baggagePolicy(FareClass.PREMIUM_ECONOMY);

        assertEquals(
                "1 cabin bag, 2 checked bags.",
                policy
        );
    }

    @Test
    void baggagePolicy_shouldReturnBusinessPolicy() {
        String policy = fareService.baggagePolicy(FareClass.BUSINESS);

        assertEquals(
                "2 cabin bags, 2 checked bags.",
                policy
        );
    }

    @Test
    void baggagePolicy_shouldReturnFirstClassPolicy() {
        String policy = fareService.baggagePolicy(FareClass.FIRST);

        assertEquals(
                "2 cabin bags, 3 checked bags.",
                policy
        );
    }


}
