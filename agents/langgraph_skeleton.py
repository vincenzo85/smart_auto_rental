#!/usr/bin/env python3
"""
Minimal multi-agent skeleton with LangGraph + Ollama + function-calling.

Adapt imports and APIs to your installed versions.
"""

from __future__ import annotations

from typing import Any, Dict, List, TypedDict

from langchain_ollama import ChatOllama
from langchain_core.tools import tool
from langgraph.graph import START, END, StateGraph


class AgentState(TypedDict, total=False):
    request: Dict[str, Any]
    classification: Dict[str, Any]
    plan: Dict[str, Any]
    impact: Dict[str, Any]
    implementation: Dict[str, Any]
    tests: Dict[str, Any]
    verification: Dict[str, Any]
    review: Dict[str, Any]


# ---------------------------
# Tooling layer (demo stubs)
# ---------------------------

@tool
def repo_search(query: str, glob: str = "") -> str:
    """Search source code and return matches."""
    return f"repo_search(query={query}, glob={glob})"


@tool
def read_file(path: str, start_line: int = 1, end_line: int = 300) -> str:
    """Read file content from repository."""
    return f"read_file(path={path}, start_line={start_line}, end_line={end_line})"


@tool
def apply_patch(path: str, patch: str) -> str:
    """Apply patch on a file."""
    return f"apply_patch(path={path}, patch_len={len(patch)})"


@tool
def run_command(command_id: str, args: List[str] | None = None) -> str:
    """Run only allowlisted commands (test_unit/test_regression/test_e2e/lint)."""
    args = args or []
    allowlist = {"test_unit", "test_integration", "test_regression", "test_e2e", "lint"}
    if command_id not in allowlist:
        raise ValueError(f"Command not allowed: {command_id}")
    return f"run_command(command_id={command_id}, args={args})"


TOOLS = [repo_search, read_file, apply_patch, run_command]


# ---------------------------
# Model instances
# ---------------------------

# Replace with an available local model, e.g. "qwen2.5-coder:14b"
BASE_MODEL = "your-coder-model"

planner_llm = ChatOllama(model=BASE_MODEL, temperature=0)
impact_llm = ChatOllama(model=BASE_MODEL, temperature=0)
impl_llm = ChatOllama(model=BASE_MODEL, temperature=0)
test_llm = ChatOllama(model=BASE_MODEL, temperature=0)
review_llm = ChatOllama(model=BASE_MODEL, temperature=0)


# ---------------------------
# Agent nodes
# ---------------------------

def intake_node(state: AgentState) -> AgentState:
    request = state["request"]
    prompt = (
        "Classify request type and risk. Output strict JSON with keys: "
        "type, risk_level, summary.\n"
        f"request={request}"
    )
    out = planner_llm.invoke(prompt)
    return {"classification": {"raw": str(out)}}


def planner_node(state: AgentState) -> AgentState:
    prompt = (
        "Create task DAG for this request. Output strict JSON with keys: "
        "tasks[], acceptance_checks[].\n"
        f"request={state['request']}\nclassification={state.get('classification')}"
    )
    out = planner_llm.invoke(prompt)
    return {"plan": {"raw": str(out)}}


def impact_node(state: AgentState) -> AgentState:
    # Example direct tool call + LLM reasoning
    _ = repo_search.invoke({"query": "payment", "glob": "src/main/**"})
    prompt = (
        "Identify impacted files and regression risks. Output JSON keys: "
        "files[], risks[].\n"
        f"plan={state.get('plan')}"
    )
    out = impact_llm.invoke(prompt)
    return {"impact": {"raw": str(out)}}


def implementer_node(state: AgentState) -> AgentState:
    prompt = (
        "Produce minimal patch for requested change. Output JSON keys: "
        "patches[]. Each patch has path and unified_diff.\n"
        f"impact={state.get('impact')}"
    )
    out = impl_llm.invoke(prompt)
    return {"implementation": {"raw": str(out)}}


def tester_node(state: AgentState) -> AgentState:
    prompt = (
        "Generate test updates for acceptance criteria. Output JSON keys: "
        "test_patches[], cases[].\n"
        f"plan={state.get('plan')}\nimplementation={state.get('implementation')}"
    )
    out = test_llm.invoke(prompt)
    return {"tests": {"raw": str(out)}}


def verifier_node(state: AgentState) -> AgentState:
    # In production, call mapped commands only
    _ = run_command.invoke({"command_id": "test_regression", "args": []})
    _ = run_command.invoke({"command_id": "test_e2e", "args": []})
    return {"verification": {"status": "simulated_pass"}}


def reviewer_node(state: AgentState) -> AgentState:
    prompt = (
        "Review changes for defects/regressions/security. Output JSON keys: "
        "findings[], verdict.\n"
        f"implementation={state.get('implementation')}\nverification={state.get('verification')}"
    )
    out = review_llm.invoke(prompt)
    return {"review": {"raw": str(out)}}


# ---------------------------
# Graph wiring
# ---------------------------

def build_graph():
    graph = StateGraph(AgentState)
    graph.add_node("intake", intake_node)
    graph.add_node("planner", planner_node)
    graph.add_node("impact", impact_node)
    graph.add_node("implementer", implementer_node)
    graph.add_node("tester", tester_node)
    graph.add_node("verifier", verifier_node)
    graph.add_node("reviewer", reviewer_node)

    graph.add_edge(START, "intake")
    graph.add_edge("intake", "planner")
    graph.add_edge("planner", "impact")
    graph.add_edge("impact", "implementer")
    graph.add_edge("implementer", "tester")
    graph.add_edge("tester", "verifier")
    graph.add_edge("verifier", "reviewer")
    graph.add_edge("reviewer", END)

    return graph.compile()


if __name__ == "__main__":
    app = build_graph()
    initial_state: AgentState = {
        "request": {
            "id": "CR-POC-001",
            "type": "defect",
            "title": "Sample defect",
            "description": "Build regression test and minimal fix",
            "risk_level": "medium",
            "acceptance_criteria": ["test fails before fix", "test passes after fix"]
        }
    }

    result = app.invoke(initial_state)
    print("Workflow completed.")
    print(result)
