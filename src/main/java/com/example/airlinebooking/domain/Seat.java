package com.example.airlinebooking.domain;

import java.util.Objects;

/**
 * Mutable seat aggregate so status transitions (available/locked/booked) can be applied safely in memory.
 * We keep identity-based equality to avoid duplication across inventory operations.
 */
public class Seat {
    private final String id;
    private final String seatNumber;
    private final FareClass fareClass;
    private SeatStatus status;

    public Seat(String id, String seatNumber, FareClass fareClass, SeatStatus status) {
        this.id = id;
        this.seatNumber = seatNumber;
        this.fareClass = fareClass;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public FareClass getFareClass() {
        return fareClass;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Seat seat = (Seat) o;
        return Objects.equals(id, seat.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
