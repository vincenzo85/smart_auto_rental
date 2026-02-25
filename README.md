# Smart Auto Rental Platform

Piattaforma MVP Spring Boot per noleggio auto smart con pricing dinamico base, disponibilita, prenotazioni, pagamenti simulati, manutenzione, audit e osservabilita.

## 1) OODA

### Observe
- Attori: `CUSTOMER`, `ADMIN`, `OPERATOR`, manutentore (ruolo operativo estendibile).
- Casi d'uso principali:
  - autenticazione JWT
  - gestione flotta (CRUD auto)
  - ricerca disponibilita per sede/intervallo/categoria
  - prenotazione con conflitto date
  - pagamento mock e conferma condizionata
  - manutenzione e blocco disponibilita
  - report admin
- Vincoli tecnici: Java 21, Spring Boot 3.x, PostgreSQL, Docker Compose, Flyway.
- Rischi gestiti:
  - overbooking: lock pessimista su auto + controllo conflitto in transazione
  - date invalide: validazione e business rules
  - pagamento fallito/pending: booking non confermato
  - auto in manutenzione: esclusione in ricerca e booking

### Orient
- Architettura scelta: monolite modulare (MVP) con confini chiari per evoluzione.
- Persistenza: PostgreSQL + Flyway migrations.
- API: REST + OpenAPI (`/swagger-ui.html`).
- Auth: JWT stateless con ruoli.
- Caching/locking: locking transazionale DB (Redis opzionale v2).
- Test: unit + integration con Testcontainers (quando ambiente supporta Maven/Testcontainers).

### Decide (MoSCoW)
- Must:
  - auth JWT + ruoli
  - CRUD auto
  - disponibilita
  - booking con prevenzione overbooking
  - pricing base + weekend + discount + insurance + dynamic surcharge
  - pagamento mock e conferma booking
  - manutenzione
  - audit trail
  - scheduler scadenza booking pending
  - Docker + Flyway + Swagger
- Should:
  - waitlist automatica
  - report top-rented/utilization
  - webhook mock pagamento
- Could:
  - coupon avanzati
  - notifiche reali email/SMS
  - rate limiter integrazioni terze parti
- Won't (ora):
  - multi-tenant completo
  - pagamento reale PSP
  - telemetria IoT

### Act
Implementazione inclusa in questo repository:
- struttura modulare per dominio
- entity, dto, service, repository, controller
- exception handler JSON standard
- validazioni Bean Validation + business rules
- Dockerfile + docker-compose
- Flyway schema/seed
- endpoint integrazione terze parti
- test unit/integration di base

## 2) PECx

### P (Problema)
Il noleggio auto reale non e solo CRUD: occorre gestire disponibilita temporale, concorrenza prenotazioni, manutenzioni, pagamenti incerti, pricing variabile.

### E (Espansione)
Funzionalita introdotte:
- pricing dinamico (+15% se disponibilita bassa)
- waitlist se slot non disponibile
- audit trail prenotazioni
- scheduler per booking pending scaduti
- webhook mock pagamento

### C (Critica)
Failure mode / anti-pattern evitati:
- overbooking causato da race condition
- business logic nei controller
- entita JPA esposte direttamente alle API
- mancata tracciabilita eventi booking

### x (Sintesi operativa)
Monolite modulare con confini per sottosistema; transazioni su booking; servizi separati per pricing/payment/maintenance; pronto a estrazione microservizi in v2/v3.

## 3) SYS - Architettura di sistema

### Sottosistemi
- `Identity & Access` (`identity`, `security`)
- `Fleet Management` (`fleet`)
- `Booking Engine` (`booking`)
- `Pricing Engine` (`pricing`)
- `Payment Simulation` (`payment`)
- `Maintenance & Availability` (`maintenance`, `fleet/availability`)
- `Audit/Logs` (`booking_audits`, SLF4J)
- `Notification stub` (`notification`)
- `Integration API` (`integration`, `payment webhook`)
- `Observability` (Actuator + Prometheus metrics)

### Diagramma testuale
```
[Client/Admin/Operator/Partner]
        |
        v
[REST Controllers] ---> [Security JWT]
        |
        +--> [Booking Service] ---> [Pricing Service]
        |           |               [Coupon Service]
        |           +--> [Payment Service] ---> [Webhook]
        |           +--> [Maintenance Check]
        |           +--> [Audit + Notification]
        |
        +--> [Fleet Service]
        +--> [Maintenance Service]
        +--> [Admin Report Service]
        |
        v
[PostgreSQL via JPA + Flyway]
        |
        v
[Actuator/Metrics]
```

## 4) Struttura progetto

```text
.
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── CHANGELOG.md
├── README.md
├── docs/
│   ├── checklist.md
│   ├── user-stories.md
│   ├── git-workflow.md
│   ├── local-ai-prompts.md
│   ├── engine-versions.md
│   ├── versioning-and-data-tests.md
│   ├── frontend-console.md
│   ├── database-access.md
│   └── scaffold-cli-guide.md
├── scripts/
│   ├── scaffold-cli.sh
│   ├── generate-single-file.sh
│   ├── run-versioning-checks.sh
│   └── run-data-smoke-tests.sh
└── src/
    ├── main/
    │   ├── java/com/smartautorental/platform/... (moduli backend + UiController)
    │   └── resources/
    │       ├── templates/index.html
    │       ├── static/assets/css/app.css
    │       ├── static/assets/js/*.js (frontend modules)
    │       ├── application.yml
    │       ├── application-docker.yml
    │       ├── application-test.yml
    │       └── db/migration/V1__init_schema.sql, V2__seed_data.sql
    └── test/java/com/smartautorental/platform/... (unit + integration)
```

## 5) Endpoints principali

### Auth
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`

### Fleet
- `POST /api/v1/cars` (`ADMIN`, `OPERATOR`)
- `PUT /api/v1/cars/{carId}` (`ADMIN`, `OPERATOR`)
- `GET /api/v1/cars/{carId}`
- `GET /api/v1/cars`
- `DELETE /api/v1/cars/{carId}` (`ADMIN`)

### Availability
- `GET /api/v1/availability?branchId&startTime&endTime&category`

### Booking
- `POST /api/v1/bookings`
- `GET /api/v1/bookings/{bookingId}`
- `GET /api/v1/bookings/me`
- `POST /api/v1/bookings/{bookingId}/cancel`
- `GET /api/v1/bookings/{bookingId}/audit`

### Payments
- `POST /api/v1/payments/{bookingId}/retry`
- `GET /api/v1/payments/{bookingId}/transactions`
- `POST /api/v1/integrations/payments/webhook`

### Maintenance
- `POST /api/v1/maintenance`
- `POST /api/v1/maintenance/{maintenanceId}/complete`
- `GET /api/v1/maintenance/car/{carId}`

### Admin Reports
- `GET /api/v1/admin/reports/top-rented`
- `GET /api/v1/admin/reports/utilization`

### Third-party Integration
- `GET /api/v1/integrations/availability` (`X-API-KEY`)
- `GET /api/v1/integrations/bookings/status` (`X-API-KEY`)

### UI
- `GET /` (dashboard grafica modulare)
- `GET /ui` (alias dashboard)

### Docs/Observability
- `GET /swagger-ui.html`
- `GET /v3/api-docs`
- `GET /actuator/health`
- `GET /actuator/prometheus`

## 6) Schema DB (estratto)

Tabelle:
- `users`
- `branches`
- `cars`
- `bookings`
- `payment_transactions`
- `maintenance_records`
- `waitlist_entries`
- `booking_audits`

Relazioni principali:
- `cars.branch_id -> branches.id`
- `bookings.customer_id -> users.id`
- `bookings.car_id -> cars.id`
- `bookings.branch_id -> branches.id`
- `payment_transactions.booking_id -> bookings.id`
- `maintenance_records.car_id -> cars.id`
- `waitlist_entries.customer_id -> users.id`

## 7) Avvio rapido

### Docker (consigliato)
```bash
docker compose up --build
```

Dashboard grafica:
- `http://localhost:8080/`
- guida frontend: `docs/frontend-console.md`

Database tools:
- `http://localhost:5050` (pgAdmin)
- `http://localhost:8081` (Adminer)
- guida DB: `docs/database-access.md`

### Locale
Prerequisiti: Java 21 + Maven 3.9+
```bash
mvn clean spring-boot:run
```

## 8) Demo utenti seed

Creati al bootstrap applicazione:
- `admin@smartauto.local / Admin123!`
- `operator@smartauto.local / Operator123!`
- `customer@smartauto.local / Customer123!`

Le stesse credenziali demo sono mostrate anche direttamente nella UI (`/`).

## 9) Esempi curl

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"customer@smartauto.local","password":"Customer123!"}'
```

### Ricerca disponibilita
```bash
curl -G "http://localhost:8080/api/v1/availability" \
  -H "Authorization: Bearer <TOKEN>" \
  --data-urlencode "branchId=1" \
  --data-urlencode "startTime=2026-03-02T10:00:00Z" \
  --data-urlencode "endTime=2026-03-05T10:00:00Z" \
  --data-urlencode "category=ECONOMY"
```

### Crea booking con pagamento forzato SUCCESS
```bash
curl -X POST http://localhost:8080/api/v1/bookings \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "carId": 1,
    "startTime": "2026-03-10T09:00:00Z",
    "endTime": "2026-03-12T09:00:00Z",
    "insuranceSelected": true,
    "couponCode": "WELCOME10",
    "payAtDesk": false,
    "allowWaitlist": true,
    "forcedPaymentStatus": "SUCCESS"
  }'
```

### Webhook pagamento
```bash
curl -X POST http://localhost:8080/api/v1/integrations/payments/webhook \
  -H "Content-Type: application/json" \
  -d '{"bookingId":1,"status":"SUCCESS","providerReference":"mock-ext-1"}'
```

## 10) Test strategy

- Unit test:
  - pricing formula (`PricingServiceTest`)
- Integration test (Testcontainers PostgreSQL):
  - ricerca disponibilita (`AvailabilityIntegrationTest`)
  - conflitto booking (`BookingConflictIntegrationTest`)
  - pagamento + conferma booking (`PaymentConfirmationIntegrationTest`)
- End-to-end test (MockMvc + JWT + PostgreSQL container):
  - flusso login -> availability -> booking (`BookingE2ETest`)
- UI test:
  - rendering dashboard (`UiControllerTest`)
- Script test di versionamento:
  - `./scripts/run-versioning-checks.sh`
- Script test dati su DB seed/migration:
  - `./scripts/run-data-smoke-tests.sh`

## 11) CLI scaffolding

- menu interattivo:
  - `./scripts/scaffold-cli.sh menu`
- comandi rapidi:
  - `./scripts/scaffold-cli.sh feature <feature_name>`
  - `./scripts/scaffold-cli.sh controller <feature_name> <name>`
  - `./scripts/scaffold-cli.sh service <feature_name> <name>`
  - `./scripts/scaffold-cli.sh dto <feature_name> <name> <request|response>`
  - `./scripts/scaffold-cli.sh api <feature_name> <resource_name>`
  - `./scripts/scaffold-cli.sh migration <description>`
- guida completa:
  - `docs/scaffold-cli-guide.md`

## 12) Roadmap evolutiva

### v2
- Redis lock/distributed cache
- promozione automatica waitlist -> booking draft
- notifiche email/SMS reali
- miglioramenti policy cancellazione

### v3
- gateway pagamenti reali
- multi-filiale avanzato e tariffazione per stagione
- telemetria veicoli (IoT) + predizione manutenzione
- split in microservizi (booking/payment/pricing)

---
Dettagli operativi aggiuntivi in `docs/`.
