INSERT INTO cars (created_at, updated_at, version, license_plate, brand, model, category, branch_id, status, base_daily_rate)
VALUES
    (NOW(), NOW(), 0, 'FF666FF', 'Peugeot', '208', 'ECONOMY', 1, 'AVAILABLE', 52.00),
    (NOW(), NOW(), 0, 'GG777GG', 'Renault', 'Clio', 'ECONOMY', 2, 'AVAILABLE', 50.00),
    (NOW(), NOW(), 0, 'HH888HH', 'Jeep', 'Compass', 'SUV', 2, 'AVAILABLE', 118.00),
    (NOW(), NOW(), 0, 'II999II', 'Volvo', 'XC60', 'SUV', 1, 'AVAILABLE', 132.00),
    (NOW(), NOW(), 0, 'JJ111JJ', 'Tesla', 'Model 3', 'LUXURY', 1, 'AVAILABLE', 195.00),
    (NOW(), NOW(), 0, 'KK222KK', 'BMW', 'Serie 5', 'LUXURY', 2, 'AVAILABLE', 205.00),
    (NOW(), NOW(), 0, 'LL333LL', 'Ford', 'Transit', 'VAN', 1, 'AVAILABLE', 125.00)
ON CONFLICT (license_plate) DO NOTHING;
