package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.PaymentTransaction;
import com.example.airlinebooking.repository.jdbc.PaymentTransactionEntity;
import com.example.airlinebooking.repository.jdbc.PaymentTransactionJdbcRepository;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JDBC-backed payment transaction repository for persistent payment tracking.
 */
@Repository
public class JdbcPaymentTransactionRepository implements PaymentTransactionRepository {
    private final PaymentTransactionJdbcRepository transactionJdbcRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public JdbcPaymentTransactionRepository(PaymentTransactionJdbcRepository transactionJdbcRepository, JdbcAggregateTemplate jdbcAggregateTemplate
    ) {
        this.transactionJdbcRepository = transactionJdbcRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
    }

    @Override
    public PaymentTransaction update(PaymentTransaction transaction) {
        PaymentTransactionEntity entity = new PaymentTransactionEntity(
                transaction.getId(),
                transaction.getBookingId(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                transaction.getUpdatesAt(),
                transaction.getPaymentProviderTransactionId(),
                transaction.getPaymentProviderResponse()


        );
        transactionJdbcRepository.save(entity);
        return transaction;
    }

    @Override
    public PaymentTransaction insert(PaymentTransaction transaction) {
        PaymentTransactionEntity entity = new PaymentTransactionEntity(
                transaction.getId(),
                transaction.getBookingId(),
                transaction.getAmount(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                transaction.getUpdatesAt(),
                transaction.getPaymentProviderTransactionId(),
                transaction.getPaymentProviderResponse()


        );
        jdbcAggregateTemplate.insert(entity);
        return transaction;
    }

    @Override
    public Optional<PaymentTransaction> findById(String id) {
        Optional<PaymentTransactionEntity> entity = transactionJdbcRepository.findById(id);
        if (entity.isEmpty()) {
            return Optional.empty();
        }
        PaymentTransactionEntity transactionEntity = entity.get();
        return Optional.of(new PaymentTransaction(
                transactionEntity.getId(),
                transactionEntity.getBookingId(),
                transactionEntity.getAmount(),
                transactionEntity.getStatus(),
                transactionEntity.getCreatedAt(),
                transactionEntity.getUpdatesAt(),
                transactionEntity.getPaymentProviderTransactionId(),
                transactionEntity.getPaymentProviderResponse()
        ));
    }


}
