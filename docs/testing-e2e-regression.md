# E2E & Regression Testing Guide

Questa guida definisce due suite separate:

- `scripts/run-e2e-tests.sh`: test end-to-end di flusso completo
- `scripts/run-regression-tests.sh`: test regressione API/UI e regole business

## Prerequisiti
- stack avviato: `docker compose up -d`
- API raggiungibile su `http://localhost:8080`
- tool locali: `curl`, `jq`

## 1) E2E suite

Esecuzione:
```bash
./scripts/run-e2e-tests.sh
```

Copertura:
1. login customer/admin
2. ricerca disponibilita
3. creazione booking confermato
4. verifica `bookings/me`
5. storico transazioni pagamento
6. report admin
7. endpoint integrazione con API key
8. reachability UI + Swagger

## 2) Regression suite

Esecuzione:
```bash
./scripts/run-regression-tests.sh
```

Copertura:
1. availability UI/Swagger
2. invalid login -> `401`
3. endpoint availability senza token -> `401`
4. booking nel passato -> `400`
5. conflitto prenotazioni -> `409`
6. audit trail disponibile
7. endpoint payment transactions disponibile

## CI/local workflow consigliato
```bash
./scripts/run-versioning-checks.sh
./scripts/run-data-smoke-tests.sh
./scripts/run-regression-tests.sh
./scripts/run-e2e-tests.sh
```

## Variabili opzionali
- `BASE_URL` (default: `http://localhost:8080`)
- `INTEGRATION_KEY` (default: `integration-docker-key`, usata in E2E)
