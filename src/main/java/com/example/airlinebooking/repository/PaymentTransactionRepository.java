package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.PaymentTransaction;

import java.util.Optional;

/**
 * Repository for payment transactions so booking flow can update status independently of payment integration.
 */
public interface PaymentTransactionRepository {
    PaymentTransaction update(PaymentTransaction transaction);

    PaymentTransaction insert(PaymentTransaction transaction);

    Optional<PaymentTransaction> findById(String id);

}
