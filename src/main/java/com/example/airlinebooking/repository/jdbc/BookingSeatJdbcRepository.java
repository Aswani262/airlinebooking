package com.example.airlinebooking.repository.jdbc;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Stores booking-to-seat mappings for seat release and retrieval.
 */
public interface BookingSeatJdbcRepository extends CrudRepository<BookingSeatEntity, Long> {
    @Query("SELECT * FROM booking_seats WHERE booking_id = :bookingId")
    List<BookingSeatEntity> findByBookingId(String bookingId);

    @Modifying
    @Query("DELETE FROM booking_seats WHERE booking_id = :bookingId")
    void deleteByBookingId(String bookingId);
}
