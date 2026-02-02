package com.example.airlinebooking.integration;

import com.example.airlinebooking.domain.PaymentTransaction;

/**
 * Integrates with external payment providers to execute charges or refunds.
 */
public interface PaymentIntegrationService {
    void requestPayment(PaymentTransaction transaction);
}
