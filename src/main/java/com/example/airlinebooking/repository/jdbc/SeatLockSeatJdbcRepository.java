package com.example.airlinebooking.repository.jdbc;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Stores seat lock seat mappings for releases and validation.
 */
public interface SeatLockSeatJdbcRepository extends CrudRepository<SeatLockSeatEntity, Long> {
    @Query("SELECT * FROM seat_lock_seats WHERE lock_id = :lockId")
    List<SeatLockSeatEntity> findByLockId(String lockId);

    @Modifying
    @Query("DELETE FROM seat_lock_seats WHERE lock_id = :lockId")
    void deleteByLockId(String lockId);
}
