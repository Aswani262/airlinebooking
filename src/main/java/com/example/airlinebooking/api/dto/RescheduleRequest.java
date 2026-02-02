package com.example.airlinebooking.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Captures a booking reschedule request so we can move passengers to a new flight and seats.
 */
@Data
public class RescheduleRequest {
    @NotBlank
    private String newFlightId;
    @NotEmpty
    private List<String> seatIds;
    private double amount;


    public RescheduleRequest(String newFlightId, List<String> seatIds) {
        this.newFlightId = newFlightId;
        this.seatIds = seatIds;
    }

}
