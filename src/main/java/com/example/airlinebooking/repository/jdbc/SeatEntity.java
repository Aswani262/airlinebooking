package com.example.airlinebooking.repository.jdbc;

import com.example.airlinebooking.domain.FareClass;
import com.example.airlinebooking.domain.SeatStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Represents per-flight seat inventory so locks and bookings mutate rows atomically in Postgres.
 */
@Table("seats")
@Data
public class SeatEntity {
    @Id
    private String id;
    private String flightId;
    private String aircraftId;
    private String seatNumber;
    private FareClass fareClass;
    private SeatStatus status;

    public SeatEntity() {
    }

    public SeatEntity(String id, String flightId, String aircraftId, String seatNumber, FareClass fareClass, SeatStatus status) {
        this.id = id;
        this.flightId = flightId;
        this.aircraftId = aircraftId;
        this.seatNumber = seatNumber;
        this.fareClass = fareClass;
        this.status = status;
    }

}
