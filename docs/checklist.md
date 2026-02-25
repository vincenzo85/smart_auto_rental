# Delivery Checklist

## MVP Core
- [x] Java 21 + Spring Boot 3.x
- [x] PostgreSQL + Flyway migrations
- [x] Dockerfile + docker-compose
- [x] JWT auth con ruoli CUSTOMER/ADMIN/OPERATOR
- [x] CRUD auto
- [x] Ricerca disponibilita per sede/intervallo/categoria
- [x] Prenotazione con prevenzione conflitti
- [x] Pricing base + weekend + durata + assicurazione + dinamico
- [x] Simulazione pagamento SUCCESS/FAILED/PENDING
- [x] Conferma booking condizionata a pagamento
- [x] Cancellazione con policy base rimborso
- [x] Gestione manutenzione + storico
- [x] OpenAPI/Swagger
- [x] Seed iniziale dati demo
- [x] Dashboard grafica modulare (auth, availability, booking, admin report)
- [x] Seed esteso auto demo (>=10)

## Funzioni Interessanti
- [x] Pricing dinamico semplice
- [x] Prevenzione overbooking con lock transazionale
- [x] Waitlist quando non disponibile
- [x] Audit trail prenotazioni
- [x] Webhook mock pagamenti
- [x] Scheduler scadenza booking pending
- [x] Report admin top-rented e utilization

## Qualita
- [x] DTO separati da Entity
- [x] Exception handling JSON standard
- [x] Validazione input (Bean Validation + business rules)
- [x] Logging SLF4J
- [x] Profili local/docker/test
- [x] Actuator + Prometheus metrics
- [x] Unit test base
- [x] Integration test base
- [x] End-to-end test base (MockMvc)
- [x] UI smoke test (render template + module bootstrap)

## Processo/Governance
- [x] Documentazione README
- [x] User stories (utente/admin/programmatore)
- [x] Prompt efficaci per IA locali
- [x] Workflow branch + commit conventions
- [x] Changelog versionato
- [x] Engine versions documentate
- [x] Script test versionamento
- [x] Script test dati seed/migration
- [x] Documentazione frontend console

## Note TODO
- [ ] Promozione automatica waitlist in booking draft (attualmente notifica + fulfill)
- [ ] Hardening produzione (rate limiting, secret manager, RBAC avanzato)
