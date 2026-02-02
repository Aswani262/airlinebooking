package com.example.airlinebooking.domain;

import java.time.LocalDateTime;

/**
 * Represents a scheduled flight and its assigned aircraft; encapsulated to act as the booking consistency boundary.
 */
public class Flight {
    private final String id;
    private final String flightNumber;
    private final String origin;
    private final String destination;
    private final LocalDateTime departureTime;
    private final Aircraft aircraft;

    public Flight(String id, String flightNumber, String origin, String destination, LocalDateTime departureTime, Aircraft aircraft) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.aircraft = aircraft;
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

    public Aircraft getAircraft() {
        return aircraft;
    }
}
