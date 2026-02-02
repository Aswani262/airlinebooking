package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.PaymentTransaction;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for payment transactions so booking flow can update status independently of payment integration.
 */
public interface PaymentTransactionRepository {
    PaymentTransaction save(PaymentTransaction transaction);

    Optional<PaymentTransaction> findById(String id);

    List<PaymentTransaction> findExpiredPending(Instant cutoff);
}
