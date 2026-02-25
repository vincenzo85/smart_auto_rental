# Prompt efficaci per IA locali

## 1) Generazione endpoint
"Genera endpoint Spring Boot 3 per `POST /api/v1/bookings` con DTO separati, Bean Validation, gestione errori JSON standard, senza usare Entity nel controller."

## 2) Refactor service
"Refattorizza `BookingService` per ridurre complessita ciclomatica mantenendo behavior invariato. Estrai metodi puri per pricing e policy cancellazione; mantieni transazione su create/cancel."

## 3) Test integration
"Scrivi integration test con Testcontainers PostgreSQL per conflitto prenotazione: prima prenotazione confermata, seconda nello stesso intervallo deve restituire `CONFLICT` o `WAITLISTED` se flag attivo."

## 4) Sicurezza
"Rivedi SecurityConfig Spring Security 6: conferma regole role-based, endpoint pubblici minimi, filtro JWT in stateless mode, nessun fallback insicuro."

## 5) SQL migration review
"Analizza migration Flyway e segnala: indici mancanti, vincoli referenziali assenti, colonne nullable non intenzionali, problemi per query di disponibilita date-range."
