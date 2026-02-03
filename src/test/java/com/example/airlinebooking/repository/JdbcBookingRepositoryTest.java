package com.example.airlinebooking.repository;

import com.example.airlinebooking.domain.Booking;
import com.example.airlinebooking.domain.BookingStatus;
import com.example.airlinebooking.repository.jdbc.BookingEntity;
import com.example.airlinebooking.repository.jdbc.BookingJdbcRepository;
import com.example.airlinebooking.repository.jdbc.BookingSeatEntity;
import com.example.airlinebooking.repository.jdbc.BookingSeatJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdbcBookingRepositoryTest {

    @Mock private BookingJdbcRepository bookingJdbcRepository;
    @Mock private BookingSeatJdbcRepository bookingSeatJdbcRepository;
    @Mock private JdbcAggregateTemplate jdbcAggregateTemplate;

    private JdbcBookingRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JdbcBookingRepository(bookingJdbcRepository, bookingSeatJdbcRepository, jdbcAggregateTemplate);
    }

    @Test
    void insert_shouldInsertBookingEntity_andSeatMappings() {
        // Arrange
        Instant createdAt = Instant.parse("2026-02-03T10:00:00Z");
        Booking booking = new Booking("B-1", "FL-1", List.of("S1", "S2"), BookingStatus.PENDING, 1500.0, createdAt);

        // seatId -> passengerId
        Map<String, String> seatToPassenger = new LinkedHashMap<>();
        seatToPassenger.put("S1", "P1");
        seatToPassenger.put("S2", "P2");

        // Act
        repository.insert(booking, seatToPassenger);

        // Assert: booking entity inserted via JdbcAggregateTemplate
        ArgumentCaptor<BookingEntity> bookingEntityCaptor = ArgumentCaptor.forClass(BookingEntity.class);
        verify(jdbcAggregateTemplate).insert(bookingEntityCaptor.capture());

        BookingEntity inserted = bookingEntityCaptor.getValue();
        assertEquals("B-1", inserted.getId());
        assertEquals("FL-1", inserted.getFlightId());
        assertEquals(BookingStatus.PENDING, inserted.getStatus());
        assertEquals(1500.0, inserted.getAmount());
        assertEquals(createdAt, inserted.getCreatedAt());

        // Assert: seat mappings saved
        ArgumentCaptor<BookingSeatEntity> seatCaptor = ArgumentCaptor.forClass(BookingSeatEntity.class);
        verify(bookingSeatJdbcRepository, times(2)).save(seatCaptor.capture());

        List<BookingSeatEntity> savedSeats = seatCaptor.getAllValues();
        assertEquals(2, savedSeats.size());

        // We don’t rely on order too hard, but LinkedHashMap preserves insertion order.
        assertEquals("B-1", savedSeats.get(0).getBookingId());
        assertEquals("S1", savedSeats.get(0).getSeatId());

        assertEquals("B-1", savedSeats.get(1).getBookingId());
        assertEquals("S2", savedSeats.get(1).getSeatId());

        verifyNoInteractions(bookingJdbcRepository); // insert uses template, not repository.save
    }

    @Test
    void update_shouldSaveBookingEntity_andSeatMappings_andReturnBooking() {
        // Arrange
        Instant createdAt = Instant.parse("2026-02-03T10:00:00Z");
        Booking booking = new Booking("B-1", "FL-1", List.of("S1", "S2"), BookingStatus.CONFIRMED, 1500.0, createdAt);

        Map<String, String> seatToPassenger = new LinkedHashMap<>();
        seatToPassenger.put("S1", "P1");
        seatToPassenger.put("S2", "P2");

        when(bookingJdbcRepository.save(any(BookingEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        Booking result = repository.update(booking, seatToPassenger);

        // Assert booking saved
        ArgumentCaptor<BookingEntity> bookingEntityCaptor = ArgumentCaptor.forClass(BookingEntity.class);
        verify(bookingJdbcRepository).save(bookingEntityCaptor.capture());

        BookingEntity saved = bookingEntityCaptor.getValue();
        assertEquals("B-1", saved.getId());
        assertEquals("FL-1", saved.getFlightId());
        assertEquals(BookingStatus.CONFIRMED, saved.getStatus());
        assertEquals(1500.0, saved.getAmount());
        assertEquals(createdAt, saved.getCreatedAt());

        // Assert seat mappings saved
        ArgumentCaptor<BookingSeatEntity> seatCaptor = ArgumentCaptor.forClass(BookingSeatEntity.class);
        verify(bookingSeatJdbcRepository, times(2)).save(seatCaptor.capture());

        List<BookingSeatEntity> savedSeats = seatCaptor.getAllValues();
        assertEquals(2, savedSeats.size());

        assertEquals("B-1", savedSeats.get(0).getBookingId());
        assertEquals("S1", savedSeats.get(0).getSeatId());

        assertEquals("B-1", savedSeats.get(1).getBookingId());
        assertEquals("S2", savedSeats.get(1).getSeatId());

        assertSame(booking, result);
        verifyNoInteractions(jdbcAggregateTemplate);
    }

    @Test
    void findById_shouldReturnEmpty_whenBookingEntityNotFound() {
        when(bookingJdbcRepository.findById("B-404")).thenReturn(Optional.empty());

        Optional<Booking> result = repository.findById("B-404");

        assertTrue(result.isEmpty());

        verify(bookingJdbcRepository).findById("B-404");
        verifyNoInteractions(bookingSeatJdbcRepository);
    }

    @Test
    void findById_shouldReturnBookingWithSeatIds_whenFound() {
        // Arrange
        Instant createdAt = Instant.parse("2026-02-03T10:00:00Z");

        BookingEntity entity = new BookingEntity("B-1", "FL-1", BookingStatus.CONFIRMED, 1500.0, createdAt);
        when(bookingJdbcRepository.findById("B-1")).thenReturn(Optional.of(entity));

        // bookingSeatJdbcRepository returns seat rows; repository maps to seatIds
        BookingSeatEntity s1 = new BookingSeatEntity(1L, "B-1", "S1", "P1");
        BookingSeatEntity s2 = new BookingSeatEntity(2L, "B-1", "S2", "P2");

        when(bookingSeatJdbcRepository.findByBookingId("B-1")).thenReturn(List.of(s1, s2));

        // Act
        Optional<Booking> result = repository.findById("B-1");

        // Assert
        assertTrue(result.isPresent());
        Booking booking = result.get();

        assertEquals("B-1", booking.getId());
        assertEquals("FL-1", booking.getFlightId());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(1500.0, booking.getAmount());
        assertEquals(createdAt, booking.getCreatedAt());

        // Seat IDs should be mapped from BookingSeatEntity::getSeatId
        assertEquals(List.of("S1", "S2"), booking.getSeatIds());

        verify(bookingJdbcRepository).findById("B-1");
        verify(bookingSeatJdbcRepository).findByBookingId("B-1");
    }
}
