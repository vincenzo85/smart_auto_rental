#!/usr/bin/env python3
from __future__ import annotations

import fnmatch
import json
import os
import re
import subprocess
import uuid
from datetime import datetime, timezone
from pathlib import Path
from typing import Any, Callable, Dict, List, Literal, Optional

import requests
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field


# ---------------------------
# Config
# ---------------------------

REPO_ROOT = Path(os.getenv("REPO_ROOT", Path(__file__).resolve().parents[1]))
OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "qwen2.5-coder:14b")
DEFAULT_MODE: Literal["stub", "ollama"] = os.getenv("AGENT_DEFAULT_MODE", "stub")  # type: ignore[assignment]


# ---------------------------
# Schemas
# ---------------------------


class RequestScope(BaseModel):
    include_paths: List[str] = Field(default_factory=list)
    exclude_paths: List[str] = Field(default_factory=list)


class AgentRequest(BaseModel):
    id: str
    type: Literal["change_request", "defect", "test_request"]
    title: str
    description: str
    risk_level: Literal["low", "medium", "high", "critical"]
    acceptance_criteria: List[str]
    scope: Optional[RequestScope] = None
    metadata: Dict[str, Any] = Field(default_factory=dict)


class RunInput(BaseModel):
    request: AgentRequest
    mode: Literal["stub", "ollama"] = DEFAULT_MODE
    dry_run: bool = True


class AgentStep(BaseModel):
    agent_id: str
    status: Literal["ok", "blocked", "needs_human"]
    summary: str
    payload: Dict[str, Any] = Field(default_factory=dict)


class RunReport(BaseModel):
    run_id: str
    status: Literal["completed", "failed", "needs_human"]
    mode: Literal["stub", "ollama"]
    dry_run: bool
    created_at: str
    request: AgentRequest
    steps: List[AgentStep]


# ---------------------------
# Tooling
# ---------------------------


class ToolContext:
    def __init__(self, repo_root: Path, dry_run: bool):
        self.repo_root = repo_root
        self.dry_run = dry_run


def _safe_path(path: str, repo_root: Path) -> Path:
    target = (repo_root / path).resolve()
    if repo_root not in target.parents and target != repo_root:
        raise ValueError(f"Path out of repo root: {path}")
    return target


def _is_allowed_by_scope(path: str, scope: Optional[RequestScope]) -> bool:
    if scope is None:
        return True

    include_ok = True
    if scope.include_paths:
        include_ok = any(fnmatch.fnmatch(path, pattern) for pattern in scope.include_paths)

    exclude_hit = any(fnmatch.fnmatch(path, pattern) for pattern in scope.exclude_paths)
    return include_ok and not exclude_hit


def tool_repo_search(query: str, glob: str = "") -> Dict[str, Any]:
    cmd = ["rg", "-n", query, str(REPO_ROOT)]
    if glob:
        cmd = ["rg", "-n", "-g", glob, query, str(REPO_ROOT)]

    proc = subprocess.run(cmd, capture_output=True, text=True)
    out = (proc.stdout or "")[:12000]
    err = (proc.stderr or "")[:12000]
    return {
        "exit_code": proc.returncode,
        "stdout": out,
        "stderr": err,
    }


def tool_read_file(path: str, start_line: int = 1, end_line: int = 300) -> Dict[str, Any]:
    target = _safe_path(path, REPO_ROOT)
    if not target.exists():
        return {"error": f"File not found: {path}"}

    text = target.read_text(encoding="utf-8", errors="ignore").splitlines()
    start = max(1, start_line)
    end = min(max(start, end_line), len(text))
    chunk = "\n".join(text[start - 1 : end])
    return {
        "path": path,
        "start_line": start,
        "end_line": end,
        "content": chunk,
    }


def tool_apply_patch(patch: str, ctx: ToolContext) -> Dict[str, Any]:
    if not patch.strip():
        return {"error": "Empty patch"}

    cmd = ["git", "apply", "--check", "-"] if ctx.dry_run else ["git", "apply", "-"]
    proc = subprocess.run(
        cmd,
        cwd=str(ctx.repo_root),
        input=patch,
        capture_output=True,
        text=True,
    )

    return {
        "dry_run": ctx.dry_run,
        "exit_code": proc.returncode,
        "stdout": (proc.stdout or "")[:12000],
        "stderr": (proc.stderr or "")[:12000],
    }


def tool_run_command(command_id: str, ctx: ToolContext) -> Dict[str, Any]:
    # Hard allowlist only.
    command_map: Dict[str, List[str]] = {
        "test_unit": ["bash", "-lc", "echo 'test_unit not configured'; exit 0"],
        "test_integration": ["bash", "-lc", "echo 'test_integration not configured'; exit 0"],
        "test_regression": ["./scripts/run-regression-tests.sh"],
        "test_e2e": ["./scripts/run-e2e-tests.sh"],
        "lint": ["bash", "-lc", "echo 'lint not configured'; exit 0"],
    }

    if command_id not in command_map:
        return {"error": f"Command not allowed: {command_id}"}

    cmd = command_map[command_id]
    proc = subprocess.run(cmd, cwd=str(ctx.repo_root), capture_output=True, text=True)
    return {
        "command_id": command_id,
        "exit_code": proc.returncode,
        "stdout": (proc.stdout or "")[:12000],
        "stderr": (proc.stderr or "")[:12000],
    }


TOOL_SPECS = [
    {
        "type": "function",
        "function": {
            "name": "repo_search",
            "description": "Search repository for code/text",
            "parameters": {
                "type": "object",
                "properties": {
                    "query": {"type": "string"},
                    "glob": {"type": "string"},
                },
                "required": ["query"],
                "additionalProperties": False,
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "read_file",
            "description": "Read file content with line range",
            "parameters": {
                "type": "object",
                "properties": {
                    "path": {"type": "string"},
                    "start_line": {"type": "integer", "minimum": 1},
                    "end_line": {"type": "integer", "minimum": 1},
                },
                "required": ["path"],
                "additionalProperties": False,
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "apply_patch",
            "description": "Apply unified diff patch. In dry-run mode validates only.",
            "parameters": {
                "type": "object",
                "properties": {
                    "patch": {"type": "string"},
                },
                "required": ["patch"],
                "additionalProperties": False,
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "run_command",
            "description": "Run allowlisted command id",
            "parameters": {
                "type": "object",
                "properties": {
                    "command_id": {
                        "type": "string",
                        "enum": ["test_unit", "test_integration", "test_regression", "test_e2e", "lint"],
                    }
                },
                "required": ["command_id"],
                "additionalProperties": False,
            },
        },
    },
]


def _parse_json_object(text: str) -> Dict[str, Any]:
    text = text.strip()
    if not text:
        return {}

    try:
        return json.loads(text)
    except json.JSONDecodeError:
        # Fallback: extract first JSON object block.
        match = re.search(r"\{[\s\S]*\}", text)
        if not match:
            return {"raw": text}
        try:
            return json.loads(match.group(0))
        except json.JSONDecodeError:
            return {"raw": text}


def _ollama_chat(
    messages: List[Dict[str, Any]],
    tools: Optional[List[Dict[str, Any]]] = None,
) -> Dict[str, Any]:
    payload: Dict[str, Any] = {
        "model": OLLAMA_MODEL,
        "messages": messages,
        "stream": False,
    }
    if tools:
        payload["tools"] = tools

    resp = requests.post(f"{OLLAMA_BASE_URL}/api/chat", json=payload, timeout=120)
    if resp.status_code >= 400:
        raise RuntimeError(f"Ollama error {resp.status_code}: {resp.text[:400]}")
    return resp.json()


def _execute_tool_call(tool_call: Dict[str, Any], ctx: ToolContext) -> Dict[str, Any]:
    fn = tool_call.get("function", {})
    name = fn.get("name")
    args = fn.get("arguments") or {}

    if isinstance(args, str):
        try:
            args = json.loads(args)
        except json.JSONDecodeError:
            args = {}

    if name == "repo_search":
        return tool_repo_search(query=args.get("query", ""), glob=args.get("glob", ""))
    if name == "read_file":
        return tool_read_file(
            path=args.get("path", ""),
            start_line=int(args.get("start_line", 1)),
            end_line=int(args.get("end_line", 300)),
        )
    if name == "apply_patch":
        return tool_apply_patch(patch=args.get("patch", ""), ctx=ctx)
    if name == "run_command":
        return tool_run_command(command_id=args.get("command_id", ""), ctx=ctx)

    return {"error": f"Unknown tool: {name}"}


def run_llm_agent(
    system_prompt: str,
    user_payload: Dict[str, Any],
    ctx: ToolContext,
    enable_tools: bool,
) -> Dict[str, Any]:
    messages: List[Dict[str, Any]] = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": json.dumps(user_payload, ensure_ascii=False)},
    ]

    tools = TOOL_SPECS if enable_tools else None

    for _ in range(6):
        response = _ollama_chat(messages, tools=tools)
        message = response.get("message", {})
        messages.append(message)

        tool_calls = message.get("tool_calls") or []
        if not tool_calls:
            content = message.get("content", "")
            return _parse_json_object(content)

        for call in tool_calls:
            result = _execute_tool_call(call, ctx=ctx)
            messages.append(
                {
                    "role": "tool",
                    "name": call.get("function", {}).get("name", "tool"),
                    "content": json.dumps(result, ensure_ascii=False),
                }
            )

    return {"error": "Max tool-call rounds reached"}


# ---------------------------
# Orchestrator
# ---------------------------


class AgentOrchestrator:
    def __init__(self, repo_root: Path):
        self.repo_root = repo_root

    def run(self, payload: RunInput) -> RunReport:
        ctx = ToolContext(repo_root=self.repo_root, dry_run=payload.dry_run)
        steps: List[AgentStep] = []

        if payload.mode == "stub":
            steps.extend(self._run_stub(payload))
        else:
            steps.extend(self._run_ollama(payload, ctx))

        final_status: Literal["completed", "failed", "needs_human"] = "completed"
        if any(step.status == "blocked" for step in steps):
            final_status = "failed"
        elif any(step.status == "needs_human" for step in steps):
            final_status = "needs_human"

        return RunReport(
            run_id=str(uuid.uuid4()),
            status=final_status,
            mode=payload.mode,
            dry_run=payload.dry_run,
            created_at=datetime.now(timezone.utc).isoformat(),
            request=payload.request,
            steps=steps,
        )

    def _run_stub(self, payload: RunInput) -> List[AgentStep]:
        req = payload.request
        return [
            AgentStep(
                agent_id="intake",
                status="ok",
                summary="Request classified",
                payload={"type": req.type, "risk_level": req.risk_level},
            ),
            AgentStep(
                agent_id="planner",
                status="ok",
                summary="Plan generated",
                payload={
                    "tasks": [
                        "impact_analysis",
                        "implementation",
                        "test_generation",
                        "verification",
                        "review",
                    ],
                    "acceptance_criteria": req.acceptance_criteria,
                },
            ),
            AgentStep(
                agent_id="impact",
                status="ok",
                summary="Impact analysis completed",
                payload={"files": req.scope.include_paths if req.scope else []},
            ),
            AgentStep(
                agent_id="implementer",
                status="needs_human",
                summary="Patch proposal produced in stub mode",
                payload={"note": "Switch mode=ollama to execute tool-calling flow"},
            ),
            AgentStep(
                agent_id="tester",
                status="ok",
                summary="Test strategy generated",
                payload={"cases": req.acceptance_criteria},
            ),
            AgentStep(
                agent_id="verifier",
                status="ok",
                summary="Verification simulated",
                payload={"dry_run": payload.dry_run},
            ),
            AgentStep(
                agent_id="reviewer",
                status="ok",
                summary="Review completed",
                payload={"findings": []},
            ),
        ]

    def _run_ollama(self, payload: RunInput, ctx: ToolContext) -> List[AgentStep]:
        req = payload.request

        intake_out = run_llm_agent(
            system_prompt=(
                "You are Intake Agent. Return ONLY JSON with keys: type, risk_level, summary. "
                "Do not use tools."
            ),
            user_payload=req.model_dump(),
            ctx=ctx,
            enable_tools=False,
        )

        planner_out = run_llm_agent(
            system_prompt=(
                "You are Planner Agent. Return ONLY JSON with keys: tasks (array), acceptance_checks (array). "
                "Do not use tools."
            ),
            user_payload={"request": req.model_dump(), "intake": intake_out},
            ctx=ctx,
            enable_tools=False,
        )

        impact_out = run_llm_agent(
            system_prompt=(
                "You are Impact Agent. Use tools repo_search/read_file if needed. "
                "Return ONLY JSON with keys: files (array), risks (array)."
            ),
            user_payload={"request": req.model_dump(), "plan": planner_out},
            ctx=ctx,
            enable_tools=True,
        )

        impl_out = run_llm_agent(
            system_prompt=(
                "You are Implementer Agent. If a valid unified diff is available you may call apply_patch. "
                "Respect request scope. Return ONLY JSON with keys: changes (array), notes (array)."
            ),
            user_payload={
                "request": req.model_dump(),
                "plan": planner_out,
                "impact": impact_out,
                "dry_run": payload.dry_run,
            },
            ctx=ctx,
            enable_tools=True,
        )

        test_out = run_llm_agent(
            system_prompt=(
                "You are Test Agent. Generate/update tests. You may use repo_search/read_file/apply_patch. "
                "Return ONLY JSON with keys: tests_changed (array), cases (array)."
            ),
            user_payload={"request": req.model_dump(), "implementation": impl_out},
            ctx=ctx,
            enable_tools=True,
        )

        verify_out = run_llm_agent(
            system_prompt=(
                "You are Verifier Agent. Call run_command with allowlisted IDs when useful. "
                "Return ONLY JSON with keys: verdict, checks (array), failures (array)."
            ),
            user_payload={"request": req.model_dump(), "test_changes": test_out},
            ctx=ctx,
            enable_tools=True,
        )

        review_out = run_llm_agent(
            system_prompt=(
                "You are Review Agent. Return ONLY JSON with keys: findings (array), verdict. "
                "Do not use tools."
            ),
            user_payload={
                "request": req.model_dump(),
                "implementation": impl_out,
                "verification": verify_out,
            },
            ctx=ctx,
            enable_tools=False,
        )

        steps: List[AgentStep] = [
            AgentStep(agent_id="intake", status="ok", summary="Intake completed", payload=intake_out),
            AgentStep(agent_id="planner", status="ok", summary="Planning completed", payload=planner_out),
            AgentStep(agent_id="impact", status="ok", summary="Impact completed", payload=impact_out),
            AgentStep(agent_id="implementer", status="ok", summary="Implementation step completed", payload=impl_out),
            AgentStep(agent_id="tester", status="ok", summary="Testing step completed", payload=test_out),
            AgentStep(agent_id="verifier", status="ok", summary="Verification step completed", payload=verify_out),
            AgentStep(agent_id="reviewer", status="ok", summary="Review step completed", payload=review_out),
        ]

        # Scope guard: if model proposes out-of-scope files, mark needs_human.
        scope = req.scope
        if scope:
            touched: List[str] = []
            for key in ["files", "changes", "tests_changed"]:
                for source in [impact_out, impl_out, test_out]:
                    value = source.get(key)
                    if isinstance(value, list):
                        touched.extend([str(item) for item in value if isinstance(item, str)])

            invalid = [p for p in touched if not _is_allowed_by_scope(p, scope)]
            if invalid:
                steps.append(
                    AgentStep(
                        agent_id="scope_guard",
                        status="needs_human",
                        summary="Out-of-scope paths detected",
                        payload={"invalid_paths": invalid},
                    )
                )

        return steps


# ---------------------------
# API
# ---------------------------


app = FastAPI(title="SmartAuto Multi-Agent Orchestrator", version="0.1.0")
orchestrator = AgentOrchestrator(repo_root=REPO_ROOT)
RUNS: Dict[str, RunReport] = {}


@app.get("/health")
def health() -> Dict[str, Any]:
    return {
        "status": "UP",
        "repo_root": str(REPO_ROOT),
        "ollama_base_url": OLLAMA_BASE_URL,
        "default_mode": DEFAULT_MODE,
    }


@app.post("/api/v1/agent-runs", response_model=RunReport)
def create_run(payload: RunInput) -> RunReport:
    try:
        report = orchestrator.run(payload)
    except Exception as ex:  # pragma: no cover - runtime boundary
        raise HTTPException(status_code=500, detail=f"Orchestrator error: {ex}") from ex

    RUNS[report.run_id] = report
    return report


@app.get("/api/v1/agent-runs/{run_id}", response_model=RunReport)
def get_run(run_id: str) -> RunReport:
    report = RUNS.get(run_id)
    if report is None:
        raise HTTPException(status_code=404, detail="Run not found")
    return report
