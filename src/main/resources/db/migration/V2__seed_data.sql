INSERT INTO branches (created_at, updated_at, version, name, city, address)
VALUES
    (NOW(), NOW(), 0, 'Milano Centrale', 'Milano', 'Via Centrale 10'),
    (NOW(), NOW(), 0, 'Roma Termini', 'Roma', 'Piazza dei Noleggi 5');

INSERT INTO cars (created_at, updated_at, version, license_plate, brand, model, category, branch_id, status, base_daily_rate)
VALUES
    (NOW(), NOW(), 0, 'AA111AA', 'Fiat', '500', 'ECONOMY', 1, 'AVAILABLE', 49.00),
    (NOW(), NOW(), 0, 'BB222BB', 'Toyota', 'Yaris', 'ECONOMY', 1, 'AVAILABLE', 55.00),
    (NOW(), NOW(), 0, 'CC333CC', 'BMW', 'X3', 'SUV', 1, 'AVAILABLE', 110.00),
    (NOW(), NOW(), 0, 'DD444DD', 'Mercedes', 'Vito', 'VAN', 2, 'AVAILABLE', 120.00),
    (NOW(), NOW(), 0, 'EE555EE', 'Audi', 'A6', 'LUXURY', 2, 'AVAILABLE', 180.00);
