package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.FareClass;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Read-only seat inventory queries to keep availability checks separate from lock mutations.
 */
@Service
public class SeatInventoryService {
    private final SeatJdbcRepository seatJdbcRepository;

    public SeatInventoryService(SeatJdbcRepository seatJdbcRepository) {
        this.seatJdbcRepository = seatJdbcRepository;
    }

    public List<String> availableSeatIds(String flightId, FareClass fareClass) {
        return seatJdbcRepository.findByFlightIdAndFareClassAndStatus(flightId, fareClass, SeatStatus.AVAILABLE)
                .stream()
                .map(seat -> seat.getId())
                .toList();
    }
}
