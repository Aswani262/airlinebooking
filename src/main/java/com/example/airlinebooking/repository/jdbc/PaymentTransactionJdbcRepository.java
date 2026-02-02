package com.example.airlinebooking.repository.jdbc;

import com.example.airlinebooking.domain.PaymentStatus;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data JDBC repository for payment transactions.
 */
public interface PaymentTransactionJdbcRepository extends CrudRepository<PaymentTransactionEntity, String> {
    @Query("SELECT * FROM payment_transactions WHERE status = :status AND expires_at <= :cutoff")
    List<PaymentTransactionEntity> findExpired(PaymentStatus status, Instant cutoff);
}
