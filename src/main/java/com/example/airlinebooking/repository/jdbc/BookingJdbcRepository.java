package com.example.airlinebooking.repository.jdbc;

import org.springframework.data.repository.CrudRepository;

/**
 * Spring Data JDBC access for bookings as the booking aggregate root.
 */
public interface BookingJdbcRepository extends CrudRepository<BookingEntity, String> {
}
