package com.example.airlinebooking.api;

import com.example.airlinebooking.domain.FareClass;
import com.example.airlinebooking.domain.Flight;
import com.example.airlinebooking.domain.Passenger;
import com.example.airlinebooking.domain.Seat;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.service.BookingService;
import com.example.airlinebooking.service.FareService;
import com.example.airlinebooking.service.FlightSearchService;
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

    public AirlineController(FlightSearchService flightSearchService, FareService fareService, BookingService bookingService) {
        this.flightSearchService = flightSearchService;
        this.fareService = fareService;
        this.bookingService = bookingService;
    }

    @GetMapping("/flights/search")
    public List<FlightSummary> searchFlights(@RequestParam String origin, @RequestParam String destination) {
        return flightSearchService.search(origin, destination).stream()
                .map(this::toSummary)
                .toList();
    }

    @GetMapping("/flights/{flightId}/seats")
    public SeatAvailabilityResponse seatAvailability(@PathVariable String flightId, @RequestParam FareClass fareClass) {
        Flight flight = flightSearchService.getById(flightId);
        List<String> seats = flight.getAircraft().seats().stream()
                .filter(seat -> seat.getFareClass() == fareClass)
                .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                .map(Seat::getId)
                .toList();
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
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request) {
        Passenger passenger = new Passenger(UUID.randomUUID().toString(), request.getPassengerName(), request.getPassengerEmail());
        var booking = bookingService.book(request.getFlightId(), passenger, request.getSeatIds());
        return new BookingResponse(
                booking.getId(),
                booking.getFlightId(),
                booking.getPassenger().fullName(),
                booking.getStatus().name(),
                booking.getSeatIds(),
                booking.getCreatedAt()
        );
    }

    @PostMapping("/bookings/{bookingId}/cancel")
    public CancelResponse cancelBooking(@PathVariable String bookingId) {
        var booking = bookingService.cancel(bookingId);
        return new CancelResponse(booking.getId(), booking.getStatus().name());
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
