package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.SeatLock;
import com.example.airlinebooking.repository.jdbc.SeatLockEntity;
import com.example.airlinebooking.repository.jdbc.SeatLockJdbcRepository;
import com.example.airlinebooking.repository.jdbc.SeatLockSeatEntity;
import com.example.airlinebooking.repository.jdbc.SeatLockSeatJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdbcSeatLockRepositoryTest {

    @Mock private SeatLockJdbcRepository seatLockJdbcRepository;
    @Mock private JdbcAggregateTemplate jdbcAggregateTemplate;
    @Mock private SeatLockSeatJdbcRepository seatLockSeatJdbcRepository;

    private JdbcSeatLockRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JdbcSeatLockRepository(seatLockJdbcRepository, jdbcAggregateTemplate, seatLockSeatJdbcRepository);
    }

    @Test
    void insert_shouldInsertLockEntity_andSeatMappings_andReturnLock() {
        // Arrange
        Instant expiresAt = Instant.parse("2026-02-03T10:05:00Z");
        SeatLock lock = new SeatLock("LOCK-1", "FL-1", List.of("S1", "S2"), expiresAt);

        // Act
        SeatLock result = repository.insert(lock);

        // Assert: lock entity inserted
        ArgumentCaptor<SeatLockEntity> lockEntityCaptor = ArgumentCaptor.forClass(SeatLockEntity.class);
        verify(jdbcAggregateTemplate).insert(lockEntityCaptor.capture());

        SeatLockEntity inserted = lockEntityCaptor.getValue();
        assertEquals("LOCK-1", inserted.getId());
        assertEquals("FL-1", inserted.getFlightId());
        assertEquals(expiresAt, inserted.getExpiresAt());

        // Assert: seat rows inserted
        ArgumentCaptor<SeatLockSeatEntity> seatCaptor = ArgumentCaptor.forClass(SeatLockSeatEntity.class);
        verify(seatLockSeatJdbcRepository, times(2)).save(seatCaptor.capture());

        List<SeatLockSeatEntity> savedSeats = seatCaptor.getAllValues();
        assertEquals(2, savedSeats.size());

        assertEquals("LOCK-1", savedSeats.get(0).getLockId());
        assertEquals("S1", savedSeats.get(0).getSeatId());

        assertEquals("LOCK-1", savedSeats.get(1).getLockId());
        assertEquals("S2", savedSeats.get(1).getSeatId());

        // insert returns original lock
        assertSame(lock, result);

        // insert() should not use seatLockJdbcRepository.save()
        verifyNoInteractions(seatLockJdbcRepository);
    }

    @Test
    void findById_shouldReturnEmpty_whenLockNotFound() {
        when(seatLockJdbcRepository.findById("LOCK-404")).thenReturn(Optional.empty());

        Optional<SeatLock> result = repository.findById("LOCK-404");

        assertTrue(result.isEmpty());

        verify(seatLockJdbcRepository).findById("LOCK-404");
        verifyNoInteractions(seatLockSeatJdbcRepository);
    }

    @Test
    void findById_shouldReturnSeatLockWithSeatIds_whenFound() {
        // Arrange
        Instant expiresAt = Instant.parse("2026-02-03T10:05:00Z");

        SeatLockEntity entity = new SeatLockEntity("LOCK-1", "FL-1", expiresAt);
        when(seatLockJdbcRepository.findById("LOCK-1")).thenReturn(Optional.of(entity));

        SeatLockSeatEntity s1 = new SeatLockSeatEntity(1L, "LOCK-1", "S1");
        SeatLockSeatEntity s2 = new SeatLockSeatEntity(2L, "LOCK-1", "S2");
        when(seatLockSeatJdbcRepository.findByLockId("LOCK-1")).thenReturn(List.of(s1, s2));

        // Act
        Optional<SeatLock> result = repository.findById("LOCK-1");

        // Assert
        assertTrue(result.isPresent());
        SeatLock lock = result.get();

        assertEquals("LOCK-1", lock.id());
        assertEquals("FL-1", lock.flightId());
        assertEquals(expiresAt, lock.expiresAt());
        assertEquals(List.of("S1", "S2"), lock.seatIds());

        verify(seatLockJdbcRepository).findById("LOCK-1");
        verify(seatLockSeatJdbcRepository).findByLockId("LOCK-1");
    }

    @Test
    void delete_shouldDeleteSeatRowsThenDeleteLockRow() {
        // Act
        repository.delete("LOCK-1");

        // Assert order: child rows first, then parent
        InOrder inOrder = inOrder(seatLockSeatJdbcRepository, seatLockJdbcRepository);

        inOrder.verify(seatLockSeatJdbcRepository).deleteByLockId("LOCK-1");
        inOrder.verify(seatLockJdbcRepository).deleteById("LOCK-1");

        verifyNoMoreInteractions(seatLockSeatJdbcRepository, seatLockJdbcRepository);
    }

    @Test
    void deleteByFlightId_shouldDelegateToRepository() {
        repository.deleteByFlightId("FL-1");

        verify(seatLockJdbcRepository).deleteByFlightId("FL-1");
        verifyNoInteractions(seatLockSeatJdbcRepository, jdbcAggregateTemplate);
    }
}
