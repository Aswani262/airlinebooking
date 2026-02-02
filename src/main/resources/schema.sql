CREATE TABLE aircraft (
    id VARCHAR(40) PRIMARY KEY,
    model VARCHAR(50) NOT NULL
);

CREATE TABLE flights (
    id VARCHAR(40) PRIMARY KEY,
    flight_number VARCHAR(20) NOT NULL,
    origin VARCHAR(10) NOT NULL,
    destination VARCHAR(10) NOT NULL,
    departure_time TIMESTAMPTZ NOT NULL,
    aircraft_id VARCHAR(40) NOT NULL REFERENCES aircraft(id)
);

CREATE INDEX flights_route_idx ON flights (origin, destination);

CREATE TABLE seats (
    id VARCHAR(40) PRIMARY KEY,
    flight_id VARCHAR(40) NOT NULL REFERENCES flights(id),
    aircraft_id VARCHAR(40) NOT NULL REFERENCES aircraft(id),
    seat_number VARCHAR(10) NOT NULL,
    fare_class VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE INDEX seats_flight_idx ON seats (flight_id);
CREATE INDEX seats_flight_fare_status_idx ON seats (flight_id, fare_class, status);

CREATE TABLE bookings (
    id VARCHAR(40) PRIMARY KEY,
    flight_id VARCHAR(40) NOT NULL REFERENCES flights(id),
    passenger_id VARCHAR(40) NOT NULL,
    passenger_name VARCHAR(100) NOT NULL,
    passenger_email VARCHAR(120) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX bookings_flight_idx ON bookings (flight_id);

CREATE TABLE booking_seats (
    id BIGSERIAL PRIMARY KEY,
    booking_id VARCHAR(40) NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    seat_id VARCHAR(40) NOT NULL REFERENCES seats(id)
);

CREATE INDEX booking_seats_booking_idx ON booking_seats (booking_id);
CREATE INDEX booking_seats_seat_idx ON booking_seats (seat_id);

CREATE TABLE seat_locks (
    id VARCHAR(40) PRIMARY KEY,
    flight_id VARCHAR(40) NOT NULL REFERENCES flights(id),
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX seat_locks_flight_idx ON seat_locks (flight_id);
CREATE INDEX seat_locks_expires_idx ON seat_locks (expires_at);

CREATE TABLE seat_lock_seats (
    id BIGSERIAL PRIMARY KEY,
    lock_id VARCHAR(40) NOT NULL REFERENCES seat_locks(id) ON DELETE CASCADE,
    seat_id VARCHAR(40) NOT NULL REFERENCES seats(id)
);

CREATE INDEX seat_lock_seats_lock_idx ON seat_lock_seats (lock_id);

CREATE TABLE payment_transactions (
    id VARCHAR(40) PRIMARY KEY,
    booking_id VARCHAR(40) NOT NULL,
    flight_id VARCHAR(40) NOT NULL REFERENCES flights(id),
    amount_cents INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX payment_transactions_booking_idx ON payment_transactions (booking_id);
CREATE INDEX payment_transactions_status_idx ON payment_transactions (status);
CREATE INDEX payment_transactions_expires_idx ON payment_transactions (expires_at);

CREATE TABLE payment_transaction_seats (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(40) NOT NULL REFERENCES payment_transactions(id) ON DELETE CASCADE,
    seat_id VARCHAR(40) NOT NULL REFERENCES seats(id)
);

CREATE INDEX payment_transaction_seats_tx_idx ON payment_transaction_seats (transaction_id);
