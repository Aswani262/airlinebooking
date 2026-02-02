package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.Booking;
import com.example.airlinebooking.domain.Passenger;
import com.example.airlinebooking.domain.PaymentTransaction;

import java.util.List;
import java.util.Map;

/**
 * Orchestrates booking lifecycle steps across seat locks and payments.
 */
public interface BookingProcessManager {
    PaymentTransaction startBooking(String flightId, double amount, Map<String,String> passengerSeatMap);

    Booking handlePaymentSuccess(String transactionId);

    void handlePaymentFailure(String transactionId);

    void handlePayment(String bookingId, String paymentGatewayTransactionId, String transactionId, String rawPayload, String status);
}
