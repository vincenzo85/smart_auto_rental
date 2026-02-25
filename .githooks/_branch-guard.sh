#!/usr/bin/env bash
set -euo pipefail

if [[ "${ALLOW_PROTECTED_BRANCH_COMMIT:-0}" == "1" ]]; then
  exit 0
fi

branch="$(git symbolic-ref --quiet --short HEAD 2>/dev/null || true)"
if [[ -z "$branch" ]]; then
  echo "[branch-guard] Detached HEAD non consentito. Crea un branch feature/* prima di committare."
  exit 1
fi

if [[ "$branch" != feature/* ]]; then
  cat <<EOF
[branch-guard] Commit bloccato su '$branch'.
Policy: il progetto accetta commit solo su branch 'feature/*'.

Esempio:
  git checkout -b feature/nome-feature
EOF
  exit 1
fi
