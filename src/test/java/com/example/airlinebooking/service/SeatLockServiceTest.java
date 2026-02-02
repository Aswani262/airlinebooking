package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.Aircraft;
import com.example.airlinebooking.domain.Flight;
import com.example.airlinebooking.domain.SeatLock;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.repository.FlightRepository;
import com.example.airlinebooking.repository.SeatLockRepository;
import com.example.airlinebooking.repository.jdbc.SeatEntity;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SeatLockServiceTest {
    @Test
    void locksAndReleasesSeats() {
        FlightRepository flightRepository = mock(FlightRepository.class);
        SeatLockRepository seatLockRepository = mock(SeatLockRepository.class);
        SeatJdbcRepository seatJdbcRepository = mock(SeatJdbcRepository.class);
        SeatLockService seatLockService = new DefaultSeatLockService(flightRepository, seatLockRepository, seatJdbcRepository);

        Flight flight = new Flight("FL-100", "XY100", "JFK", "SFO", LocalDateTime.now(),
                new Aircraft("AC-1", "A320", Collections.emptyList()));
        when(flightRepository.findById("FL-100")).thenReturn(Optional.of(flight));
        SeatEntity seatEntity = new SeatEntity("AE2", "FL-100", "AC-1", "2A",
                com.example.airlinebooking.domain.FareClass.ECONOMY, SeatStatus.AVAILABLE);
        when(seatJdbcRepository.findByFlightIdAndSeatIds("FL-100", List.of("AE2"))).thenReturn(List.of(seatEntity));
        when(seatJdbcRepository.updateStatusIfCurrent("FL-100", List.of("AE2"), SeatStatus.AVAILABLE, SeatStatus.LOCKED))
                .thenReturn(1);
        SeatLock lock = new SeatLock("LOCK-1", "FL-100", List.of("AE2"), Instant.now().plusSeconds(600));
        when(seatLockRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(lock);

        SeatLock created = seatLockService.lockSeats("FL-100", List.of("AE2"));

        verify(seatLockRepository).save(created);

        seatLockService.releaseLock(created);

        verify(seatJdbcRepository).updateStatusIfCurrent("FL-100", List.of("AE2"), SeatStatus.LOCKED, SeatStatus.AVAILABLE);
        verify(seatLockRepository).delete("LOCK-1");
    }

    @Test
    void rejectsUnavailableSeats() {
        FlightRepository flightRepository = mock(FlightRepository.class);
        SeatLockRepository seatLockRepository = mock(SeatLockRepository.class);
        SeatJdbcRepository seatJdbcRepository = mock(SeatJdbcRepository.class);
        SeatLockService seatLockService = new DefaultSeatLockService(flightRepository, seatLockRepository, seatJdbcRepository);

        Flight flight = new Flight("FL-100", "XY100", "JFK", "SFO", LocalDateTime.now(),
                new Aircraft("AC-1", "A320", Collections.emptyList()));
        when(flightRepository.findById("FL-100")).thenReturn(Optional.of(flight));
        SeatEntity seatEntity = new SeatEntity("AE3", "FL-100", "AC-1", "3A",
                com.example.airlinebooking.domain.FareClass.ECONOMY, SeatStatus.BOOKED);
        when(seatJdbcRepository.findByFlightIdAndSeatIds("FL-100", List.of("AE3"))).thenReturn(List.of(seatEntity));

        assertThatThrownBy(() -> seatLockService.lockSeats("FL-100", List.of("AE3")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unavailable");
    }
}
