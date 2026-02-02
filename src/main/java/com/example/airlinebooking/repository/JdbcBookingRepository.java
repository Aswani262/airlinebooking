package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.Booking;
import com.example.airlinebooking.domain.Passenger;
import com.example.airlinebooking.repository.jdbc.BookingEntity;
import com.example.airlinebooking.repository.jdbc.BookingJdbcRepository;
import com.example.airlinebooking.repository.jdbc.BookingSeatEntity;
import com.example.airlinebooking.repository.jdbc.BookingSeatJdbcRepository;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JDBC-backed booking repository to persist bookings and their seat mappings in PostgreSQL.
 */
@Repository
public class JdbcBookingRepository implements BookingRepository {
    private final BookingJdbcRepository bookingJdbcRepository;
    private final BookingSeatJdbcRepository bookingSeatJdbcRepository;
    private final JdbcAggregateTemplate jdbcAggregateTemplate;

    public JdbcBookingRepository(BookingJdbcRepository bookingJdbcRepository, BookingSeatJdbcRepository bookingSeatJdbcRepository, JdbcAggregateTemplate jdbcAggregateTemplate) {
        this.bookingJdbcRepository = bookingJdbcRepository;
        this.bookingSeatJdbcRepository = bookingSeatJdbcRepository;
        this.jdbcAggregateTemplate = jdbcAggregateTemplate;
    }

    @Override
    public Booking update(Booking booking, Map<String,String> passengerSeatMap) {
        BookingEntity entity = new BookingEntity(
                booking.getId(),
                booking.getFlightId(),
                booking.getStatus(),
                booking.getAmount(),
                booking.getCreatedAt()
        );
        bookingJdbcRepository.save(entity);
        for (Map.Entry<String, String>  seatIdPassenger : passengerSeatMap.entrySet()) {
            bookingSeatJdbcRepository.save(new BookingSeatEntity(null, booking.getId(), seatIdPassenger.getKey(),seatIdPassenger.getValue()));
        }
        return booking;
    }

    @Override
    public Optional<Booking> findById(String id) {
        Optional<BookingEntity> entity = bookingJdbcRepository.findById(id);
        if (entity.isEmpty()) {
            return Optional.empty();
        }
        List<String> seatIds = bookingSeatJdbcRepository.findByBookingId(id).stream()
                .map(BookingSeatEntity::getSeatId)
                .collect(Collectors.toList());
        BookingEntity bookingEntity = entity.get();

        return Optional.of(new Booking(
                bookingEntity.getId(),
                bookingEntity.getFlightId(),
                seatIds,
                bookingEntity.getStatus(),
                bookingEntity.getAmount(),
                bookingEntity.getCreatedAt()
        ));
    }

    @Override
    public void insert(Booking booking, Map<String, String> passagnerSeatMap) {
        jdbcAggregateTemplate.insert(new BookingEntity(
                booking.getId(),
                booking.getFlightId(),
                booking.getStatus(),
                booking.getAmount(),
                booking.getCreatedAt()
        ));
        for (Map.Entry<String, String>  seatIdPassenger : passagnerSeatMap.entrySet()) {
            bookingSeatJdbcRepository.save(new BookingSeatEntity(null, booking.getId(), seatIdPassenger.getKey(),seatIdPassenger.getValue()));
        }
    }
}
