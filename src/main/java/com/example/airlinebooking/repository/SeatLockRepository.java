package com.example.airlinebooking.repository;

import java.util.List;
import java.util.Optional;
import com.example.airlinebooking.domain.SeatLock;

/**
 * Seat lock repository interface to keep lock persistence swappable.
 */
public interface SeatLockRepository {
    SeatLock insert(SeatLock lock);

    Optional<SeatLock> findById(String id);

    void delete(String id);

    void deleteByFlightId(String flightId);
}
