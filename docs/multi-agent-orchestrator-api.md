# Multi-Agent Orchestrator API

API operativa minimale per orchestrare agenti a scope ridotto.

File runtime:
- `agents/orchestrator_api.py`

## 1) Avvio locale

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r agents/requirements.txt
uvicorn agents.orchestrator_api:app --host 0.0.0.0 --port 8091
```

## 2) Avvio con Docker Compose

```bash
docker compose --profile agents up -d --build agent-orchestrator
```

Endpoint:
- `http://localhost:8091/health`
- `http://localhost:8091/api/v1/agent-runs`

## 3) Config env principali

- `AGENT_DEFAULT_MODE=stub|ollama`
- `OLLAMA_BASE_URL=http://localhost:11434`
- `OLLAMA_MODEL=qwen2.5-coder:14b`
- `REPO_ROOT=/path/to/repo`

## 4) Endpoints

### GET /health
Ritorna stato orchestratore e configurazione runtime base.

### POST /api/v1/agent-runs
Esegue un workflow completo:
- intake
- planner
- impact
- implementer
- tester
- verifier
- reviewer

Request:

```json
{
  "request": {
    "id": "CR-2026-0021",
    "type": "defect",
    "title": "Payment transactions visibility leak",
    "description": "A customer can query payment history of another booking",
    "risk_level": "high",
    "acceptance_criteria": [
      "Customer can read only own transactions"
    ],
    "scope": {
      "include_paths": ["src/main/java/com/smartautorental/platform/payment/**"],
      "exclude_paths": ["src/main/resources/static/**"]
    },
    "metadata": {}
  },
  "mode": "stub",
  "dry_run": true
}
```

Response: `RunReport` con elenco step e payload per agente.

### GET /api/v1/agent-runs/{run_id}
Ritorna il report salvato in-memory del run.

## 5) Tool gating

Tool disponibili in mode `ollama`:
- `repo_search`
- `read_file`
- `apply_patch`
- `run_command`

Vincoli:
- path sotto `REPO_ROOT`
- command allowlist (`test_unit`, `test_integration`, `test_regression`, `test_e2e`, `lint`)
- `dry_run=true` consigliato in ambienti shared

## 6) Sicurezza / governance

- nessun merge automatico su `main`
- usare policy branch `feature/*`
- mantenere gate umano pre-commit/pre-merge in produzione
- aggiungere auth API prima di esposizione su rete non trusted
