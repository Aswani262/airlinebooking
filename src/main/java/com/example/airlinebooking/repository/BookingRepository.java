package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.Booking;
import com.example.airlinebooking.domain.Passenger;
import com.example.airlinebooking.repository.jdbc.BookingEntity;
import com.example.airlinebooking.repository.jdbc.BookingJdbcRepository;
import com.example.airlinebooking.repository.jdbc.BookingSeatEntity;
import com.example.airlinebooking.repository.jdbc.BookingSeatJdbcRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JDBC-backed booking repository to persist bookings and their seat mappings in PostgreSQL.
 */
@Repository
public class BookingRepository {
    private final BookingJdbcRepository bookingJdbcRepository;
    private final BookingSeatJdbcRepository bookingSeatJdbcRepository;

    public BookingRepository(BookingJdbcRepository bookingJdbcRepository, BookingSeatJdbcRepository bookingSeatJdbcRepository) {
        this.bookingJdbcRepository = bookingJdbcRepository;
        this.bookingSeatJdbcRepository = bookingSeatJdbcRepository;
    }

    public Booking save(Booking booking) {
        BookingEntity entity = new BookingEntity(
                booking.getId(),
                booking.getFlightId(),
                booking.getPassenger().id(),
                booking.getPassenger().fullName(),
                booking.getPassenger().email(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
        bookingJdbcRepository.save(entity);
        bookingSeatJdbcRepository.deleteByBookingId(booking.getId());
        for (String seatId : booking.getSeatIds()) {
            bookingSeatJdbcRepository.save(new BookingSeatEntity(null, booking.getId(), seatId));
        }
        return booking;
    }

    public Optional<Booking> findById(String id) {
        Optional<BookingEntity> entity = bookingJdbcRepository.findById(id);
        if (entity.isEmpty()) {
            return Optional.empty();
        }
        List<String> seatIds = bookingSeatJdbcRepository.findByBookingId(id).stream()
                .map(BookingSeatEntity::getSeatId)
                .collect(Collectors.toList());
        BookingEntity bookingEntity = entity.get();
        Passenger passenger = new Passenger(bookingEntity.getPassengerId(), bookingEntity.getPassengerName(),
                bookingEntity.getPassengerEmail());
        return Optional.of(new Booking(
                bookingEntity.getId(),
                bookingEntity.getFlightId(),
                passenger,
                seatIds,
                bookingEntity.getStatus(),
                bookingEntity.getCreatedAt()
        ));
    }
}
