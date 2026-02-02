package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.SeatLock;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.repository.FlightRepository;
import com.example.airlinebooking.repository.SeatLockRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeatLockServiceTest {
    @Test
    void locksAndReleasesSeats() {
        FlightRepository flightRepository = new FlightRepository();
        SeatLockRepository seatLockRepository = new SeatLockRepository();
        SeatLockService seatLockService = new SeatLockService(flightRepository, seatLockRepository);

        SeatLock lock = seatLockService.lockSeats("FL-100", List.of("AE2"));

        var flight = flightRepository.findById("FL-100").orElseThrow();
        var seatStatus = flight.getAircraft().seats().stream()
                .filter(seat -> seat.getId().equals("AE2"))
                .findFirst()
                .orElseThrow()
                .getStatus();
        assertThat(seatStatus).isEqualTo(SeatStatus.LOCKED);

        seatLockService.releaseLock(lock);

        var releasedStatus = flight.getAircraft().seats().stream()
                .filter(seat -> seat.getId().equals("AE2"))
                .findFirst()
                .orElseThrow()
                .getStatus();
        assertThat(releasedStatus).isEqualTo(SeatStatus.AVAILABLE);
    }

    @Test
    void rejectsUnavailableSeats() {
        FlightRepository flightRepository = new FlightRepository();
        SeatLockRepository seatLockRepository = new SeatLockRepository();
        SeatLockService seatLockService = new SeatLockService(flightRepository, seatLockRepository);

        seatLockService.lockSeats("FL-100", List.of("AE3"));

        assertThatThrownBy(() -> seatLockService.lockSeats("FL-100", List.of("AE3")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unavailable");
    }
}
