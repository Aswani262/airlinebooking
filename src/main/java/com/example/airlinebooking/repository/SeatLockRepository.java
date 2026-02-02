package com.example.airlinebooking.repository;

import java.util.Optional;
import com.example.airlinebooking.domain.SeatLock;

/**
 * Seat lock repository interface to keep lock persistence swappable.
 */
public interface SeatLockRepository {
    SeatLock save(SeatLock lock);

    Optional<SeatLock> findById(String id);

    void delete(String id);
}
