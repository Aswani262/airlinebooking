package com.example.airlinebooking.domain;

import jdk.jfr.DataAmount;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents a scheduled flight and its assigned aircraft; encapsulated to act as the booking consistency boundary.
 */
@Data
public class Flight {
    private final String id;
    private final String flightNumber;
    private final String origin;
    private final String destination;
    private final LocalDateTime departureTime;
    private final Aircraft aircraft;

}
