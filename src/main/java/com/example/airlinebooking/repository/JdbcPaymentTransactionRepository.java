package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.PaymentStatus;
import com.example.airlinebooking.domain.PaymentTransaction;
import com.example.airlinebooking.repository.jdbc.PaymentTransactionEntity;
import com.example.airlinebooking.repository.jdbc.PaymentTransactionJdbcRepository;
import com.example.airlinebooking.repository.jdbc.PaymentTransactionSeatEntity;
import com.example.airlinebooking.repository.jdbc.PaymentTransactionSeatJdbcRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JDBC-backed payment transaction repository for persistent payment tracking.
 */
@Repository
public class JdbcPaymentTransactionRepository implements PaymentTransactionRepository {
    private final PaymentTransactionJdbcRepository transactionJdbcRepository;
    private final PaymentTransactionSeatJdbcRepository transactionSeatJdbcRepository;

    public JdbcPaymentTransactionRepository(PaymentTransactionJdbcRepository transactionJdbcRepository,
                                            PaymentTransactionSeatJdbcRepository transactionSeatJdbcRepository) {
        this.transactionJdbcRepository = transactionJdbcRepository;
        this.transactionSeatJdbcRepository = transactionSeatJdbcRepository;
    }

    @Override
    public PaymentTransaction save(PaymentTransaction transaction) {
        PaymentTransactionEntity entity = new PaymentTransactionEntity(
                transaction.getId(),
                transaction.getBookingId(),
                transaction.getFlightId(),
                transaction.getAmountCents(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                transaction.getExpiresAt()
        );
        transactionJdbcRepository.save(entity);
        transactionSeatJdbcRepository.deleteByTransactionId(transaction.getId());
        for (String seatId : transaction.getSeatIds()) {
            transactionSeatJdbcRepository.save(new PaymentTransactionSeatEntity(null, transaction.getId(), seatId));
        }
        return transaction;
    }

    @Override
    public Optional<PaymentTransaction> findById(String id) {
        Optional<PaymentTransactionEntity> entity = transactionJdbcRepository.findById(id);
        if (entity.isEmpty()) {
            return Optional.empty();
        }
        List<String> seatIds = transactionSeatJdbcRepository.findByTransactionId(id).stream()
                .map(PaymentTransactionSeatEntity::getSeatId)
                .collect(Collectors.toList());
        PaymentTransactionEntity transactionEntity = entity.get();
        return Optional.of(new PaymentTransaction(
                transactionEntity.getId(),
                transactionEntity.getBookingId(),
                transactionEntity.getFlightId(),
                seatIds,
                transactionEntity.getAmountCents(),
                transactionEntity.getStatus(),
                transactionEntity.getCreatedAt(),
                transactionEntity.getExpiresAt()
        ));
    }

    @Override
    public List<PaymentTransaction> findExpiredPending(Instant cutoff) {
        return transactionJdbcRepository.findExpired(PaymentStatus.PENDING, cutoff).stream()
                .map(entity -> {
                    List<String> seatIds = transactionSeatJdbcRepository.findByTransactionId(entity.getId()).stream()
                            .map(PaymentTransactionSeatEntity::getSeatId)
                            .collect(Collectors.toList());
                    return new PaymentTransaction(
                            entity.getId(),
                            entity.getBookingId(),
                            entity.getFlightId(),
                            seatIds,
                            entity.getAmountCents(),
                            entity.getStatus(),
                            entity.getCreatedAt(),
                            entity.getExpiresAt()
                    );
                })
                .collect(Collectors.toList());
    }
}
