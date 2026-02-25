# Database Access Guide (PostgreSQL)

## Servizi disponibili
- PostgreSQL: `localhost:5432`
- pgAdmin: `http://localhost:5050`
- Adminer (equivalente lightweight di phpMyAdmin): `http://localhost:8081`

Avvio stack:
```bash
docker compose up -d
```

## Credenziali DB
- DB: `smart_rental`
- User: `smart_user`
- Password: `smart_pass`
- Host (da host machine): `localhost`
- Host (tra container): `postgres`

## Accesso rapido con Adminer
1. Apri `http://localhost:8081`
2. Seleziona `PostgreSQL`
3. Inserisci:
   - Server: `postgres`
   - Username: `smart_user`
   - Password: `smart_pass`
   - Database: `smart_rental`
4. Login

## Accesso rapido con pgAdmin
1. Apri `http://localhost:5050`
2. Login UI:
   - Email: `admin@smartauto.com`
   - Password: `admin`
3. Registra server:
   - Name: `smart-rental-db`
   - Host: `postgres`
   - Port: `5432`
   - Username: `smart_user`
   - Password: `smart_pass`

## Query utili (lettura)
```sql
-- versioni migration applicate
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;

-- flotta totale e per categoria
SELECT category, COUNT(*) AS total
FROM cars
GROUP BY category
ORDER BY category;

-- prenotazioni recenti
SELECT id, code, status, payment_status, start_time, end_time, total_price
FROM bookings
ORDER BY created_at DESC
LIMIT 20;

-- auto in manutenzione pianificata/in corso
SELECT m.id, c.license_plate, m.status, m.start_time, m.end_time
FROM maintenance_records m
JOIN cars c ON c.id = m.car_id
WHERE m.status IN ('SCHEDULED', 'IN_PROGRESS')
ORDER BY m.start_time;
```

## CLI psql da container
```bash
docker compose exec -T postgres psql -U smart_user -d smart_rental
```

## Backup / restore rapido
Backup:
```bash
docker compose exec -T postgres pg_dump -U smart_user -d smart_rental > backup.sql
```

Restore:
```bash
cat backup.sql | docker compose exec -T postgres psql -U smart_user -d smart_rental
```
