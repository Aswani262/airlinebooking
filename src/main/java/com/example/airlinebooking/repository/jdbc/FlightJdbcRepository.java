package com.example.airlinebooking.repository.jdbc;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Route-based flight lookup repository to support search requests.
 */
public interface FlightJdbcRepository extends CrudRepository<FlightEntity, String> {
    @Query("SELECT * FROM flights WHERE LOWER(origin) = LOWER(:origin) AND LOWER(destination) = LOWER(:destination)")
    List<FlightEntity> findByRoute(String origin, String destination);
}
