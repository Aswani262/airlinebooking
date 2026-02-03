package com.example.airlinebooking.integration;

import com.example.airlinebooking.domain.Booking;

/**
 * Abstracts airline IT system coordination for ticket issuance and schedule changes.
 */
public interface AirlineItService {
    void issueTicket(Booking booking);

    void notifyCancellation(Booking booking);

    void notifyReschedule(Booking newBooking);
}
