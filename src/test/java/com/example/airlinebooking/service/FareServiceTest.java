//package com.example.airlinebooking.service;
//
//import com.example.airlinebooking.domain.FareClass;
//import org.junit.jupiter.api.Test;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//class FareServiceTest {
//    @Test
//    void returnsFareRulesAndBaggagePolicy() {
//        FareService fareService = new DefaultFareService();
//
//        String rules = fareService.fareRules(FareClass.BUSINESS);
//        String baggage = fareService.baggagePolicy(FareClass.BUSINESS);
//
//        assertThat(rules).contains("Changes free");
//        assertThat(baggage).contains("2 cabin bags");
//    }
//}
