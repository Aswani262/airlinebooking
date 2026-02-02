INSERT INTO aircraft (id, model) VALUES
('AC-1', 'A320'),
('AC-2', 'B737');

INSERT INTO flights (id, flight_number, origin, destination, departure_time, aircraft_id) VALUES
('FL-100', 'XY100', 'JFK', 'SFO', '2025-05-10 08:00:00+00', 'AC-1'),
('FL-200', 'XY200', 'SFO', 'SEA', '2025-05-11 09:30:00+00', 'AC-2');

INSERT INTO seats (id, flight_id, aircraft_id, seat_number, fare_class, status) VALUES
('AE1', 'FL-100', 'AC-1', '1A', 'ECONOMY', 'AVAILABLE'),
('AE2', 'FL-100', 'AC-1', '2A', 'ECONOMY', 'AVAILABLE'),
('AE3', 'FL-100', 'AC-1', '3A', 'ECONOMY', 'AVAILABLE'),
('AE4', 'FL-100', 'AC-1', '4A', 'ECONOMY', 'AVAILABLE'),
('AE5', 'FL-100', 'AC-1', '5A', 'ECONOMY', 'AVAILABLE'),
('AP1', 'FL-100', 'AC-1', '21B', 'PREMIUM_ECONOMY', 'AVAILABLE'),
('AP2', 'FL-100', 'AC-1', '22B', 'PREMIUM_ECONOMY', 'AVAILABLE'),
('AB1', 'FL-100', 'AC-1', '31C', 'BUSINESS', 'AVAILABLE'),
('AB2', 'FL-100', 'AC-1', '32C', 'BUSINESS', 'AVAILABLE'),
('BE1', 'FL-200', 'AC-2', '1A', 'ECONOMY', 'AVAILABLE'),
('BE2', 'FL-200', 'AC-2', '2A', 'ECONOMY', 'AVAILABLE'),
('BE3', 'FL-200', 'AC-2', '3A', 'ECONOMY', 'AVAILABLE'),
('BP1', 'FL-200', 'AC-2', '21B', 'PREMIUM_ECONOMY', 'AVAILABLE'),
('BB1', 'FL-200', 'AC-2', '31C', 'BUSINESS', 'AVAILABLE');
