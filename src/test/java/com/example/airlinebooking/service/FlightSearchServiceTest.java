package com.example.airlinebooking.service;

import com.example.airlinebooking.repository.FlightRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FlightSearchServiceTest {
    @Test
    void findsFlightsByRoute() {
        FlightRepository flightRepository = new FlightRepository();
        FlightSearchService service = new FlightSearchService(flightRepository);

        var flights = service.search("JFK", "SFO");

        assertThat(flights).hasSize(1);
        assertThat(flights.get(0).getFlightNumber()).isEqualTo("XY100");
    }

    @Test
    void getsFlightById() {
        FlightRepository flightRepository = new FlightRepository();
        FlightSearchService service = new FlightSearchService(flightRepository);

        var flight = service.getById("FL-200");

        assertThat(flight.getOrigin()).isEqualTo("SFO");
    }
}
