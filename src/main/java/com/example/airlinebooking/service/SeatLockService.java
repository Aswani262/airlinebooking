package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.Flight;
import com.example.airlinebooking.domain.Seat;
import com.example.airlinebooking.domain.SeatLock;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.repository.FlightRepository;
import com.example.airlinebooking.repository.SeatLockRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Manages temporary seat holds to prevent double booking while payments are processed.
 */
@Service
public class SeatLockService {
    private static final Duration HOLD_DURATION = Duration.ofMinutes(15);

    private final FlightRepository flightRepository;
    private final SeatLockRepository seatLockRepository;

    public SeatLockService(FlightRepository flightRepository, SeatLockRepository seatLockRepository) {
        this.flightRepository = flightRepository;
        this.seatLockRepository = seatLockRepository;
    }

    public SeatLock lockSeats(String flightId, List<String> seatIds) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        synchronized (flight) {
            List<Seat> seatsToLock = flight.getAircraft().seats().stream()
                    .filter(seat -> seatIds.contains(seat.getId()))
                    .toList();
            if (seatsToLock.size() != seatIds.size()) {
                throw new IllegalArgumentException("Some seats do not exist");
            }
            boolean allAvailable = seatsToLock.stream().allMatch(seat -> seat.getStatus() == SeatStatus.AVAILABLE);
            if (!allAvailable) {
                throw new IllegalStateException("One or more seats are unavailable");
            }
            seatsToLock.forEach(seat -> seat.setStatus(SeatStatus.LOCKED));
            SeatLock lock = new SeatLock(UUID.randomUUID().toString(), flightId, seatIds, Instant.now().plus(HOLD_DURATION));
            seatLockRepository.save(lock);
            return lock;
        }
    }

    public void releaseLock(SeatLock lock) {
        flightRepository.findById(lock.flightId()).ifPresent(flight -> {
            synchronized (flight) {
                flight.getAircraft().seats().stream()
                        .filter(seat -> lock.seatIds().contains(seat.getId()))
                        .filter(seat -> seat.getStatus() == SeatStatus.LOCKED)
                        .forEach(seat -> seat.setStatus(SeatStatus.AVAILABLE));
                seatLockRepository.delete(lock.id());
            }
        });
    }

    public void finalizeLock(SeatLock lock) {
        seatLockRepository.delete(lock.id());
    }
}
