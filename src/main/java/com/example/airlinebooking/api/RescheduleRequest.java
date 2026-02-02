package com.example.airlinebooking.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Captures a booking reschedule request so we can move passengers to a new flight and seats.
 */
public class RescheduleRequest {
    @NotBlank
    private String newFlightId;
    @NotEmpty
    private List<String> seatIds;

    public RescheduleRequest() {
    }

    public RescheduleRequest(String newFlightId, List<String> seatIds) {
        this.newFlightId = newFlightId;
        this.seatIds = seatIds;
    }

    public String getNewFlightId() {
        return newFlightId;
    }

    public List<String> getSeatIds() {
        return seatIds;
    }
}
