package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.*;
import com.example.airlinebooking.integration.AirlineItService;
import com.example.airlinebooking.integration.PaymentService;
import com.example.airlinebooking.repository.BookingRepository;
import com.example.airlinebooking.repository.FlightRepository;
import com.example.airlinebooking.repository.PaymentTransactionRepository;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultBookingProcessManagerTest {

    @Mock private SeatLockService seatLockService;
    @Mock private PaymentService paymentService;
    @Mock private PaymentTransactionRepository paymentTransactionRepository;
    @Mock private SeatJdbcRepository seatJdbcRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private FlightRepository flightRepository;
    @Mock private AirlineItService airlineItService;

    @InjectMocks
    private DefaultBookingProcessManager manager;

    @BeforeEach
    void setUp() {
        // @InjectMocks will wire mocks automatically.
    }

    // -----------------------
    // startBooking tests
    // -----------------------

    @Test
    void startBooking_shouldLockSeatsCreateBookingAndCreatePaymentTransaction() {
        String flightId = "FL-1";
        double amount = 5000.0;

        Map<String, String> passengerSeatMap = new LinkedHashMap<>();
        passengerSeatMap.put("P1", "S1");
        passengerSeatMap.put("P2", "S2");

        List<String> seatIds = new ArrayList<>(passengerSeatMap.keySet().stream().toList()); // your code uses keySet

        when(flightRepository.findById(flightId)).thenReturn(Optional.of(mock(Flight.class)));
        when(seatLockService.lockSeats(eq(flightId), anyList()))
                .thenReturn(new SeatLock("LOCK-1", flightId, seatIds, Instant.now().plusSeconds(600)));

        PaymentTransaction tx = new PaymentTransaction();
        tx.setId("TX-1");
        tx.setBookingId("B-1");
        tx.setStatus(PaymentStatus.PENDING);

        when(paymentService.createTransaction(anyString(), eq(amount), anyString())).thenReturn(tx);

        PaymentTransaction result = manager.startBooking(flightId, amount, passengerSeatMap);

        assertNotNull(result);
        assertEquals("TX-1", result.getId());

        // verify lock called
        verify(seatLockService).lockSeats(eq(flightId), anyList());

        // verify booking insert called
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).insert(bookingCaptor.capture(), eq(passengerSeatMap));

        Booking savedBooking = bookingCaptor.getValue();
        assertNotNull(savedBooking.getId());
        assertEquals(flightId, savedBooking.getFlightId());
        assertEquals(BookingStatus.PENDING, savedBooking.getStatus());
        assertEquals(amount, savedBooking.getAmount());
        assertNotNull(savedBooking.getCreatedAt());

        // verify payment created with booking id
        verify(paymentService).createTransaction(eq(savedBooking.getId()), eq(amount), eq("Seat looked"));

        verifyNoMoreInteractions(airlineItService);
    }

    @Test
    void startBooking_shouldThrowIfFlightNotFound() {
        when(flightRepository.findById("FL-X")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> manager.startBooking("FL-X", 100, Map.of("P1", "S1")));

        assertEquals("Flight not found", ex.getMessage());

        verify(flightRepository).findById("FL-X");
        verifyNoInteractions(seatLockService, bookingRepository, paymentService);
    }

    // -----------------------
    // handlePaymentSuccess tests
    // -----------------------

    @Test
    void handlePaymentSuccess_shouldConfirmBookingUpdateSeatsReleaseLockIssueTicket() {
        String transactionId = "TX-1";
        String bookingId = "B-1";
        String flightId = "FL-1";

        PaymentTransaction tx = new PaymentTransaction();
        tx.setId(transactionId);
        tx.setBookingId(bookingId);
        tx.setStatus(PaymentStatus.PENDING);

        Booking booking = new Booking(
                bookingId,
                flightId,
                List.of("S1", "S2"),
                BookingStatus.PENDING,
                5000.0,
                Instant.now()
        );

        when(paymentTransactionRepository.findById(transactionId)).thenReturn(Optional.of(tx));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        Booking result = manager.handlePaymentSuccess(transactionId);

        assertNotNull(result);
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
        assertEquals(PaymentStatus.SUCCEEDED, tx.getStatus());

        verify(bookingRepository).update(eq(booking), eq(Collections.emptyMap()));

        verify(seatJdbcRepository).updateStatusIfCurrent(
                eq(flightId),
                eq(List.of("S1", "S2")),
                eq(SeatStatus.LOCKED),
                eq(SeatStatus.BOOKED)
        );

        verify(paymentTransactionRepository).update(eq(tx));
        verify(seatLockService).releaseLock(eq(flightId));
        verify(airlineItService).issueTicket(eq(booking));
    }

    @Test
    void handlePaymentSuccess_shouldThrowIfTransactionNotFound() {
        when(paymentTransactionRepository.findById("TX-X")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> manager.handlePaymentSuccess("TX-X"));

        assertEquals("Payment transaction not found", ex.getMessage());
        verify(paymentTransactionRepository).findById("TX-X");
        verifyNoMoreInteractions(paymentTransactionRepository);
        verifyNoInteractions(bookingRepository, seatJdbcRepository, seatLockService, airlineItService);
    }

    @Test
    void handlePaymentSuccess_shouldThrowIfPaymentNotPending() {
        PaymentTransaction tx = new PaymentTransaction();
        tx.setId("TX-1");
        tx.setBookingId("B-1");
        tx.setStatus(PaymentStatus.FAILED); // not pending

        when(paymentTransactionRepository.findById("TX-1")).thenReturn(Optional.of(tx));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> manager.handlePaymentSuccess("TX-1"));

        assertEquals("Payment is not pending", ex.getMessage());

        verify(paymentTransactionRepository).findById("TX-1");
        verifyNoInteractions(bookingRepository, seatJdbcRepository, seatLockService, airlineItService);
    }

    @Test
    void handlePaymentSuccess_refundBranchIsUnreachableWithCurrentGuard() {
        // This test documents the current behavior:
        // if tx is REFUND_IN_PROGRESS, the method throws before reaching refund branch.
        PaymentTransaction tx = new PaymentTransaction();
        tx.setId("TX-1");
        tx.setBookingId("B-1");
        tx.setStatus(PaymentStatus.REFUND_IN_PROGRESS);

        when(paymentTransactionRepository.findById("TX-1")).thenReturn(Optional.of(tx));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> manager.handlePaymentSuccess("TX-1"));

        assertEquals("Payment is not pending", ex.getMessage());
    }

    // -----------------------
    // handlePaymentFailure tests
    // -----------------------

    @Test
    void handlePaymentFailure_shouldMarkBookingFailedReleaseLockAndMakeSeatsAvailable() {
        String transactionId = "TX-1";
        String bookingId = "B-1";
        String flightId = "FL-1";

        PaymentTransaction tx = new PaymentTransaction();
        tx.setId(transactionId);
        tx.setBookingId(bookingId);
        tx.setStatus(PaymentStatus.PENDING);

        Booking booking = new Booking(
                bookingId,
                flightId,
                List.of("S1", "S2"),
                BookingStatus.PENDING,
                5000.0,
                Instant.now()
        );

        when(paymentTransactionRepository.findById(transactionId)).thenReturn(Optional.of(tx));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        manager.handlePaymentFailure(transactionId);

        verify(seatJdbcRepository).updateStatusIfCurrent(
                eq(flightId),
                eq(List.of("S1", "S2")),
                eq(SeatStatus.LOCKED),
                eq(SeatStatus.AVAILABLE)
        );
        verify(seatLockService).releaseLock(eq(flightId));

        assertEquals(PaymentStatus.FAILED, tx.getStatus());
        assertEquals(BookingStatus.FAILED, booking.getStatus());

        verify(bookingRepository).update(eq(booking), eq(Collections.emptyMap()));
        verify(paymentTransactionRepository, times(1)).update(eq(tx)); // your code updates twice in else branch
        verifyNoInteractions(airlineItService);
    }

    @Test
    void handlePaymentFailure_shouldSetRefundFailedWhenBookingCancelledAndRefundInProgress() {
        String transactionId = "TX-1";
        String bookingId = "B-1";
        String flightId = "FL-1";

        PaymentTransaction tx = new PaymentTransaction();
        tx.setId(transactionId);
        tx.setBookingId(bookingId);
        tx.setStatus(PaymentStatus.REFUND_IN_PROGRESS);

        Booking booking = new Booking(
                bookingId,
                flightId,
                List.of("S1"),
                BookingStatus.CANCELLED,
                5000.0,
                Instant.now()
        );

        when(paymentTransactionRepository.findById(transactionId)).thenReturn(Optional.of(tx));
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        manager.handlePaymentFailure(transactionId);

        assertEquals(PaymentStatus.REFUND_FAILED, tx.getStatus());

        verify(paymentTransactionRepository, times(1)).update(eq(tx)); // once in if branch, once after if/else
        verify(bookingRepository, never()).update(any(), anyMap());    // refund failure doesn't change booking in your code
    }

    // -----------------------
    // handlePayment routing tests
    // -----------------------

    @Test
    void handlePayment_shouldRouteToSuccess() {
        DefaultBookingProcessManager spy = spy(manager);

        doReturn(mock(Booking.class)).when(spy).handlePaymentSuccess("TX-1");

        spy.handlePayment("B-1", "PG-1", "TX-1", "{}", "SUCCESS");

        verify(spy).handlePaymentSuccess("TX-1");
        verify(spy, never()).handlePaymentFailure(anyString());
    }

    @Test
    void handlePayment_shouldRouteToFailure() {
        DefaultBookingProcessManager spy = spy(manager);

        doNothing().when(spy).handlePaymentFailure("TX-1");

        spy.handlePayment("B-1", "PG-1", "TX-1", "{}", "FAILED");

        verify(spy).handlePaymentFailure("TX-1");
        verify(spy, never()).handlePaymentSuccess(anyString());
    }

// -----------------------
// cancel() tests
// -----------------------

    @Test
    void cancel_shouldThrowWhenBookingNotFound() {
        when(bookingRepository.findById("B-404")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> manager.cancel("B-404")
        );

        assertEquals("Booking not found", ex.getMessage());

        verify(bookingRepository).findById("B-404");
        verifyNoInteractions(flightRepository, seatJdbcRepository, seatLockService, paymentService, airlineItService);
    }

    @Test
    void cancel_shouldReturnBookingAsIs_whenAlreadyCancelled() {
        Booking booking = new Booking("B-1", "FL-1", List.of("S1"), BookingStatus.CANCELLED, 1000.0, Instant.now());
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(booking));

        Booking result = manager.cancel("B-1");

        assertSame(booking, result);
        assertEquals(BookingStatus.CANCELLED, result.getStatus());

        verify(bookingRepository).findById("B-1");
        verifyNoInteractions(flightRepository, seatJdbcRepository, seatLockService, paymentService, airlineItService);
        verify(bookingRepository, never()).update(any(), anyMap());
    }

    @Test
    void cancel_shouldReturnBookingAsIs_whenExpired() {
        Booking booking = new Booking("B-1", "FL-1", List.of("S1"), BookingStatus.EXPIRED, 1000.0, Instant.now());
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(booking));

        Booking result = manager.cancel("B-1");

        assertSame(booking, result);
        assertEquals(BookingStatus.EXPIRED, result.getStatus());

        verify(bookingRepository).findById("B-1");
        verifyNoInteractions(flightRepository, seatJdbcRepository, seatLockService, paymentService, airlineItService);
        verify(bookingRepository, never()).update(any(), anyMap());
    }

    @Test
    void cancel_shouldReturnBookingAsIs_whenFailed() {
        Booking booking = new Booking("B-1", "FL-1", List.of("S1"), BookingStatus.FAILED, 1000.0, Instant.now());
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(booking));

        Booking result = manager.cancel("B-1");

        assertSame(booking, result);
        assertEquals(BookingStatus.FAILED, result.getStatus());

        verify(bookingRepository).findById("B-1");
        verifyNoInteractions(flightRepository, seatJdbcRepository, seatLockService, paymentService, airlineItService);
        verify(bookingRepository, never()).update(any(), anyMap());
    }

    @Test
    void cancel_shouldThrowWhenFlightNotFound() {
        Booking booking = new Booking("B-1", "FL-404", List.of("S1"), BookingStatus.CONFIRMED, 1000.0, Instant.now());
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(booking));
        when(flightRepository.findById("FL-404")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> manager.cancel("B-1")
        );

        assertEquals("Flight not found", ex.getMessage());

        verify(bookingRepository).findById("B-1");
        verify(flightRepository).findById("FL-404");

        verifyNoInteractions(seatJdbcRepository, seatLockService, paymentService, airlineItService);
        verify(bookingRepository, never()).update(any(), anyMap());
    }

    @Test
    void cancel_shouldCancelBooking_releaseSeats_releaseLock_refund_notifyCancellation() {
        Booking booking = new Booking("B-1", "FL-1", List.of("S1", "S2"), BookingStatus.CONFIRMED, 1500.0, Instant.now());

        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(booking));
        when(flightRepository.findById("FL-1")).thenReturn(Optional.of(mock(Flight.class)));

        Booking result = manager.cancel("B-1");

        assertSame(booking, result);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());

        verify(seatJdbcRepository).updateStatus("FL-1", List.of("S1", "S2"), SeatStatus.AVAILABLE);
        verify(bookingRepository).update(eq(booking), eq(Collections.emptyMap()));
        verify(seatLockService).releaseLock("FL-1");
        verify(paymentService).initateRefund("B-1", 1500.0, "Customer cancellation");
        verify(airlineItService).notifyCancellation(booking);
    }


    // -----------------------
// reschedule() tests
// -----------------------

    @Test
    void reschedule_shouldThrowWhenBookingNotFound() {
        when(bookingRepository.findById("B-404")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> manager.reschedule("B-404", "FL-NEW", Map.of("S9", "P1"), 2000.0)
        );

        assertEquals("Booking not found", ex.getMessage());

        verify(bookingRepository).findById("B-404");
        verifyNoInteractions(flightRepository, seatLockService, seatJdbcRepository, paymentService, airlineItService);
    }

    @Test
    void reschedule_shouldThrowWhenBookingNotConfirmed() {
        Booking booking = new Booking("B-1", "FL-OLD", List.of("S1"), BookingStatus.PENDING, 1000.0, Instant.now());
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(booking));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> manager.reschedule("B-1", "FL-NEW", Map.of("S9", "P1"), 2000.0)
        );

        assertEquals("Only confirmed bookings can be rescheduled", ex.getMessage());

        verify(bookingRepository).findById("B-1");
        verifyNoInteractions(flightRepository, seatLockService, seatJdbcRepository, paymentService, airlineItService);
    }

    @Test
    void reschedule_shouldThrowWhenOldFlightNotFound() {
        Booking booking = new Booking("B-1", "FL-OLD", List.of("S1"), BookingStatus.CONFIRMED, 1000.0, Instant.now());
        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(booking));
        when(flightRepository.findById("FL-OLD")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> manager.reschedule("B-1", "FL-NEW", Map.of("S9", "P1"), 2000.0)
        );

        assertEquals("Flight not found", ex.getMessage());

        verify(flightRepository).findById("FL-OLD");
        verifyNoInteractions(seatLockService, seatJdbcRepository, paymentService, airlineItService);
    }

    @Test
    void reschedule_shouldLockNewSeats_releaseOldSeats_releaseOldLock_setPending_update_collectFee_notify() {
        Booking existingBooking = new Booking("B-1", "FL-OLD", List.of("S1", "S2"), BookingStatus.CONFIRMED, 1000.0, Instant.now());

        Map<String, String> passengersSeatMap = new LinkedHashMap<>();
        passengersSeatMap.put("S9", "P1");
        passengersSeatMap.put("S10", "P2");

        List<String> newSeatIds = passengersSeatMap.keySet().stream().toList();

        when(bookingRepository.findById("B-1")).thenReturn(Optional.of(existingBooking));
        when(flightRepository.findById("FL-OLD")).thenReturn(Optional.of(mock(Flight.class)));
        when(seatLockService.lockSeats("FL-NEW", newSeatIds))
                .thenReturn(new SeatLock("LOCK-NEW", "FL-NEW", newSeatIds, Instant.now().plusSeconds(600)));

        Booking result = manager.reschedule("B-1", "FL-NEW", passengersSeatMap, 2000.0);

        assertSame(existingBooking, result);
        assertEquals(BookingStatus.PENDING, existingBooking.getStatus());

        // lock new flight seats
        verify(seatLockService).lockSeats("FL-NEW", newSeatIds);

        // release old seats on old flight
        verify(seatJdbcRepository).updateStatus("FL-OLD", List.of("S1", "S2"), SeatStatus.AVAILABLE);

        // release old lock
        verify(seatLockService).releaseLock("FL-OLD");

        // booking updated with new passenger-seat map
        verify(bookingRepository).update(eq(existingBooking), eq(passengersSeatMap));

        // change fee collected
        verify(paymentService).collectChangeFee("B-1", 300, "Schedule change");

        // notify IT
        verify(airlineItService).notifyReschedule(existingBooking);
    }


}
