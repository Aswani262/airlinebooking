package com.example.airlinebooking.integration;

import com.example.airlinebooking.domain.PaymentTransaction;
import org.springframework.stereotype.Service;

/**
 * Default payment integration stub that would call a bank or payment gateway API.
 */
@Service
public class DefaultPaymentIntegrationService implements PaymentIntegrationService {
    @Override
    public void requestPayment(PaymentTransaction transaction) {
        // Placeholder for third-party payment API call.
    }
}
