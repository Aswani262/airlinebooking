package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.BookingStatus;
import com.example.airlinebooking.domain.Passenger;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.repository.BookingRepository;
import com.example.airlinebooking.repository.FlightRepository;
import com.example.airlinebooking.repository.SeatLockRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookingServiceTest {
    @Test
    void booksAndCancelsSeats() {
        FlightRepository flightRepository = new FlightRepository();
        BookingRepository bookingRepository = new BookingRepository();
        SeatLockRepository seatLockRepository = new SeatLockRepository();
        SeatLockService seatLockService = new SeatLockService(flightRepository, seatLockRepository);
        BookingService bookingService = new BookingService(bookingRepository, flightRepository, seatLockService);

        Passenger passenger = new Passenger("P-1", "Alex Doe", "alex@example.com");
        var booking = bookingService.book("FL-100", passenger, List.of("AE1"));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getSeatIds()).containsExactly("AE1");

        var flight = flightRepository.findById("FL-100").orElseThrow();
        var seatStatus = flight.getAircraft().seats().stream()
                .filter(seat -> seat.getId().equals("AE1"))
                .findFirst()
                .orElseThrow()
                .getStatus();
        assertThat(seatStatus).isEqualTo(SeatStatus.BOOKED);

        var cancelled = bookingService.cancel(booking.getId());
        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);

        var statusAfterCancel = flight.getAircraft().seats().stream()
                .filter(seat -> seat.getId().equals("AE1"))
                .findFirst()
                .orElseThrow()
                .getStatus();
        assertThat(statusAfterCancel).isEqualTo(SeatStatus.AVAILABLE);
    }
}
