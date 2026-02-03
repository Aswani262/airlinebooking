package com.example.airlinebooking.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Captures a booking reschedule request so we can move passengers to a new flight and seats.
 */
@Data
public class RescheduleRequest {
    @NotBlank
    private String flightId;
    @NotEmpty
    private Map<String,String> passengersSeatMap; // passenger name to seat ID
    @Positive
    private int amount;
    private LocalDate bookingDate;

}
