package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.Booking;
import com.example.airlinebooking.domain.Passenger;
import com.example.airlinebooking.domain.PaymentTransaction;

import java.util.List;

/**
 * Orchestrates booking lifecycle steps across seat locks and payments.
 */
public interface BookingProcessManager {
    PaymentTransaction startBooking(String flightId, Passenger passenger, List<String> seatIds, int amountCents);

    Booking handlePaymentSuccess(String transactionId);

    void handlePaymentFailure(String transactionId);

    int releaseExpiredPayments();
}
