package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.Aircraft;
import com.example.airlinebooking.domain.Flight;
import com.example.airlinebooking.domain.Seat;
import com.example.airlinebooking.repository.jdbc.AircraftEntity;
import com.example.airlinebooking.repository.jdbc.AircraftJdbcRepository;
import com.example.airlinebooking.repository.jdbc.FlightEntity;
import com.example.airlinebooking.repository.jdbc.FlightJdbcRepository;
import com.example.airlinebooking.repository.jdbc.SeatEntity;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JDBC-backed flight repository to ensure search results come from PostgreSQL.
 */
@Repository
public class JdbcFlightRepository implements FlightRepository {
    private final FlightJdbcRepository flightJdbcRepository;
    private final AircraftJdbcRepository aircraftJdbcRepository;
    private final SeatJdbcRepository seatJdbcRepository;

    public JdbcFlightRepository(FlightJdbcRepository flightJdbcRepository, AircraftJdbcRepository aircraftJdbcRepository,
                                SeatJdbcRepository seatJdbcRepository) {
        this.flightJdbcRepository = flightJdbcRepository;
        this.aircraftJdbcRepository = aircraftJdbcRepository;
        this.seatJdbcRepository = seatJdbcRepository;
    }

    @Override
    public List<Flight> findByRoute(String origin, String destination) {
        return flightJdbcRepository.findByRoute(origin, destination)
                .stream()
                .map(entity -> toDomain(entity, false))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Flight> findById(String id) {
        return flightJdbcRepository.findById(id)
                .map(entity -> toDomain(entity, true));
    }

    private Flight toDomain(FlightEntity entity, boolean includeSeats) {
        AircraftEntity aircraftEntity = aircraftJdbcRepository.findById(entity.getAircraftId())
                .orElseGet(() -> new AircraftEntity(entity.getAircraftId(), "UNKNOWN"));
        List<Seat> seats = includeSeats
                ? seatJdbcRepository.findByFlightId(entity.getId()).stream()
                .map(this::toSeat)
                .collect(Collectors.toList())
                : Collections.emptyList();
        Aircraft aircraft = new Aircraft(aircraftEntity.getId(), aircraftEntity.getModel(), seats);
        return new Flight(entity.getId(), entity.getFlightNumber(), entity.getOrigin(), entity.getDestination(),
                entity.getDepartureTime(), aircraft);
    }

    private Seat toSeat(SeatEntity entity) {
        return new Seat(entity.getId(), entity.getSeatNumber(), entity.getFareClass(), entity.getStatus());
    }
}
