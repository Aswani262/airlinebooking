package com.example.airlinebooking.repository.jdbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Join table for the seats tied to a booking for quick release or confirmation updates.
 */
@Table("booking_seats")
public class BookingSeatEntity {
    @Id
    private Long id;
    private String bookingId;
    private String seatId;

    public BookingSeatEntity() {
    }

    public BookingSeatEntity(Long id, String bookingId, String seatId) {
        this.id = id;
        this.bookingId = bookingId;
        this.seatId = seatId;
    }

    public Long getId() {
        return id;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getSeatId() {
        return seatId;
    }
}
