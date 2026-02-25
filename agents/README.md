# Agents Quickstart (LangGraph + Ollama)

Questa cartella contiene template e un orchestratore API minimale per orchestrazione multi-agent.

## File inclusi
- `agent-registry.example.yaml`: configurazione agenti/scope/tool.
- `tool-manifest.example.json`: contratti function-calling lato dispatcher.
- `request.example.json`: payload esempio change/defect/test.
- `langgraph_skeleton.py`: scheletro orchestratore Python.
- `orchestrator_api.py`: API reale minimale (`FastAPI`) con flusso multi-agent.
- `requirements.txt`: dipendenze Python.
- `Dockerfile`: container runtime orchestratore.

## 1) Setup locale (esempio)

Prerequisiti:
- Python 3.11+
- Ollama avviato in locale

Install (ambiente virtuale):

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r agents/requirements.txt
pip install langchain langgraph langchain-ollama
```

## 2) Esecuzione orchestratore API

```bash
uvicorn agents.orchestrator_api:app --host 0.0.0.0 --port 8091
```

Health check:

```bash
curl -s http://localhost:8091/health | jq
```

`request.example.json` contiene solo l'oggetto richiesta, mentre l'API accetta:

```json
{
  "request": { "...": "..." },
  "mode": "stub",
  "dry_run": true
}
```

Esempio completo:

```bash
python3 - <<'PY' | curl -s -X POST http://localhost:8091/api/v1/agent-runs \\
  -H "Content-Type: application/json" \\
  -d @- | jq
import json
from pathlib import Path
req = json.loads(Path("agents/request.example.json").read_text())
print(json.dumps({"request": req, "mode": "stub", "dry_run": True}))
PY
```

## 3) Esecuzione skeleton LangGraph

```bash
python agents/langgraph_skeleton.py
```

## 4) Note operative

- Lo skeleton e volutamente minimale: collega solo alcuni tool demo.
- Sostituisci `your-coder-model` con il modello disponibile in Ollama.
- In `orchestrator_api.py`, modalita predefinita: `stub` (consigliata all'avvio).
- Per usare Ollama reale: imposta `AGENT_DEFAULT_MODE=ollama` e `OLLAMA_MODEL=<modello>`.
- Prima di produzione aggiungi:
  - autenticazione tool API
  - sandbox forte per command execution
  - audit log persistente
  - gate umano prima di commit/PR
