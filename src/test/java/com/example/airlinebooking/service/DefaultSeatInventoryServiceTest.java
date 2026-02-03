package com.example.airlinebooking.service;

import com.example.airlinebooking.domain.FareClass;
import com.example.airlinebooking.domain.SeatStatus;
import com.example.airlinebooking.repository.jdbc.SeatJdbcRepository;
import com.example.airlinebooking.repository.jdbc.SeatEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultSeatInventoryServiceTest {

    @Mock
    private SeatJdbcRepository seatJdbcRepository;

    @InjectMocks
    private DefaultSeatInventoryService seatInventoryService;

    @Test
    void availableSeatIds_shouldReturnSeatIdsForAvailableSeats() {
        String flightId = "FL-1";
        FareClass fareClass = FareClass.ECONOMY;

        SeatEntity s1 = mock(SeatEntity.class);
        SeatEntity s2 = mock(SeatEntity.class);

        when(s1.getId()).thenReturn("S1");
        when(s2.getId()).thenReturn("S2");

        when(seatJdbcRepository.findByFlightIdAndFareClassAndStatus(
                flightId, fareClass, SeatStatus.AVAILABLE
        )).thenReturn(List.of(s1, s2));

        // Act
        List<String> result = seatInventoryService.availableSeatIds(flightId, fareClass);

        // Assert
        assertEquals(List.of("S1", "S2"), result);

        verify(seatJdbcRepository).findByFlightIdAndFareClassAndStatus(
                flightId, fareClass, SeatStatus.AVAILABLE
        );
    }

    @Test
    void availableSeatIds_shouldReturnEmptyListWhenNoSeatsAvailable() {
        String flightId = "FL-1";
        FareClass fareClass = FareClass.BUSINESS;

        when(seatJdbcRepository.findByFlightIdAndFareClassAndStatus(
                flightId, fareClass, SeatStatus.AVAILABLE
        )).thenReturn(List.of());

        // Act
        List<String> result = seatInventoryService.availableSeatIds(flightId, fareClass);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(seatJdbcRepository).findByFlightIdAndFareClassAndStatus(
                flightId, fareClass, SeatStatus.AVAILABLE
        );
    }
}
