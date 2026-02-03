package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import com.example.airlinebooking.repository.jdbc.SeatLockEntity;
import com.example.airlinebooking.repository.jdbc.SeatLockJdbcRepository;
import com.example.airlinebooking.repository.jdbc.SeatLockSeatJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatLockCleanupSchedulerTest {

    @Mock private SeatLockJdbcRepository seatLockJdbcRepository;
    @Mock private SeatLockSeatJdbcRepository seatLockSeatJdbcRepository;
    @Mock private SeatJdbcRepository seatJdbcRepository;

    private SeatLockCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new SeatLockCleanupScheduler(seatLockJdbcRepository, seatLockSeatJdbcRepository, seatJdbcRepository);
    }

    @Test
    void cleanupExpiredLocks_shouldReturnImmediately_whenNoExpiredLocks() {
        when(seatLockJdbcRepository.findExpiredLocks(any(Instant.class)))
                .thenReturn(List.of());

        scheduler.cleanupExpiredLocks();

        verify(seatLockJdbcRepository).findExpiredLocks(any(Instant.class));
        verifyNoInteractions(seatLockSeatJdbcRepository, seatJdbcRepository);
    }

    @Test
    void cleanupExpiredLocks_shouldDeleteLockRows_whenNoSeatsFoundForLock() {
        SeatLockEntity lock = mock(SeatLockEntity.class);
        when(lock.getId()).thenReturn("LOCK-1");

        when(seatLockJdbcRepository.findExpiredLocks(any(Instant.class)))
                .thenReturn(List.of(lock));
        when(seatLockSeatJdbcRepository.findSeatIdsByLockId("LOCK-1"))
                .thenReturn(List.of()); // no seats

        scheduler.cleanupExpiredLocks();

        verify(seatLockSeatJdbcRepository).findSeatIdsByLockId("LOCK-1");

        // no seat update
        verify(seatJdbcRepository, never()).updateStatusByIds(anyList(), any());

        // delete child then parent
        InOrder inOrder = inOrder(seatLockSeatJdbcRepository, seatLockJdbcRepository);
        inOrder.verify(seatLockSeatJdbcRepository).deleteByLockId("LOCK-1");
        inOrder.verify(seatLockJdbcRepository).deleteById("LOCK-1");
    }

    @Test
    void cleanupExpiredLocks_shouldReleaseSeatsAndDeleteLockRows_whenSeatsFound() {
        SeatLockEntity lock = mock(SeatLockEntity.class);
        when(lock.getId()).thenReturn("LOCK-1");

        when(seatLockJdbcRepository.findExpiredLocks(any(Instant.class)))
                .thenReturn(List.of(lock));
        when(seatLockSeatJdbcRepository.findSeatIdsByLockId("LOCK-1"))
                .thenReturn(List.of("S1", "S2"));

        when(seatJdbcRepository.updateStatusByIds(List.of("S1", "S2"), SeatStatus.AVAILABLE))
                .thenReturn(2);

        scheduler.cleanupExpiredLocks();

        verify(seatLockSeatJdbcRepository).findSeatIdsByLockId("LOCK-1");
        verify(seatJdbcRepository).updateStatusByIds(List.of("S1", "S2"), SeatStatus.AVAILABLE);

        InOrder inOrder = inOrder(seatLockSeatJdbcRepository, seatLockJdbcRepository);
        inOrder.verify(seatLockSeatJdbcRepository).deleteByLockId("LOCK-1");
        inOrder.verify(seatLockJdbcRepository).deleteById("LOCK-1");
    }

    @Test
    void cleanupExpiredLocks_shouldProcessMultipleLocks() {
        SeatLockEntity lock1 = mock(SeatLockEntity.class);
        SeatLockEntity lock2 = mock(SeatLockEntity.class);
        when(lock1.getId()).thenReturn("LOCK-1");
        when(lock2.getId()).thenReturn("LOCK-2");

        when(seatLockJdbcRepository.findExpiredLocks(any(Instant.class)))
                .thenReturn(List.of(lock1, lock2));

        when(seatLockSeatJdbcRepository.findSeatIdsByLockId("LOCK-1"))
                .thenReturn(List.of("S1"));
        when(seatLockSeatJdbcRepository.findSeatIdsByLockId("LOCK-2"))
                .thenReturn(List.of()); // second has no seats

        when(seatJdbcRepository.updateStatusByIds(List.of("S1"), SeatStatus.AVAILABLE))
                .thenReturn(1);

        scheduler.cleanupExpiredLocks();

        // lock1 seats updated
        verify(seatJdbcRepository).updateStatusByIds(List.of("S1"), SeatStatus.AVAILABLE);

        // both locks deleted
        verify(seatLockSeatJdbcRepository).deleteByLockId("LOCK-1");
        verify(seatLockJdbcRepository).deleteById("LOCK-1");

        verify(seatLockSeatJdbcRepository).deleteByLockId("LOCK-2");
        verify(seatLockJdbcRepository).deleteById("LOCK-2");
    }
}
