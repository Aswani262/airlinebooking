package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.SeatLock;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores seat locks in memory to keep the lock lifecycle explicit in the sample.
 */
@Repository
public class SeatLockRepository {
    private final Map<String, SeatLock> locks = new ConcurrentHashMap<>();

    public SeatLock save(SeatLock lock) {
        locks.put(lock.id(), lock);
        return lock;
    }

    public Optional<SeatLock> findById(String id) {
        return Optional.ofNullable(locks.get(id));
    }

    public void delete(String id) {
        locks.remove(id);
    }
}
