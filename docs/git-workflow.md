# Git Workflow e Continuous Commit

## Branch Strategy
- `main`: stabile e rilasciabile, no commit diretti
- `feature/<nome-feature>`: unico branch consentito per i commit di sviluppo

## Flusso consigliato
1. Creare branch `feature/booking-conflict-lock` da `main`.
2. Commit incrementali piccoli e descrittivi.
3. Push + PR verso `main` con checklist test.
4. Merge solo via PR approvata.

## Enforcement locale (feature-only)
Attiva gli hook locali:

```bash
./scripts/setup-git-hooks.sh
```

Hook applicati:
- `pre-commit`: blocca commit fuori da `feature/*`
- `pre-merge-commit`: blocca merge commit fuori da `feature/*`
- `pre-push`: blocca push dirette verso `main|master|dev|develop`

Override emergenza (sconsigliato):

```bash
ALLOW_PROTECTED_BRANCH_COMMIT=1 git commit -m "..."
```

## Enforcement remoto (consigliato)
Configura anche branch protection sul provider Git (GitHub/GitLab/Bitbucket):
- blocco push diretto su `main`
- merge solo via PR/MR
- review obbligatoria
- status check obbligatori prima del merge

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
- line ending coerenti gestiti da `.gitattributes`
- branch policy enforced via hook locali (`.githooks`)

## Gate minimi prima merge
- `./scripts/run-versioning-checks.sh`
- `./scripts/run-data-smoke-tests.sh`
