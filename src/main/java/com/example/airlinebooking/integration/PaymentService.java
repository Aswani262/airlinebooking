package com.example.airlinebooking.integration;

/**
 * Abstracts payment system calls so booking flows can refund or charge adjustments without vendor coupling.
 */
public interface PaymentService {
    PaymentTransaction refund(String bookingId, int amountCents, String reason);

    PaymentTransaction collectChangeFee(String bookingId, int amountCents, String reason);
}
