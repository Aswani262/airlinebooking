package com.example.airlinebooking.repository.jdbc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Join table for the seats tied to a booking for quick release or confirmation updates.
 */
@Table("booking_seats")
@Data
public class BookingSeatEntity {
    @Id
    private Long id;
    private String bookingId;
    private String seatId;
    private String passengerName;


    public BookingSeatEntity() {
    }

    public BookingSeatEntity(Long id, String bookingId, String seatId, String passengerName) {
        this.id = id;
        this.bookingId = bookingId;
        this.seatId = seatId;
        this.passengerName = passengerName;
    }

}
