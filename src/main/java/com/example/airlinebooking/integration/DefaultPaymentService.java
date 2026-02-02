package com.example.airlinebooking.integration;

import com.example.airlinebooking.domain.PaymentStatus;
import com.example.airlinebooking.domain.PaymentTransaction;
import com.example.airlinebooking.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Default payment integration stub that simulates interactions with a payment processor.
 */
@Service
public class DefaultPaymentService implements PaymentService {
    private static final Duration PAYMENT_EXPIRY = Duration.ofMinutes(10);

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PaymentIntegrationService paymentIntegrationService;

    public DefaultPaymentService(PaymentTransactionRepository paymentTransactionRepository,
                                 PaymentIntegrationService paymentIntegrationService) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.paymentIntegrationService = paymentIntegrationService;
    }

    @Override
    public PaymentTransaction createTransaction(String bookingId, double amountCents,
                                                String reason) {
        PaymentTransaction transaction = new PaymentTransaction(
                UUID.randomUUID().toString(),
                bookingId,
                amountCents,
                PaymentStatus.PENDING,
                Instant.now(),
                Instant.now().plus(PAYMENT_EXPIRY),null,null
        );
        paymentTransactionRepository.insert(transaction);
        paymentIntegrationService.requestPayment(transaction);
        return transaction;
    }

    @Override
    public PaymentTransaction collectChangeFee(String bookingId, double amount, String reason) {
        PaymentTransaction transaction = new PaymentTransaction(UUID.randomUUID().toString(), bookingId,
                amount, PaymentStatus.SUCCEEDED, Instant.now(), Instant.now().plus(PAYMENT_EXPIRY),null,null);
        paymentTransactionRepository.update(transaction);
        return transaction;
    }

    @Override
    public void initateRefund(String bookingId, double amount, String customerCancellation) {
        String transactionId =   UUID.randomUUID().toString();
        //Call the payment integration service to process the refund
        //This will interact with the payment gateway to initiate the refund
        // Will get payment gateway transaction id and response in real implementation
        paymentIntegrationService.processRefund(bookingId, amount, customerCancellation,transactionId);

        //Set the payment transaction status to REFUND_IN_PROGRESS
        //

        PaymentTransaction transaction = new PaymentTransaction(UUID.randomUUID().toString(), bookingId,
                amount, PaymentStatus.REFUND_IN_PROGRESS, Instant.now(), Instant.now(),null,null);
        paymentTransactionRepository.update(transaction);

    }
}
