package com.example.airlinebooking.repository.jdbc;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Accesses payment transaction seat mappings for cleanup and status updates.
 */
public interface PaymentTransactionSeatJdbcRepository extends CrudRepository<PaymentTransactionSeatEntity, Long> {
    @Query("SELECT * FROM payment_transaction_seats WHERE transaction_id = :transactionId")
    List<PaymentTransactionSeatEntity> findByTransactionId(String transactionId);

    @Modifying
    @Query("DELETE FROM payment_transaction_seats WHERE transaction_id = :transactionId")
    void deleteByTransactionId(String transactionId);
}
