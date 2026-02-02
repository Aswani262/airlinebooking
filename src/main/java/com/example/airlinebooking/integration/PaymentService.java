package com.example.airlinebooking.integration;

/**
 * Abstracts payment system calls so booking flows can refund or charge adjustments without vendor coupling.
 */
import com.example.airlinebooking.domain.PaymentTransaction;

import java.util.List;

public interface PaymentService {
    PaymentTransaction createTransaction(String bookingId, String flightId, List<String> seatIds, int amountCents, String reason);

    PaymentTransaction refund(String bookingId, String flightId, int amountCents, String reason);

    PaymentTransaction collectChangeFee(String bookingId, String flightId, int amountCents, String reason);
}
