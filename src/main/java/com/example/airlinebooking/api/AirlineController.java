package com.example.airlinebooking.api;

import com.example.airlinebooking.domain.FareClass;
import com.example.airlinebooking.domain.Passenger;
import com.example.airlinebooking.service.BookingProcessManager;
import com.example.airlinebooking.service.BookingService;
import com.example.airlinebooking.service.FareService;
import com.example.airlinebooking.service.FlightSearchService;
import com.example.airlinebooking.service.SeatInventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST API facade chosen to keep the exercise focused on domain flow without extra gateway components.
 */
@RestController
@RequestMapping("/api")
public class AirlineController {
    private final FlightSearchService flightSearchService;
    private final FareService fareService;
    private final BookingService bookingService;
    private final SeatInventoryService seatInventoryService;
    private final BookingProcessManager bookingProcessManager;

    public AirlineController(FlightSearchService flightSearchService, FareService fareService, BookingService bookingService,
                             SeatInventoryService seatInventoryService, BookingProcessManager bookingProcessManager) {
        this.flightSearchService = flightSearchService;
        this.fareService = fareService;
        this.bookingService = bookingService;
        this.seatInventoryService = seatInventoryService;
        this.bookingProcessManager = bookingProcessManager;
    }

    @GetMapping("/flights/search")
    public List<FlightSummary> searchFlights(@RequestParam String origin, @RequestParam String destination) {
        return flightSearchService.search(origin, destination).stream()
                .map(this::toSummary)
                .toList();
    }

    @GetMapping("/flights/{flightId}/seats")
    public SeatAvailabilityResponse seatAvailability(@PathVariable String flightId, @RequestParam FareClass fareClass) {
        List<String> seats = seatInventoryService.availableSeatIds(flightId, fareClass);
        return new SeatAvailabilityResponse(flightId, fareClass.name(), seats);
    }

    @GetMapping("/flights/{flightId}/fare-rules")
    public FareRulesResponse fareRules(@PathVariable String flightId, @RequestParam FareClass fareClass) {
        return new FareRulesResponse(
                flightId,
                fareClass.name(),
                fareService.fareRules(fareClass),
                fareService.baggagePolicy(fareClass)
        );
    }

    @PostMapping("/bookings")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public PaymentInitiatedResponse createBooking(@Valid @RequestBody BookingRequest request) {
        Passenger passenger = new Passenger(UUID.randomUUID().toString(), request.getPassengerName(), request.getPassengerEmail());
        var transaction = bookingProcessManager.startBooking(request.getFlightId(), passenger, request.getSeatIds(),
                request.getAmountCents());
        return new PaymentInitiatedResponse(
                transaction.getBookingId(),
                transaction.getId(),
                transaction.getStatus().name(),
                transaction.getExpiresAt()
        );
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public CancelResponse cancelBooking(@PathVariable String bookingId) {
        var booking = bookingService.cancel(bookingId);
        String refundStatus = booking.getStatus().name().equals("REFUNDED") ? "REFUNDED" : "PENDING";
        return new CancelResponse(booking.getId(), booking.getStatus().name(), refundStatus);
    }

    @PostMapping("/bookings/{bookingId}/reschedule")
    public RescheduleResponse rescheduleBooking(@PathVariable String bookingId, @Valid @RequestBody RescheduleRequest request) {
        var newBooking = bookingService.reschedule(bookingId, request.getNewFlightId(), request.getSeatIds());
        return new RescheduleResponse(
                bookingId,
                newBooking.getId(),
                newBooking.getStatus().name(),
                newBooking.getFlightId(),
                newBooking.getSeatIds()
        );
    }

    @PostMapping("/payments/{transactionId}/success")
    public PaymentResultResponse paymentSuccess(@PathVariable String transactionId) {
        var booking = bookingProcessManager.handlePaymentSuccess(transactionId);
        return new PaymentResultResponse(transactionId, booking.getId(), booking.getStatus().name());
    }

    @PostMapping("/payments/{transactionId}/failure")
    public PaymentResultResponse paymentFailure(@PathVariable String transactionId) {
        bookingProcessManager.handlePaymentFailure(transactionId);
        return new PaymentResultResponse(transactionId, null, "FAILED");
    }

    @PostMapping("/payments/expire")
    public PaymentExpiryResponse expirePayments() {
        int released = bookingProcessManager.releaseExpiredPayments();
        return new PaymentExpiryResponse(released);
    }

    private FlightSummary toSummary(Flight flight) {
        return new FlightSummary(
                flight.getId(),
                flight.getFlightNumber(),
                flight.getOrigin(),
                flight.getDestination(),
                flight.getDepartureTime()
        );
    }
}
