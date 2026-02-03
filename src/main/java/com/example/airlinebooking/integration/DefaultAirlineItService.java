package com.example.airlinebooking.integration;

import com.example.airlinebooking.domain.Booking;
import org.springframework.stereotype.Service;

/**
 * Default airline IT integration stub that would push ticketing and schedule changes to core systems.
 */
@Service
public class DefaultAirlineItService implements AirlineItService {
    @Override
    public void issueTicket(Booking booking) {
        // Placeholder for ticket issuance call.
    }

    @Override
    public void notifyCancellation(Booking booking) {
        // Placeholder for cancellation sync call.
    }

    @Override
    public void notifyReschedule( Booking newBooking) {
        // Placeholder for reschedule sync call.
    }
}
