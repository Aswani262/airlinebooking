package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.SeatLock;
import com.example.airlinebooking.repository.jdbc.SeatLockEntity;
import com.example.airlinebooking.repository.jdbc.SeatLockJdbcRepository;
import com.example.airlinebooking.repository.jdbc.SeatLockSeatEntity;
import com.example.airlinebooking.repository.jdbc.SeatLockSeatJdbcRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JDBC-backed seat lock repository so lock state is shared across application instances.
 */
@Repository
public class SeatLockRepository {
    private final SeatLockJdbcRepository seatLockJdbcRepository;
    private final SeatLockSeatJdbcRepository seatLockSeatJdbcRepository;

    public SeatLockRepository(SeatLockJdbcRepository seatLockJdbcRepository, SeatLockSeatJdbcRepository seatLockSeatJdbcRepository) {
        this.seatLockJdbcRepository = seatLockJdbcRepository;
        this.seatLockSeatJdbcRepository = seatLockSeatJdbcRepository;
    }

    public SeatLock save(SeatLock lock) {
        seatLockJdbcRepository.save(new SeatLockEntity(lock.id(), lock.flightId(), lock.expiresAt()));
        seatLockSeatJdbcRepository.deleteByLockId(lock.id());
        for (String seatId : lock.seatIds()) {
            seatLockSeatJdbcRepository.save(new SeatLockSeatEntity(null, lock.id(), seatId));
        }
        return lock;
    }

    public Optional<SeatLock> findById(String id) {
        Optional<SeatLockEntity> entity = seatLockJdbcRepository.findById(id);
        if (entity.isEmpty()) {
            return Optional.empty();
        }
        List<String> seatIds = seatLockSeatJdbcRepository.findByLockId(id).stream()
                .map(SeatLockSeatEntity::getSeatId)
                .collect(Collectors.toList());
        SeatLockEntity lockEntity = entity.get();
        return Optional.of(new SeatLock(lockEntity.getId(), lockEntity.getFlightId(), seatIds, lockEntity.getExpiresAt()));
    }

    public void delete(String id) {
        seatLockSeatJdbcRepository.deleteByLockId(id);
        seatLockJdbcRepository.deleteById(id);
    }
}
