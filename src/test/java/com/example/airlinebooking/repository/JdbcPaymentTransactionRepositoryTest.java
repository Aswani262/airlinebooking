package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.PaymentStatus;
import com.example.airlinebooking.domain.PaymentTransaction;
import com.example.airlinebooking.repository.jdbc.PaymentTransactionEntity;
import com.example.airlinebooking.repository.jdbc.PaymentTransactionJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdbcPaymentTransactionRepositoryTest {

    @Mock private PaymentTransactionJdbcRepository transactionJdbcRepository;
    @Mock private JdbcAggregateTemplate jdbcAggregateTemplate;

    private JdbcPaymentTransactionRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JdbcPaymentTransactionRepository(transactionJdbcRepository, jdbcAggregateTemplate);
    }

    @Test
    void insert_shouldInsertEntityUsingJdbcAggregateTemplate_andReturnTransaction() {
        // Arrange
        Instant createdAt = Instant.parse("2026-02-03T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-02-03T10:01:00Z");

        PaymentTransaction tx = new PaymentTransaction();
        tx.setId("TX-1");
        tx.setBookingId("B-1");
        tx.setAmount(1500.0);
        tx.setStatus(PaymentStatus.PENDING);
        tx.setCreatedAt(createdAt);
        tx.setUpdatesAt(updatedAt);
        tx.setPaymentProviderTransactionId("PG-123");
        tx.setPaymentProviderResponse("{\"status\":\"PENDING\"}");

        // Act
        PaymentTransaction result = repository.insert(tx);

        // Assert
        ArgumentCaptor<PaymentTransactionEntity> captor = ArgumentCaptor.forClass(PaymentTransactionEntity.class);
        verify(jdbcAggregateTemplate).insert(captor.capture());

        PaymentTransactionEntity entity = captor.getValue();
        assertEquals("TX-1", entity.getId());
        assertEquals("B-1", entity.getBookingId());
        assertEquals(1500.0, entity.getAmount());
        assertEquals(PaymentStatus.PENDING, entity.getStatus());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatesAt());
        assertEquals("PG-123", entity.getPaymentProviderTransactionId());
        assertEquals("{\"status\":\"PENDING\"}", entity.getPaymentProviderResponse());

        // insert() should not call save()
        verifyNoInteractions(transactionJdbcRepository);

        assertSame(tx, result);
    }

    @Test
    void update_shouldSaveEntityUsingJdbcRepository_andReturnTransaction() {
        // Arrange
        Instant createdAt = Instant.parse("2026-02-03T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-02-03T10:02:00Z");

        PaymentTransaction tx = new PaymentTransaction();
        tx.setId("TX-1");
        tx.setBookingId("B-1");
        tx.setAmount(1500.0);
        tx.setStatus(PaymentStatus.SUCCEEDED);
        tx.setCreatedAt(createdAt);
        tx.setUpdatesAt(updatedAt);
        tx.setPaymentProviderTransactionId("PG-123");
        tx.setPaymentProviderResponse("{\"status\":\"SUCCESS\"}");

        when(transactionJdbcRepository.save(any(PaymentTransactionEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        PaymentTransaction result = repository.update(tx);

        // Assert
        ArgumentCaptor<PaymentTransactionEntity> captor = ArgumentCaptor.forClass(PaymentTransactionEntity.class);
        verify(transactionJdbcRepository).save(captor.capture());

        PaymentTransactionEntity entity = captor.getValue();
        assertEquals("TX-1", entity.getId());
        assertEquals("B-1", entity.getBookingId());
        assertEquals(1500.0, entity.getAmount());
        assertEquals(PaymentStatus.SUCCEEDED, entity.getStatus());
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatesAt());
        assertEquals("PG-123", entity.getPaymentProviderTransactionId());
        assertEquals("{\"status\":\"SUCCESS\"}", entity.getPaymentProviderResponse());

        // update() should not call insert(template)
        verifyNoInteractions(jdbcAggregateTemplate);

        assertSame(tx, result);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        when(transactionJdbcRepository.findById("TX-404")).thenReturn(Optional.empty());

        Optional<PaymentTransaction> result = repository.findById("TX-404");

        assertTrue(result.isEmpty());
        verify(transactionJdbcRepository).findById("TX-404");
        verifyNoInteractions(jdbcAggregateTemplate);
    }

    @Test
    void findById_shouldMapEntityToDomainTransaction_whenFound() {
        // Arrange
        Instant createdAt = Instant.parse("2026-02-03T10:00:00Z");
        Instant updatedAt = Instant.parse("2026-02-03T10:02:00Z");

        PaymentTransactionEntity entity = new PaymentTransactionEntity(
                "TX-1",
                "B-1",
                1500.0,
                PaymentStatus.PENDING,
                createdAt,
                updatedAt,
                "PG-123",
                "{\"status\":\"PENDING\"}"
        );

        when(transactionJdbcRepository.findById("TX-1")).thenReturn(Optional.of(entity));

        // Act
        Optional<PaymentTransaction> result = repository.findById("TX-1");

        // Assert
        assertTrue(result.isPresent());

        PaymentTransaction tx = result.get();
        assertEquals("TX-1", tx.getId());
        assertEquals("B-1", tx.getBookingId());
        assertEquals(1500.0, tx.getAmount());
        assertEquals(PaymentStatus.PENDING, tx.getStatus());
        assertEquals(createdAt, tx.getCreatedAt());
        assertEquals(updatedAt, tx.getUpdatesAt());
        assertEquals("PG-123", tx.getPaymentProviderTransactionId());
        assertEquals("{\"status\":\"PENDING\"}", tx.getPaymentProviderResponse());

        verify(transactionJdbcRepository).findById("TX-1");
        verifyNoInteractions(jdbcAggregateTemplate);
    }
}
