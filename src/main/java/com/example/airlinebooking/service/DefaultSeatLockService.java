package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.SeatLock;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.repository.FlightRepository;
import com.example.airlinebooking.repository.SeatLockRepository;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Default seat lock service that persists lock state in PostgreSQL.
 */
@Service
public class DefaultSeatLockService implements SeatLockService {
    private static final Duration HOLD_DURATION = Duration.ofMinutes(15);

    private final FlightRepository flightRepository;
    private final SeatLockRepository seatLockRepository;
    private final SeatJdbcRepository seatJdbcRepository;

    public DefaultSeatLockService(FlightRepository flightRepository, SeatLockRepository seatLockRepository,
                                  SeatJdbcRepository seatJdbcRepository) {
        this.flightRepository = flightRepository;
        this.seatLockRepository = seatLockRepository;
        this.seatJdbcRepository = seatJdbcRepository;
    }

    @Override
    @Transactional
    public SeatLock lockSeats(String flightId, List<String> seatIds) {
        flightRepository.findById(flightId)
                .orElseThrow(() -> new IllegalArgumentException("Flight not found"));
        var seatsToLock = seatJdbcRepository.findByFlightIdAndSeatIds(flightId, seatIds);
        if (seatsToLock.size() != seatIds.size()) {
            throw new IllegalArgumentException("Some seats do not exist");
        }
        boolean allAvailable = seatsToLock.stream().allMatch(seat -> seat.getStatus() == SeatStatus.AVAILABLE);
        if (!allAvailable) {
            throw new IllegalStateException("One or more seats are unavailable");
        }
        int updated = seatJdbcRepository.updateStatusIfCurrent(flightId, seatIds, SeatStatus.AVAILABLE, SeatStatus.LOCKED);
        if (updated != seatIds.size()) {
            throw new IllegalStateException("Seat availability changed while locking");
        }
        SeatLock lock = new SeatLock(UUID.randomUUID().toString(), flightId, seatIds, Instant.now().plus(HOLD_DURATION));
        seatLockRepository.save(lock);
        return lock;
    }

    @Override
    @Transactional
    public void releaseLock(SeatLock lock) {
        seatJdbcRepository.updateStatusIfCurrent(lock.flightId(), lock.seatIds(), SeatStatus.LOCKED, SeatStatus.AVAILABLE);
        seatLockRepository.delete(lock.id());
    }

    @Override
    @Transactional
    public void finalizeLock(SeatLock lock) {
        seatLockRepository.delete(lock.id());
    }
}
