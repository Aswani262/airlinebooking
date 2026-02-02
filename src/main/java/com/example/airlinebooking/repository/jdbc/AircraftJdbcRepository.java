package com.example.airlinebooking.repository.jdbc;

import org.springframework.data.repository.CrudRepository;

/**
 * Spring Data JDBC repository to access aircraft reference data.
 */
public interface AircraftJdbcRepository extends CrudRepository<AircraftEntity, String> {
}
