# Scaffold CLI Guide

Script: `scripts/scaffold-cli.sh`

Obiettivo: generare componenti standard (feature/controller/service/dto/api/migration) con naming e path coerenti.

## Modalita menu (interattiva)
```bash
./scripts/scaffold-cli.sh menu
```

Menu disponibile:
1. Create feature skeleton
2. Create controller
3. Create service
4. Create DTO request
5. Create DTO response
6. Create API bundle
7. Create Flyway migration

## Modalita comandi diretti
```bash
./scripts/scaffold-cli.sh feature <feature_name>
./scripts/scaffold-cli.sh controller <feature_name> <name>
./scripts/scaffold-cli.sh service <feature_name> <name>
./scripts/scaffold-cli.sh dto <feature_name> <name> <request|response>
./scripts/scaffold-cli.sh api <feature_name> <resource_name>
./scripts/scaffold-cli.sh migration <description>
```

## Esempi pratici
```bash
# crea modulo coupon con cartelle standard
./scripts/scaffold-cli.sh feature coupon

# crea service/controller/dto singoli
./scripts/scaffold-cli.sh service coupon Coupon
./scripts/scaffold-cli.sh controller coupon Coupon
./scripts/scaffold-cli.sh dto coupon Coupon request
./scripts/scaffold-cli.sh dto coupon Coupon response

# crea bundle completo in un colpo
./scripts/scaffold-cli.sh api coupon Coupon

# crea migration V4__add_coupon_tables.sql (numero automatico)
./scripts/scaffold-cli.sh migration add_coupon_tables
```

## Output generato (pattern)
- Java: `src/main/java/com/smartautorental/platform/<feature>/...`
- Migration: `src/main/resources/db/migration/V<next>__<description>.sql`

## Note
- Lo script non sovrascrive file esistenti.
- I nomi vengono normalizzati (`kebab-case`/`snake_case` -> formati Java).
- Dopo lo scaffold: completare business logic, test e aggiornare checklist/documentazione.
