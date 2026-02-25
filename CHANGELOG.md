# Changelog

## [1.0.0] - 2026-02-25
### Added
- MVP Smart Auto Rental Platform (Spring Boot monolite modulare).
- JWT auth con ruoli CUSTOMER/ADMIN/OPERATOR.
- Fleet CRUD + availability search.
- Booking engine con lock pessimista e controllo conflitti.
- Pricing engine (base, weekend, durata, insurance, dynamic surcharge, coupon base).
- Payment simulation + retry + webhook mock.
- Waitlist e audit trail booking.
- Maintenance scheduling/history.
- Admin reports (top rented, utilization).
- Scheduler per scadenza prenotazioni pending.
- OpenAPI/Swagger, Actuator e metriche Prometheus.
- Flyway migrations + seed iniziale.
- Dockerfile + docker-compose (app, postgres, pgadmin).
- Test unit/integration iniziali.

### Notes
- Build/test locali richiedono Maven installato o container build.
