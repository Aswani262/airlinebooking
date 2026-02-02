package com.example.airlinebooking.api.dto;

import lombok.Data;

@Data
public class PaymentGatewayWebhookResponse {
    private String paymentGatewayTransactionId;
    //The unique identifier for the payment transaction associated with the webhook event
    //provide by the payment gateway as metadata during payment and correlate with booking and payment transaction
    private String transactionId;
    private String bookingId;
    private String status;
    private String rawPayload;
}
