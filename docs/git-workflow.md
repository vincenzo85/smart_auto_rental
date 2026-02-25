# Git Workflow e Continuous Commit

## Branch Strategy
- `main`: stabile e rilasciabile
- `dev`: integrazione continua funzionalita validate
- `feature/<nome-feature>`: sviluppo verticale per singola feature

## Flusso consigliato
1. Creare branch `feature/booking-conflict-lock` da `dev`.
2. Commit incrementali piccoli e descrittivi.
3. Push + PR verso `dev` con checklist test.
4. Merge `dev -> main` solo per release.

## Commit message pattern
- `feat(booking): add conflict detection with pessimistic lock`
- `feat(pricing): introduce dynamic surcharge when low availability`
- `test(booking): cover payment success and conflict scenarios`
- `docs(readme): update endpoint and curl examples`
- `chore(docker): add pgadmin service`

## Policy review
- almeno 1 review tecnica
- test verdi su branch PR
- nessun merge senza migration/documentazione aggiornate

## Gate minimi prima merge
- `./scripts/run-versioning-checks.sh`
- `./scripts/run-data-smoke-tests.sh`
