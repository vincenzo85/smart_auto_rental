# Payment Core Integration

Questo backend espone API pagamenti ad alto livello e delega l'esecuzione al `payment core` esterno.

## 1) Responsabilita

### Questo servizio (orchestrator)
- valida ownership e stato prenotazione
- invoca il core esterno per inizializzazione/retry pagamento
- aggiorna `bookings.payment_status` e `bookings.status`
- salva storico su `payment_transactions`
- riceve webhook asincroni su `/api/v1/integrations/payments/webhook`

### Payment core esterno
- autorizzazione/capture del pagamento reale
- integrazione PSP
- antifrode e riconciliazione
- callback webhook verso questo backend

## 2) Configurazione

`application.yml`:

```yaml
app:
  payment-core:
    mode: stub        # stub | http
    base-url: "http://localhost:8090"
    charge-path: "/api/v1/core/payments/charge"
    api-key: ""
    connect-timeout-ms: 2000
    read-timeout-ms: 5000
```

- `stub`: client locale deterministico (utile per sviluppo e test)
- `http`: chiamata REST al payment core reale

## 3) Mock payment core in Docker

Con `docker compose up --build` parte anche un servizio mock separato:
- service: `payment-core` (WireMock)
- host: `http://localhost:8090`
- mapping: `docker/payment-core-mock/mappings/charge.json`

Verifica rapida:

```bash
curl -s http://localhost:8090/__admin/mappings | jq
```

Il profilo Docker dell'app usa gia `mode=http` verso `http://payment-core:8080`.

## 4) Contract verso payment core

Request `POST {base-url}{charge-path}`:

```json
{
  "bookingId": 123,
  "bookingCode": "BKG-AB12CD34",
  "amount": 189.90,
  "currency": "EUR",
  "attemptType": "INITIAL"
}
```

Response attesa:

```json
{
  "status": "SUCCESS",
  "providerReference": "core-9f3b2a"
}
```

Valori `status`: `SUCCESS`, `PENDING`, `FAILED`.

## 5) Passare dal mock al core reale

Puoi sostituire il mock senza cambiare codice, sovrascrivendo variabili env:

```bash
APP_PAYMENT_CORE_MODE=http \
APP_PAYMENT_CORE_BASE_URL=https://payment-core.company.tld \
APP_PAYMENT_CORE_CHARGE_PATH=/api/v1/core/payments/charge \
APP_PAYMENT_CORE_API_KEY=core-shared-key \
docker compose up -d app
```

Se usi il core reale, puoi spegnere il mock locale:

```bash
docker compose stop payment-core
```

## 6) API alto livello esposte

- `POST /api/v1/payments/{bookingId}/retry`
- `GET /api/v1/payments/{bookingId}/transactions`
- `POST /api/v1/integrations/payments/webhook`

Nota: il body di creazione booking non espone piu campi di test payment (es. forcing status).
