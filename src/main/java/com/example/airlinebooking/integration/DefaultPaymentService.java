package com.example.airlinebooking.integration;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Default payment integration stub that simulates interactions with a payment processor.
 */
@Service
public class DefaultPaymentService implements PaymentService {
    @Override
    public PaymentTransaction refund(String bookingId, int amountCents, String reason) {
        return new PaymentTransaction(UUID.randomUUID().toString(), "REFUNDED", amountCents, reason);
    }

    @Override
    public PaymentTransaction collectChangeFee(String bookingId, int amountCents, String reason) {
        return new PaymentTransaction(UUID.randomUUID().toString(), "CHARGED", amountCents, reason);
    }
}
