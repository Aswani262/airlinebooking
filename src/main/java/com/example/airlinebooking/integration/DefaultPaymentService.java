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
    public PaymentTransaction createTransaction(String bookingId, String flightId, java.util.List<String> seatIds, int amountCents,
                                                String reason) {
        PaymentTransaction transaction = new PaymentTransaction(
                UUID.randomUUID().toString(),
                bookingId,
                flightId,
                seatIds,
                amountCents,
                PaymentStatus.PENDING,
                Instant.now(),
                Instant.now().plus(PAYMENT_EXPIRY)
        );
        paymentTransactionRepository.save(transaction);
        paymentIntegrationService.requestPayment(transaction);
        return transaction;
    }

    @Override
    public PaymentTransaction refund(String bookingId, String flightId, int amountCents, String reason) {
        PaymentTransaction transaction = new PaymentTransaction(UUID.randomUUID().toString(), bookingId, flightId, java.util.List.of(),
                amountCents, PaymentStatus.SUCCEEDED, Instant.now(), Instant.now().plus(PAYMENT_EXPIRY));
        paymentTransactionRepository.save(transaction);
        return transaction;
    }

    @Override
    public PaymentTransaction collectChangeFee(String bookingId, String flightId, int amountCents, String reason) {
        PaymentTransaction transaction = new PaymentTransaction(UUID.randomUUID().toString(), bookingId, flightId, java.util.List.of(),
                amountCents, PaymentStatus.SUCCEEDED, Instant.now(), Instant.now().plus(PAYMENT_EXPIRY));
        paymentTransactionRepository.save(transaction);
        return transaction;
    }
}
