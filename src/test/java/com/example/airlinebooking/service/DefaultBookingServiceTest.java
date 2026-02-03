package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.Booking;
import com.example.airlinebooking.domain.BookingStatus;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.integration.AirlineItService;
import com.example.airlinebooking.integration.PaymentService;
import com.example.airlinebooking.repository.BookingRepository;
import com.example.airlinebooking.repository.FlightRepository;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultBookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private FlightRepository flightRepository;
    @Mock private SeatLockService seatLockService;
    @Mock private SeatJdbcRepository seatJdbcRepository;
    @Mock private PaymentService paymentService;
    @Mock private AirlineItService airlineItService;

    @InjectMocks
    private DefaultBookingService bookingService;

    @Test
    void cancel_shouldThrowIfBookingNotFound() {
        when(bookingRepository.findById("B-404")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.cancel("B-404"));

        assertEquals("Booking not found", ex.getMessage());

        verify(bookingRepository).findById("B-404");
        verifyNoMoreInteractions(bookingRepository);
        verifyNoInteractions(flightRepository, seatJdbcRepository, seatLockService, paymentService, airlineItService);
    }

    @Test
    void cancel_shouldReturnBookingAsIs_whenAlreadyCancelled() {
        Booking booking = new Booking("B-1", "FL-1", List.of("S1"), BookingStatus.CANCELLED, 1000.0, Instant.now());
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(booking));

        Booking result = bookingService.cancel("B-1");

        assertSame(booking, result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());

        verify(bookingRepository).findById("B-1");
        verifyNoInteractions(flightRepository, seatJdbcRepository, seatLockService, paymentService, airlineItService);
    }

    @Test
    void cancel_shouldReturnBookingAsIs_whenExpired() {
        Booking booking = new Booking("B-1", "FL-1", List.of("S1"), BookingStatus.EXPIRED, 1000.0, Instant.now());
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(booking));

        Booking result = bookingService.cancel("B-1");

        assertSame(booking, result);
        assertEquals(BookingStatus.EXPIRED, result.getStatus());

        verify(bookingRepository).findById("B-1");
        verifyNoInteractions(flightRepository, seatJdbcRepository, seatLockService, paymentService, airlineItService);
    }

    @Test
    void cancel_shouldReturnBookingAsIs_whenFailed() {
        Booking booking = new Booking("B-1", "FL-1", List.of("S1"), BookingStatus.FAILED, 1000.0, Instant.now());
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(booking));

        Booking result = bookingService.cancel("B-1");

        assertSame(booking, result);
        assertEquals(BookingStatus.FAILED, result.getStatus());

        verify(bookingRepository).findById("B-1");
        verifyNoInteractions(flightRepository, seatJdbcRepository, seatLockService, paymentService, airlineItService);
    }

    @Test
    void cancel_shouldThrowIfFlightNotFound() {
        Booking booking = new Booking("B-1", "FL-404", List.of("S1"), BookingStatus.CONFIRMED, 1000.0, Instant.now());
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(booking));
        when(flightRepository.findById("FL-404")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.cancel("B-1"));

        assertEquals("Flight not found", ex.getMessage());

        verify(bookingRepository).findById("B-1");
        verify(flightRepository).findById("FL-404");

        verifyNoInteractions(seatJdbcRepository, seatLockService, paymentService, airlineItService);
        verify(bookingRepository, never()).update(any(), anyMap());
    }

    @Test
    void cancel_shouldCancelBookingReleaseSeatsReleaseLockInitiateRefundAndNotifyIT() {
        Booking booking = new Booking("B-1", "FL-1", List.of("S1", "S2"), BookingStatus.CONFIRMED, 1500.0, Instant.now());

        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(booking));
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(mock(com.example.airlinebooking.domain.Flight.class)));

        Booking result = bookingService.cancel("B-1");

        assertSame(booking, result);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());

        verify(seatJdbcRepository).updateStatus("FL-1", List.of("S1", "S2"), SeatStatus.AVAILABLE);

        verify(bookingRepository).update(eq(booking), eq(Collections.emptyMap()));

        verify(seatLockService).releaseLock("FL-1");

        verify(paymentService).initateRefund("B-1", 1500.0, "Customer cancellation");

        verify(airlineItService).notifyCancellation(booking);
    }

    @Test
    void cancel_shouldNotUpdateOrRefundIfBookingAlreadyCancelledExpiredOrFailed() {
        Booking booking = new Booking("B-1", "FL-1", List.of("S1"), BookingStatus.CANCELLED, 1000.0, Instant.now());
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(booking));

        bookingService.cancel("B-1");

        verifyNoInteractions(flightRepository, seatJdbcRepository, seatLockService, paymentService, airlineItService);
        verify(bookingRepository, never()).update(any(), anyMap());
    }

    @Test
    void cancel_happyPath_verifyOrderOfImportantSteps() {
        Booking booking = new Booking("B-1", "FL-1", List.of("S1"), BookingStatus.CONFIRMED, 1000.0, Instant.now());
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(booking));
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(mock(com.example.airlinebooking.domain.Flight.class)));

        bookingService.cancel("B-1");

        InOrder inOrder = inOrder(seatJdbcRepository, bookingRepository, seatLockService, paymentService, airlineItService);

        inOrder.verify(seatJdbcRepository).updateStatus("FL-1", List.of("S1"), SeatStatus.AVAILABLE);
        inOrder.verify(bookingRepository).update(eq(booking), eq(Collections.emptyMap()));
        inOrder.verify(seatLockService).releaseLock("FL-1");
        inOrder.verify(paymentService).initateRefund("B-1", 1000.0, "Customer cancellation");
        inOrder.verify(airlineItService).notifyCancellation(booking);
    }
}
