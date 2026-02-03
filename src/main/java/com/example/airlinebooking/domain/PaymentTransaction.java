package com.example.airlinebooking.domain;

import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * Payment transaction aggregate used by the booking process manager to track payment progress.
 */
@Data
public class PaymentTransaction {
    private  String id;
    private  String bookingId;
    private  double amount;
    private  PaymentStatus status;
    private  Instant createdAt;
    private Instant updatesAt;
    private String paymentProviderTransactionId;
    private String paymentProviderResponse;


    public PaymentTransaction() {
    }

    public PaymentTransaction(String id, String bookingId, double amount, PaymentStatus status,
                              Instant createdAt, Instant updatesAt, String paymentProviderTransactionId,
                              String paymentProviderResponse) {
        this.id = id;
        this.bookingId = bookingId;
        this.amount = amount;
        this.status = status;
        this.createdAt = createdAt;
        this.updatesAt = updatesAt;
        this.paymentProviderTransactionId = paymentProviderTransactionId;
        this.paymentProviderResponse = paymentProviderResponse;
    }

}
