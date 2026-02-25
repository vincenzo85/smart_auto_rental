# Changelog

## [1.0.0] - 2026-02-25
### Added
- MVP Smart Auto Rental Platform (Spring Boot monolite modulare).
- JWT auth con ruoli CUSTOMER/ADMIN/OPERATOR.
- Fleet CRUD + availability search.
- Booking engine con lock pessimista e controllo conflitti.
- Pricing engine (base, weekend, durata, insurance, dynamic surcharge, coupon base).
- Payment orchestration via external core adapter (`stub`/`http`) + retry + webhook.
- Waitlist e audit trail booking.
- Maintenance scheduling/history.
- Admin reports (top rented, utilization).
- Scheduler per scadenza prenotazioni pending.
- OpenAPI/Swagger, Actuator e metriche Prometheus.
- Flyway migrations + seed iniziale.
- Dockerfile + docker-compose (app, postgres, pgadmin).
- Test unit/integration iniziali.
- Script locali per test di versionamento e data smoke.
- Dashboard grafica modulare su `/` e `/ui`.
- UI controller test per rendering template.
- Seed flotta esteso con auto aggiuntive (`V3__add_more_seed_cars.sql`).
- Migliorata UX delle `select` frontend (alto contrasto).
- Credenziali demo mostrate in dashboard e guida frontend dedicata.
- Aggiunto Adminer in Docker (`http://localhost:8081`) come UI DB equivalente a phpMyAdmin.
- Documentazione DB access (`docs/database-access.md`).
- Script scaffolding CLI con menu per generazione componenti standard (`scripts/scaffold-cli.sh`).
- Aggiunte suite scriptate `run-e2e-tests.sh` e `run-regression-tests.sh`.
- Guida dedicata test E2E/regressione (`docs/testing-e2e-regression.md`).
- Aggiunto servizio Docker `payment-core` mock separato (WireMock) e profilo Docker app in `mode=http`.
- Aggiunto enforcement Git feature-branch-only con hook locali (`.githooks`) e setup script (`scripts/setup-git-hooks.sh`).
- Aggiunto blueprint multi-agent AI con Ollama + function calling (`docs/multi-agent-ollama-architecture.md`, `agents/*`, `docs/schemas/*`).

### Notes
- Build/test locali richiedono Maven installato o container build.
