package com.example.airlinebooking.service;

import java.util.List;
import com.example.airlinebooking.domain.FareClass;

/**
 * Read-only seat inventory queries to keep availability checks separate from lock mutations.
 */
public interface SeatInventoryService {
    List<String> availableSeatIds(String flightId, FareClass fareClass);
}
