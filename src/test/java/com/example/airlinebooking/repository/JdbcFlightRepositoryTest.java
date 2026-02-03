package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.Aircraft;
import com.example.airlinebooking.domain.FareClass;
import com.example.airlinebooking.domain.Flight;
import com.example.airlinebooking.domain.Seat;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.repository.jdbc.AircraftEntity;
import com.example.airlinebooking.repository.jdbc.AircraftJdbcRepository;
import com.example.airlinebooking.repository.jdbc.FlightEntity;
import com.example.airlinebooking.repository.jdbc.FlightJdbcRepository;
import com.example.airlinebooking.repository.jdbc.SeatEntity;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdbcFlightRepositoryTest {

    @Mock private FlightJdbcRepository flightJdbcRepository;
    @Mock private AircraftJdbcRepository aircraftJdbcRepository;
    @Mock private SeatJdbcRepository seatJdbcRepository;

    private JdbcFlightRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JdbcFlightRepository(flightJdbcRepository, aircraftJdbcRepository, seatJdbcRepository);
    }

    @Test
    void findByRoute_shouldMapFlights_withoutSeats() {
        // Arrange
        FlightEntity fe1 = new FlightEntity();
        fe1.setId("FL-1");
        fe1.setFlightNumber("AI-101");
        fe1.setOrigin("DEL");
        fe1.setDestination("BLR");
        fe1.setDepartureTime(LocalDateTime.parse("2026-02-10T10:15:00"));
        fe1.setAircraftId("AC-1");

        FlightEntity fe2 = new FlightEntity();
        fe2.setId("FL-2");
        fe2.setFlightNumber("AI-102");
        fe2.setOrigin("DEL");
        fe2.setDestination("BLR");
        fe2.setDepartureTime(LocalDateTime.parse("2026-02-10T12:15:00"));
        fe2.setAircraftId("AC-2");

        when(flightJdbcRepository.findByRoute("DEL", "BLR")).thenReturn(List.of(fe1, fe2));

        when(aircraftJdbcRepository.findById("AC-1"))
                .thenReturn(Optional.of(new AircraftEntity("AC-1", "A320")));
        when(aircraftJdbcRepository.findById("AC-2"))
                .thenReturn(Optional.of(new AircraftEntity("AC-2", "B737")));

        // Act
        List<Flight> flights = repository.findByRoute("DEL", "BLR");

        // Assert
        assertEquals(2, flights.size());

        Flight f1 = flights.get(0);
        assertEquals("FL-1", f1.getId());
        assertEquals("AI-101", f1.getFlightNumber());
        assertEquals("DEL", f1.getOrigin());
        assertEquals("BLR", f1.getDestination());
        assertEquals(LocalDateTime.parse("2026-02-10T10:15:00"), f1.getDepartureTime());

        Aircraft a1 = f1.getAircraft();
        assertEquals("AC-1", a1.id());
        assertEquals("A320", a1.model());
        assertNotNull(a1.seats());
        assertTrue(a1.seats().isEmpty(), "Seats must be empty for findByRoute()");

        Flight f2 = flights.get(1);
        assertEquals("FL-2", f2.getId());
        assertEquals("AC-2", f2.getAircraft().id());
        assertTrue(f2.getAircraft().seats().isEmpty(), "Seats must be empty for findByRoute()");

        // findByRoute must NOT fetch seats
        verify(seatJdbcRepository, never()).findByFlightId(anyString());

        verify(flightJdbcRepository).findByRoute("DEL", "BLR");
        verify(aircraftJdbcRepository).findById("AC-1");
        verify(aircraftJdbcRepository).findById("AC-2");
    }

    @Test
    void findByRoute_shouldReturnEmptyList_whenNoFlights() {
        when(flightJdbcRepository.findByRoute("DEL", "XXX")).thenReturn(List.of());

        List<Flight> flights = repository.findByRoute("DEL", "XXX");

        assertNotNull(flights);
        assertTrue(flights.isEmpty());

        verify(flightJdbcRepository).findByRoute("DEL", "XXX");
        verifyNoInteractions(aircraftJdbcRepository, seatJdbcRepository);
    }

    @Test
    void findById_shouldReturnFlight_withSeats() {
        // Arrange
        FlightEntity fe = new FlightEntity();
        fe.setId("FL-1");
        fe.setFlightNumber("AI-101");
        fe.setOrigin("DEL");
        fe.setDestination("BLR");
        fe.setDepartureTime(LocalDateTime.parse("2026-02-10T10:15:00"));
        fe.setAircraftId("AC-1");

        when(flightJdbcRepository.findById("FL-1")).thenReturn(Optional.of(fe));
        when(aircraftJdbcRepository.findById("AC-1")).thenReturn(Optional.of(new AircraftEntity("AC-1", "A320")));

        SeatEntity se1 = new SeatEntity();
        se1.setId("S1");
        se1.setSeatNumber("1A");
        se1.setFareClass(FareClass.BUSINESS);
        se1.setStatus(SeatStatus.AVAILABLE);

        SeatEntity se2 = new SeatEntity();
        se2.setId("S2");
        se2.setSeatNumber("1B");
        se2.setFareClass(FareClass.BUSINESS);
        se2.setStatus(SeatStatus.BOOKED);

        when(seatJdbcRepository.findByFlightId("FL-1")).thenReturn(List.of(se1, se2));

        // Act
        Optional<Flight> result = repository.findById("FL-1");

        // Assert
        assertTrue(result.isPresent());
        Flight flight = result.get();

        assertEquals("FL-1", flight.getId());
        assertEquals("AI-101", flight.getFlightNumber());
        assertEquals("DEL", flight.getOrigin());
        assertEquals("BLR", flight.getDestination());

        assertEquals("AC-1", flight.getAircraft().id());
        assertEquals("A320", flight.getAircraft().model());

        List<Seat> seats = flight.getAircraft().seats();
        assertEquals(2, seats.size());

        assertEquals("S1", seats.get(0).getId());
        assertEquals("1A", seats.get(0).getSeatNumber());
        assertEquals(FareClass.BUSINESS, seats.get(0).getFareClass());
        assertEquals(SeatStatus.AVAILABLE, seats.get(0).getStatus());

        assertEquals("S2", seats.get(1).getId());
        assertEquals("1B", seats.get(1).getSeatNumber());
        assertEquals(SeatStatus.BOOKED, seats.get(1).getStatus());

        verify(flightJdbcRepository).findById("FL-1");
        verify(aircraftJdbcRepository).findById("AC-1");
        verify(seatJdbcRepository).findByFlightId("FL-1");
    }

    @Test
    void findById_shouldReturnEmpty_whenFlightNotFound() {
        when(flightJdbcRepository.findById("FL-404")).thenReturn(Optional.empty());

        Optional<Flight> result = repository.findById("FL-404");

        assertTrue(result.isEmpty());

        verify(flightJdbcRepository).findById("FL-404");
        verifyNoInteractions(aircraftJdbcRepository, seatJdbcRepository);
    }

    @Test
    void findById_shouldFallbackToUnknownAircraft_whenAircraftMissing() {
        // Arrange
        FlightEntity fe = new FlightEntity();
        fe.setId("FL-1");
        fe.setFlightNumber("AI-101");
        fe.setOrigin("DEL");
        fe.setDestination("BLR");
        fe.setDepartureTime(LocalDateTime.parse("2026-02-10T10:15:00"));
        fe.setAircraftId("AC-MISSING");

        when(flightJdbcRepository.findById("FL-1")).thenReturn(Optional.of(fe));
        when(aircraftJdbcRepository.findById("AC-MISSING")).thenReturn(Optional.empty());

        // includeSeats=true for findById; but seat list can be empty
        when(seatJdbcRepository.findByFlightId("FL-1")).thenReturn(List.of());

        // Act
        Flight flight = repository.findById("FL-1").orElseThrow();

        // Assert fallback aircraft
        assertEquals("AC-MISSING", flight.getAircraft().id());
        assertEquals("UNKNOWN", flight.getAircraft().model());
        assertTrue(flight.getAircraft().seats().isEmpty());

        verify(aircraftJdbcRepository).findById("AC-MISSING");
        verify(seatJdbcRepository).findByFlightId("FL-1");
    }
}
