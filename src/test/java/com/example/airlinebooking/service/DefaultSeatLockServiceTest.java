package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.SeatLock;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.repository.SeatLockRepository;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import com.example.airlinebooking.repository.jdbc.SeatEntity; // <-- adjust if your seat type differs
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultSeatLockServiceTest {

    @Mock private SeatLockRepository seatLockRepository;
    @Mock private SeatJdbcRepository seatJdbcRepository;

    @InjectMocks
    private DefaultSeatLockService seatLockService;

    @Test
    void lockSeats_shouldLockSeatsAndPersistSeatLock() {
        String flightId = "FL-1";
        List<String> seatIds = List.of("S1", "S2");

        SeatEntity s1 = mock(SeatEntity.class);
        SeatEntity s2 = mock(SeatEntity.class);

        when(s1.getStatus()).thenReturn(SeatStatus.AVAILABLE);
        when(s2.getStatus()).thenReturn(SeatStatus.AVAILABLE);

        when(seatJdbcRepository.findByFlightIdAndSeatIds(flightId, seatIds))
                .thenReturn(List.of(s1, s2));

        when(seatJdbcRepository.updateStatusIfCurrent(
                flightId, seatIds, SeatStatus.AVAILABLE, SeatStatus.LOCKED
        )).thenReturn(2);

        // Act
        SeatLock lock = seatLockService.lockSeats(flightId, seatIds);

        // Assert
        assertNotNull(lock);
        assertNotNull(lock.id());
        assertEquals(flightId, lock.flightId());
        assertEquals(seatIds, lock.seatIds());
        assertTrue(lock.expiresAt().isAfter(Instant.now()), "Lock expiry should be in the future");

        // persisted
        verify(seatLockRepository).insert(lock);

        // seat status updated
        verify(seatJdbcRepository).updateStatusIfCurrent(
                flightId, seatIds, SeatStatus.AVAILABLE, SeatStatus.LOCKED
        );
    }

    @Test
    void lockSeats_shouldThrowIfSomeSeatsDoNotExist() {
        String flightId = "FL-1";
        List<String> seatIds = List.of("S1", "S2");

        SeatEntity onlyOne = mock(SeatEntity.class);

        when(seatJdbcRepository.findByFlightIdAndSeatIds(flightId, seatIds))
                .thenReturn(List.of(onlyOne)); // size mismatch

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> seatLockService.lockSeats(flightId, seatIds)
        );

        assertEquals("Some seats do not exist", ex.getMessage());

        verify(seatJdbcRepository).findByFlightIdAndSeatIds(flightId, seatIds);
        verify(seatJdbcRepository, never()).updateStatusIfCurrent(anyString(), anyList(), any(), any());
        verifyNoInteractions(seatLockRepository);
    }

    @Test
    void lockSeats_shouldThrowIfAnySeatUnavailable() {
        String flightId = "FL-1";
        List<String> seatIds = List.of("S1", "S2");

        SeatEntity s1 = mock(SeatEntity.class);
        SeatEntity s2 = mock(SeatEntity.class);

        when(s1.getStatus()).thenReturn(SeatStatus.AVAILABLE);
        when(s2.getStatus()).thenReturn(SeatStatus.BOOKED); // unavailable

        when(seatJdbcRepository.findByFlightIdAndSeatIds(flightId, seatIds))
                .thenReturn(List.of(s1, s2));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> seatLockService.lockSeats(flightId, seatIds)
        );

        assertEquals("One or more seats are unavailable", ex.getMessage());

        verify(seatJdbcRepository).findByFlightIdAndSeatIds(flightId, seatIds);
        verify(seatJdbcRepository, never()).updateStatusIfCurrent(anyString(), anyList(), any(), any());
        verifyNoInteractions(seatLockRepository);
    }

    @Test
    void lockSeats_shouldThrowIfUpdateCountDoesNotMatchSeatIds() {
        String flightId = "FL-1";
        List<String> seatIds = List.of("S1", "S2");

        SeatEntity s1 = mock(SeatEntity.class);
        SeatEntity s2 = mock(SeatEntity.class);

        when(s1.getStatus()).thenReturn(SeatStatus.AVAILABLE);
        when(s2.getStatus()).thenReturn(SeatStatus.AVAILABLE);

        when(seatJdbcRepository.findByFlightIdAndSeatIds(flightId, seatIds))
                .thenReturn(List.of(s1, s2));

        // updated less than requested (race condition)
        when(seatJdbcRepository.updateStatusIfCurrent(
                flightId, seatIds, SeatStatus.AVAILABLE, SeatStatus.LOCKED
        )).thenReturn(1);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> seatLockService.lockSeats(flightId, seatIds)
        );

        assertEquals("Seat availability changed while locking", ex.getMessage());

        verify(seatJdbcRepository).updateStatusIfCurrent(
                flightId, seatIds, SeatStatus.AVAILABLE, SeatStatus.LOCKED
        );
        verifyNoInteractions(seatLockRepository);
    }

    @Test
    void releaseLock_shouldDeleteByFlightId() {
        seatLockService.releaseLock("FL-1");

        verify(seatLockRepository).deleteByFlightId("FL-1");
        verifyNoInteractions(seatJdbcRepository);
    }
}
