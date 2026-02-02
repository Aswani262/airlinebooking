package com.example.airlinebooking.repository.jdbc;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Stores aircraft details separately so multiple flights can reference a stable equipment record.
 */
@Table("aircraft")
public class AircraftEntity {
    @Id
    private String id;
    private String model;

    public AircraftEntity() {
    }

    public AircraftEntity(String id, String model) {
        this.id = id;
        this.model = model;
    }

    public String getId() {
        return id;
    }

    public String getModel() {
        return model;
    }
}
