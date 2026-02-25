# Agents Quickstart (LangGraph + Ollama)

Questa cartella contiene template per orchestrazione multi-agent.

## File inclusi
- `agent-registry.example.yaml`: configurazione agenti/scope/tool.
- `tool-manifest.example.json`: contratti function-calling lato dispatcher.
- `request.example.json`: payload esempio change/defect/test.
- `langgraph_skeleton.py`: scheletro orchestratore Python.

## 1) Setup locale (esempio)

Prerequisiti:
- Python 3.11+
- Ollama avviato in locale

Install (ambiente virtuale):

```bash
python -m venv .venv
source .venv/bin/activate
pip install langchain langgraph langchain-ollama pydantic
```

## 2) Esecuzione skeleton

```bash
python agents/langgraph_skeleton.py
```

## 3) Note operative

- Lo skeleton e volutamente minimale: collega solo alcuni tool demo.
- Sostituisci `your-coder-model` con il modello disponibile in Ollama.
- Prima di produzione aggiungi:
  - autenticazione tool API
  - sandbox forte per command execution
  - audit log persistente
  - gate umano prima di commit/PR
