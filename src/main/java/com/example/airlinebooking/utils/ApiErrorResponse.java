package com.example.airlinebooking.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class ApiErrorResponse {
    private String message;
    private OffsetDateTime timestamp;

    public static ApiErrorResponse of( String message) {
        return new ApiErrorResponse(message, OffsetDateTime.now());
    }
}
