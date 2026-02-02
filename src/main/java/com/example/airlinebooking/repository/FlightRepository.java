package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.Aircraft;
import com.example.airlinebooking.domain.FareClass;
import com.example.airlinebooking.domain.Flight;
import com.example.airlinebooking.domain.Seat;
import com.example.airlinebooking.domain.SeatStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory flight repository to keep the sample self-contained while modeling inventory lookups.
 */
@Repository
public class FlightRepository {
    private final Map<String, Flight> flights = new ConcurrentHashMap<>();

    public FlightRepository() {
        seed();
    }

    public List<Flight> findByRoute(String origin, String destination) {
        return flights.values().stream()
                .filter(flight -> flight.getOrigin().equalsIgnoreCase(origin))
                .filter(flight -> flight.getDestination().equalsIgnoreCase(destination))
                .collect(Collectors.toList());
    }

    public Optional<Flight> findById(String id) {
        return Optional.ofNullable(flights.get(id));
    }

    private void seed() {
        flights.put("FL-100", new Flight(
                "FL-100",
                "XY100",
                "JFK",
                "SFO",
                LocalDateTime.now().plusDays(3),
                new Aircraft("AC-1", "A320", buildSeats("A"))
        ));
        flights.put("FL-200", new Flight(
                "FL-200",
                "XY200",
                "SFO",
                "SEA",
                LocalDateTime.now().plusDays(4),
                new Aircraft("AC-2", "B737", buildSeats("B"))
        ));
    }

    private List<Seat> buildSeats(String prefix) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            seats.add(new Seat(prefix + "E" + i, "" + i + "A", FareClass.ECONOMY, SeatStatus.AVAILABLE));
        }
        for (int i = 1; i <= 4; i++) {
            seats.add(new Seat(prefix + "P" + i, "" + (20 + i) + "B", FareClass.PREMIUM_ECONOMY, SeatStatus.AVAILABLE));
        }
        for (int i = 1; i <= 4; i++) {
            seats.add(new Seat(prefix + "B" + i, "" + (30 + i) + "C", FareClass.BUSINESS, SeatStatus.AVAILABLE));
        }
        return seats;
    }
}
