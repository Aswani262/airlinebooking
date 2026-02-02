package com.example.airlinebooking.domain;

import lombok.Data;

import java.util.Objects;

@Data
public class Seat {
    private final String id;
    private final String seatNumber;
    private final FareClass fareClass;
    private final SeatStatus status;

    public Seat(String id, String seatNumber, FareClass fareClass, SeatStatus status) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.fareClass = fareClass;
        this.status = status;
    }

}
