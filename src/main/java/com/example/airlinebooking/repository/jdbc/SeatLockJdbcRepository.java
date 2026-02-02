package com.example.airlinebooking.repository.jdbc;

import org.springframework.data.repository.CrudRepository;

/**
 * CRUD repository for seat lock headers.
 */
public interface SeatLockJdbcRepository extends CrudRepository<SeatLockEntity, String> {
}
