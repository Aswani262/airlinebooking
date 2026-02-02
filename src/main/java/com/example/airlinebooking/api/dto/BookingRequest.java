package com.example.airlinebooking.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Request payload modeled explicitly to validate incoming booking data at the API boundary.
 */
@Data
public class BookingRequest {
    @NotBlank
    private String flightId;
    @NotEmpty
    private Map<String,String> passengersSeatMap; // passenger name to seat ID
    @Positive
    private int amount;
    private LocalDate bookingDate;

}
