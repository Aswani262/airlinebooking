package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.BookingStatus;
import com.example.airlinebooking.domain.Flight;
import com.example.airlinebooking.domain.Passenger;
import com.example.airlinebooking.domain.SeatLock;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.domain.Aircraft;
import com.example.airlinebooking.integration.AirlineItService;
import com.example.airlinebooking.integration.PaymentService;
import com.example.airlinebooking.integration.PaymentTransaction;
import com.example.airlinebooking.repository.BookingRepository;
import com.example.airlinebooking.repository.FlightRepository;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingServiceTest {
    @Test
    void booksAndCancelsSeats() {
        FlightRepository flightRepository = mock(FlightRepository.class);
        BookingRepository bookingRepository = mock(BookingRepository.class);
        SeatLockService seatLockService = mock(SeatLockService.class);
        SeatJdbcRepository seatJdbcRepository = mock(SeatJdbcRepository.class);
        PaymentService paymentService = mock(PaymentService.class);
        AirlineItService airlineItService = mock(AirlineItService.class);
        BookingService bookingService = new DefaultBookingService(bookingRepository, flightRepository, seatLockService, seatJdbcRepository,
                paymentService, airlineItService);

        Flight flight = new Flight("FL-100", "XY100", "JFK", "SFO", java.time.LocalDateTime.now(),
                new Aircraft("AC-1", "A320", Collections.emptyList()));
        when(flightRepository.findById("FL-100")).thenReturn(Optional.of(flight));

        Passenger passenger = new Passenger("P-1", "Alex Doe", "alex@example.com");
        SeatLock lock = new SeatLock("LOCK-1", "FL-100", List.of("AE1"), Instant.now().plusSeconds(900));
        when(seatLockService.lockSeats("FL-100", List.of("AE1"))).thenReturn(lock);
        when(seatJdbcRepository.updateStatusIfCurrent("FL-100", List.of("AE1"), SeatStatus.LOCKED, SeatStatus.BOOKED))
                .thenReturn(1);
        when(bookingRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var booking = bookingService.book("FL-100", passenger, List.of("AE1"));

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getSeatIds()).containsExactly("AE1");

        verify(seatLockService).finalizeLock(lock);

        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(seatJdbcRepository.updateStatus(booking.getFlightId(), booking.getSeatIds(), SeatStatus.AVAILABLE)).thenReturn(1);
        when(paymentService.refund(booking.getId(), 0, "Customer cancellation"))
                .thenReturn(new PaymentTransaction("REF-1", "REFUNDED", 0, "Customer cancellation"));

        var cancelled = bookingService.cancel(booking.getId());
        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.REFUNDED);
    }
}
