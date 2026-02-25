# Frontend Console Guide

Dashboard disponibile su:
- `http://localhost:8080/`
- `http://localhost:8080/ui`

## Credenziali demo (visibili anche in UI)
- `admin@smartauto.local` / `Admin123!` (ADMIN)
- `operator@smartauto.local` / `Operator123!` (OPERATOR)
- `customer@smartauto.local` / `Customer123!` (CUSTOMER)

## Cosa puoi fare dalla UI
1. Login/logout JWT.
2. Ricerca disponibilita per sede/intervallo/categoria.
3. Selezione auto dai risultati.
4. Creazione prenotazione (assicurazione, coupon, waitlist, pay-at-desk).
5. Visualizzazione prenotazioni personali.
6. Report admin (solo ruolo ADMIN).

## Note UX
- Le `select` usano stile custom ad alto contrasto per migliorare leggibilita su desktop/mobile.
- UI responsive: layout a colonne su desktop, stack su mobile.

## Troubleshooting
- Se non vedi dati: verifica login e token attivo.
- Se i report admin non caricano: usa utente `ADMIN`.
- Se la pagina non risponde: controlla `docker compose ps` e `http://localhost:8080/actuator/health`.
