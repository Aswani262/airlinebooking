package com.example.airlinebooking.integration;

/**
 * Abstracts payment system calls so booking flows can refund or charge adjustments without vendor coupling.
 */
import com.example.airlinebooking.domain.PaymentTransaction;

import java.util.List;

public interface PaymentService {
    PaymentTransaction createTransaction(String bookingId, double amountCents, String reason);

    PaymentTransaction collectChangeFee(String bookingId, double amount, String reason);

    void initateRefund(String bookingId, double amount, String customerCancellation);
}
