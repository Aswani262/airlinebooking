package com.example.airlinebooking.service;

import java.util.List;
import com.example.airlinebooking.domain.SeatLock;

/**
 * Manages temporary seat holds to prevent double booking while payments are processed.
 */
public interface SeatLockService {
    SeatLock lockSeats(String flightId, List<String> seatIds);

    void releaseLock(String fightId);

}
