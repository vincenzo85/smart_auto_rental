#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

fail() {
  echo "[FAIL] $1" >&2
  exit 1
}

pass() {
  echo "[PASS] $1"
}

pom_version=$(awk '
  /<artifactId>smart-auto-rental-platform<\/artifactId>/ { target=1 }
  target && /<version>/ {
    gsub(/.*<version>|<\/version>.*/, "")
    print
    exit
  }
' pom.xml)

[[ -n "$pom_version" ]] || fail "Unable to parse project version from pom.xml"
[[ "$pom_version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] || fail "pom.xml version is not semver: $pom_version"
pass "pom.xml version parsed: $pom_version"

changelog_version=$(grep -m1 '^## \[' CHANGELOG.md | sed -E 's/^## \[([^]]+)\].*/\1/' || true)
[[ -n "$changelog_version" ]] || fail "Unable to parse top version from CHANGELOG.md"
[[ "$pom_version" == "$changelog_version" ]] || fail "Version mismatch pom=$pom_version changelog=$changelog_version"
pass "CHANGELOG version matches pom.xml"

if grep -q "Spring Boot:" docs/engine-versions.md; then
  pass "Engine version documentation present"
else
  fail "docs/engine-versions.md missing expected engine entries"
fi

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || fail "Not inside a git repository"
pass "Git repository detected"

echo "All versioning checks passed."
