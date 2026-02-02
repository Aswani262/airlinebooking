package com.example.airlinebooking.repository.jdbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Flight persistence model optimized for route and schedule lookups via JDBC queries.
 */
@Table("flights")
public class FlightEntity {
    @Id
    private String id;
    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private String aircraftId;

//    public FlightEntity() {
//    }

   // @PersistenceCreator
    public FlightEntity(String id, String flightNumber, String origin, String destination, LocalDateTime departureTime, String aircraftId) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.aircraftId = aircraftId;
    }

    public String getId() {
        return id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public String getAircraftId() {
        return aircraftId;
    }
}
