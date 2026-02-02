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
        //Add unique constraint on (flight_id, seat_id) in seat_locks table to prevent double locking
        SeatLock lock = new SeatLock(UUID.randomUUID().toString(), flightId, seatIds, Instant.now().plus(HOLD_DURATION));
        seatLockRepository.insert(lock);
        return lock;
    }

    @Override
    @Transactional
    public void releaseLock(String fightId) {
        // For simplicity, we delete the lock record here
        // we also have a scheduled job to clean up expired locks
        //Why this table because query on flight_id and seat_ids is complex on seat table for expired locks is going to complex
        // this also applied to booking table for cleaning up expired bookings
        seatLockRepository.deleteByFlightId(fightId);
    }


}
