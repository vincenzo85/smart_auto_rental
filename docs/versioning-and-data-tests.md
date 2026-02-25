# Versioning e Data Tests

Questa guida definisce controlli minimi richiesti a ogni iterazione funzionale.

## 1) Test di versionamento

Script: `scripts/run-versioning-checks.sh`

Controlla:
- versione semver in `pom.xml`
- coerenza `pom.xml` <-> versione top in `CHANGELOG.md`
- presenza documentazione motori in `docs/engine-versions.md`
- esecuzione in repository git valido

Esecuzione:
```bash
./scripts/run-versioning-checks.sh
```

## 2) Test sui dati (smoke)

Script: `scripts/run-data-smoke-tests.sh`

Prerequisito:
- stack locale avviato (`docker compose up -d`)

Controlla:
- migration Flyway applicate
- seed minimo `branches/cars/users`
- assenza duplicati su `license_plate`

Esecuzione:
```bash
./scripts/run-data-smoke-tests.sh
```

## 3) Workflow consigliato per feature

1. sviluppa su `feature/*`
2. aggiorna checklist e docs
3. esegui:
   - `./scripts/run-versioning-checks.sh`
   - `./scripts/run-data-smoke-tests.sh`
4. merge in `dev`
5. release merge `dev -> main`
