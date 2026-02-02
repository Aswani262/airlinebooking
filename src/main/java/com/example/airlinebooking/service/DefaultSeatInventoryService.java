package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.FareClass;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default seat inventory implementation backed by JDBC queries.
 */
@Service
public class DefaultSeatInventoryService implements SeatInventoryService {
    private final SeatJdbcRepository seatJdbcRepository;

    public DefaultSeatInventoryService(SeatJdbcRepository seatJdbcRepository) {
        this.seatJdbcRepository = seatJdbcRepository;
    }

    @Override
    public List<String> availableSeatIds(String flightId, FareClass fareClass) {
        return seatJdbcRepository.findByFlightIdAndFareClassAndStatus(flightId, fareClass, SeatStatus.AVAILABLE)
                .stream()
                .map(seat -> seat.getId())
                .toList();
    }
}
