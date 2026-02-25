# Multi-Agent Architecture (Ollama + Function Calling)

Questa guida definisce un setup pragmatico per avere piu agenti con scope piccolo, orientati a:
- change request
- defect fixing
- test generation/regression

## 1) Obiettivo

Implementare un sistema AI locale che:
- usa modelli via Ollama
- usa function calling per tool deterministici
- separa responsabilita in agenti piccoli
- mantiene controllo umano su merge e rilascio

## 2) Stack consigliato

### Orchestrazione
- `LangGraph` (sopra LangChain) per workflow a stati e DAG agenti.
- alternativa: orchestratore custom con code JSON + dispatcher tool.

### Model runtime
- `Ollama` locale per inferenza.
- modello coding con supporto tool-calling/json robusto (valutare in preflight sul tuo hardware).

### Esecuzione tool
- worker isolato (container) con tool allowlist:
  - read/search file
  - apply patch
  - run test/lint
  - git branch/commit (mai merge automatico su `main`)

### Storage minimo
- Postgres (o SQLite in POC) per:
  - run log
  - stato task
  - audit decisioni agenti

## 3) Principio chiave: scope piccolo

Ogni agente deve avere:
- input limitato
- output schema fisso
- tool limitati
- limiti di path modificabili
- budget (max step/tool-call)

Regola: un agente non deve fare planning + coding + review insieme.

## 4) Ruoli agenti consigliati

### 4.1 Intake Agent
- scopo: classifica richiesta (`change_request`, `defect`, `test_request`).
- output: ticket normalizzato + priorita + rischio.
- tool: nessuno.

### 4.2 Planner Agent
- scopo: produce task DAG con acceptance criteria verificabili.
- output: piano JSON.
- tool: lettura issue/template.

### 4.3 Impact Agent
- scopo: mappa file impattati e dipendenze.
- output: file target + rischi regressione.
- tool: `repo_search`, `read_file`.

### 4.4 Implementer Agent
- scopo: modifica codice solo in scope approvato.
- output: patch + rationale breve.
- tool: `read_file`, `apply_patch`.

### 4.5 Test Agent
- scopo: crea/aggiorna test unit/integration/e2e per acceptance criteria.
- output: patch test + elenco casi coperti.
- tool: `read_file`, `apply_patch`.

### 4.6 Verifier Agent
- scopo: esegue test/lint e interpreta failure.
- output: verdict (`pass`, `fail`, `flaky`) + evidenze.
- tool: `run_command` (allowlist).

### 4.7 Review Agent
- scopo: controlli statici su sicurezza, regressione, design.
- output: findings severita alta/media/bassa.
- tool: `read_file`, `repo_search`.

## 5) Workflow target

## 5.1 Change request
1. Intake classifica e normalizza requisiti.
2. Planner genera task DAG.
3. Impact definisce file scope e rischi.
4. Implementer produce patch.
5. Test Agent aggiunge test.
6. Verifier esegue suite.
7. Review Agent valida qualitativamente.
8. Human gate: approvazione finale + merge PR.

## 5.2 Defect flow
1. Intake marca `defect` + severita.
2. Impact riproduce area/funzione impattata.
3. Implementer crea fix minimo.
4. Test Agent aggiunge regression test specifico bug.
5. Verifier conferma green.
6. Review Agent controlla side-effect.

## 5.3 Test-only flow
1. Intake marca `test_request`.
2. Planner crea matrice casi.
3. Test Agent implementa suite.
4. Verifier esegue e produce coverage delta.

## 6) Contratti function calling (raccomandati)

Tool minimi:
- `repo_search(query, glob)`
- `read_file(path, start_line, end_line)`
- `apply_patch(path, patch)`
- `run_command(command_id, args)`
- `git_create_branch(name)`
- `git_commit(message)`

`run_command` deve usare comandi pre-approvati (allowlist), es:
- `test_unit`
- `test_integration`
- `test_e2e`
- `lint`

Mai esporre shell arbitraria al modello.

## 7) Policy di governance

- branch di lavoro solo `feature/*`.
- nessun commit automatico su `main`.
- merge solo via PR/MR + review umana.
- ticket ad alto rischio: blocco auto-fix, solo suggerimento patch.
- secret redaction obbligatoria in prompt e log.

## 8) Metriche operative

Traccia per ogni run:
- lead time richiesta -> patch
- test pass rate
- rollback rate
- regressioni post-merge
- file touched per agente
- retry count per task

SLO iniziale consigliato:
- `>= 90%` run senza interventi manuali extra su task low-risk.

## 9) Rollout pratico (3 fasi)

### Fase 1 (manual-assisted)
- AI propone patch, umano applica/committa.
- target: validare prompt e tool schema.

### Fase 2 (semi-auto)
- AI applica patch su branch feature dedicato.
- umano approva commit e PR.

### Fase 3 (guarded-auto)
- AI apre PR automaticamente solo per classi low-risk.
- gate rigidi su test, security e review.

## 10) LangChain/LangGraph: pattern consigliato

- Usa state object condiviso con campi tipizzati:
  - `ticket`
  - `plan`
  - `impact`
  - `patches`
  - `test_results`
  - `review_findings`
- Ogni nodo aggiorna solo il suo subset di stato.
- Inserisci `human_approval` node prima di commit/PR.

## 11) Anti-pattern da evitare

- un solo agente "full stack" che fa tutto.
- output libero senza schema JSON.
- tool non deterministici o shell aperta.
- auto-merge senza quality gate.
- assenza di regression test obbligatorio nei defect fix.

## 12) Integrazione col tuo progetto attuale

Per questo repository:
- usa i test esistenti come gate (`scripts/run-regression-tests.sh`, `scripts/run-e2e-tests.sh`).
- mantieni enforcement branch feature-only gia presente (`.githooks`).
- usa la parte payment orchestration come primo dominio pilot per agenti (change+defect+test).
