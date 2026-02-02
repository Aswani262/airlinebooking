package com.example.airlinebooking.integration;

/**
 * Represents a payment system response for refunds or change fees.
 */
public record PaymentTransaction(String id, String status, int amountCents, String reason) {
}
