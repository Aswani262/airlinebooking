package com.example.airlinebooking.repository.jdbc;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * CRUD repository for seat lock headers.
 */
public interface SeatLockJdbcRepository extends CrudRepository<SeatLockEntity, String> {


    @Modifying
    @Query("DELETE FROM seat_locks WHERE flight_id = :flightId")
    void deleteByFlightId(String flightId);

    @Query("SELECT * FROM seat_locks WHERE expires_at <= :now")
    List<SeatLockEntity> findExpiredLocks(Instant now);
}
