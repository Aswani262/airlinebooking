package com.example.airlinebooking.repository.jdbc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Stores aircraft details separately so multiple flights can reference a stable equipment record.
 */
@Table("aircraft")
@Data
public class AircraftEntity {
    @Id
    private String id;
    private String model;


    public AircraftEntity(String id, String model) {
        this.id = id;
        this.model = model;
    }

}
