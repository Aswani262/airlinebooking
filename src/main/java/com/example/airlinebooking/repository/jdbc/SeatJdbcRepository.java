package com.example.airlinebooking.repository.jdbc;

import com.example.airlinebooking.domain.FareClass;
import com.example.airlinebooking.domain.SeatStatus;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

/**
 * Seat inventory repository that supports availability checks and status updates.
 */
public interface SeatJdbcRepository extends CrudRepository<SeatEntity, String> {
    @Query("SELECT * FROM seats WHERE flight_id = :flightId")
    List<SeatEntity> findByFlightId(String flightId);

    @Query("SELECT * FROM seats WHERE flight_id = :flightId AND fare_class = :fareClass AND status = :status")
    List<SeatEntity> findByFlightIdAndFareClassAndStatus(String flightId, FareClass fareClass, SeatStatus status);

    @Query("SELECT * FROM seats WHERE flight_id = :flightId AND id IN (:seatIds)")
    List<SeatEntity> findByFlightIdAndSeatIds(String flightId, Collection<String> seatIds);

    @Modifying
    @Query("UPDATE seats SET status = :newStatus WHERE flight_id = :flightId AND id IN (:seatIds) AND status = :currentStatus")
    int updateStatusIfCurrent(String flightId, Collection<String> seatIds, SeatStatus currentStatus, SeatStatus newStatus);

    @Modifying
    @Query("UPDATE seats SET status = :newStatus WHERE flight_id = :flightId AND id IN (:seatIds)")
    int updateStatus(String flightId, Collection<String> seatIds, SeatStatus newStatus);


    @Modifying
    @Query("""
        UPDATE seats
        SET status = :status
        WHERE id IN (:seatIds)
        """)
    int updateStatusByIds(List<String> seatIds, SeatStatus seatStatus);
}
