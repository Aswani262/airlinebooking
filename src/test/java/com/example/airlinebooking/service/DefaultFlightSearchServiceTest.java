package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.Flight;
import com.example.airlinebooking.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultFlightSearchServiceTest {

    @Mock
    private FlightRepository flightRepository;

    @InjectMocks
    private DefaultFlightSearchService flightSearchService;

    // -------------------------
    // search()
    // -------------------------

    @Test
    void search_shouldReturnFlightsForRoute() {
        String origin = "DEL";
        String destination = "BLR";

        Flight flight1 = mock(Flight.class);
        Flight flight2 = mock(Flight.class);

        when(flightRepository.findByRoute(origin, destination))
                .thenReturn(List.of(flight1, flight2));

        List<Flight> result = flightSearchService.search(origin, destination);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertSame(flight1, result.get(0));
        assertSame(flight2, result.get(1));

        verify(flightRepository).findByRoute(origin, destination);
    }

    @Test
    void search_shouldReturnEmptyListWhenNoFlightsFound() {
        when(flightRepository.findByRoute("DEL", "NYC"))
                .thenReturn(List.of());

        List<Flight> result = flightSearchService.search("DEL", "NYC");

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(flightRepository).findByRoute("DEL", "NYC");
    }

    // -------------------------
    // getById()
    // -------------------------

    @Test
    void getById_shouldReturnFlightWhenFound() {
        Flight flight = mock(Flight.class);
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(flight));

        Flight result = flightSearchService.getById("FL-1");

        assertSame(flight, result);
        verify(flightRepository).findById("FL-1");
    }

    @Test
    void getById_shouldThrowExceptionWhenFlightNotFound() {
        when(flightRepository.findById("FL-404")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> flightSearchService.getById("FL-404")
        );

        assertEquals("Flight not found", ex.getMessage());
        verify(flightRepository).findById("FL-404");
    }
}
