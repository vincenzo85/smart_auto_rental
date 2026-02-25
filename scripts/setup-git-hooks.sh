#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

git config core.hooksPath .githooks
chmod +x .githooks/_branch-guard.sh .githooks/pre-commit .githooks/pre-merge-commit .githooks/pre-push

echo "Git hooks attivati: core.hooksPath=.githooks"
echo "Policy attiva: commit consentiti solo su branch feature/*"
