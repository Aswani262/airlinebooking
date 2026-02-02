//package com.example.airlinebooking.service;
//
//import com.example.airlinebooking.domain.Aircraft;
//import com.example.airlinebooking.domain.Flight;
//import com.example.airlinebooking.repository.FlightRepository;
//import org.junit.jupiter.api.Test;
//
//import java.time.LocalDateTime;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class FlightSearchServiceTest {
//    @Test
//    void findsFlightsByRoute() {
//        FlightRepository flightRepository = mock(FlightRepository.class);
//        Flight flight = new Flight("FL-100", "XY100", "JFK", "SFO", LocalDateTime.now(),
//                new Aircraft("AC-1", "A320", Collections.emptyList()));
//        when(flightRepository.findByRoute("JFK", "SFO")).thenReturn(List.of(flight));
//        FlightSearchService service = new DefaultFlightSearchService(flightRepository);
//
//        var flights = service.search("JFK", "SFO");
//
//        assertThat(flights).hasSize(1);
//        assertThat(flights.get(0).getFlightNumber()).isEqualTo("XY100");
//    }
//
//    @Test
//    void getsFlightById() {
//        FlightRepository flightRepository = mock(FlightRepository.class);
//        Flight flight = new Flight("FL-200", "XY200", "SFO", "SEA", LocalDateTime.now(),
//                new Aircraft("AC-2", "B737", Collections.emptyList()));
//        when(flightRepository.findById("FL-200")).thenReturn(Optional.of(flight));
//        FlightSearchService service = new DefaultFlightSearchService(flightRepository);
//
//        var flight = service.getById("FL-200");
//
//        assertThat(flight.getOrigin()).isEqualTo("SFO");
//    }
//}
