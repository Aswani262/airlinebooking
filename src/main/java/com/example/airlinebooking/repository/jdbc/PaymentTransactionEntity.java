package com.example.airlinebooking.repository.jdbc;

import com.example.airlinebooking.domain.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * Stores payment transactions for booking flow coordination.
 */
@Table("payment_transactions")
@Data
public class PaymentTransactionEntity {
    @Id
    private String id;
    private String bookingId;
    private double amount;
    private PaymentStatus status;
    private Instant createdAt;
    private Instant updatesAt;
    private String paymentProviderTransactionId;
    private String paymentProviderResponse;


    public PaymentTransactionEntity() {
    }

    public PaymentTransactionEntity(String id, String bookingId, double amount, PaymentStatus status,
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
