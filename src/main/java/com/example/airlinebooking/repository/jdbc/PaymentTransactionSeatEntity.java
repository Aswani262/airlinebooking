package com.example.airlinebooking.repository.jdbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Join table for seats held within a payment transaction window.
 */
@Table("payment_transaction_seats")
public class PaymentTransactionSeatEntity {
    @Id
    private Long id;
    private String transactionId;
    private String seatId;

    public PaymentTransactionSeatEntity() {
    }

    public PaymentTransactionSeatEntity(Long id, String transactionId, String seatId) {
        this.id = id;
        this.transactionId = transactionId;
        this.seatId = seatId;
    }

    public Long getId() {
        return id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getSeatId() {
        return seatId;
    }
}
