package com.example.airlinebooking.repository.jdbc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Join table listing the seats held by a lock, enabling efficient release.
 */
@Data
@Table("seat_lock_seats")
public class SeatLockSeatEntity {
    @Id
    private Long id;
    private String lockId;
    private String seatId;

    public SeatLockSeatEntity() {
    }

    public SeatLockSeatEntity(Long id, String lockId, String seatId) {
        this.id = id;
        this.lockId = lockId;
        this.seatId = seatId;
    }

}
