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

command -v docker >/dev/null 2>&1 || fail "Docker is not installed"
docker compose version >/dev/null 2>&1 || fail "Docker Compose v2 is required"

postgres_status=$(docker compose ps --format json postgres | jq -r 'if type=="array" then .[0].State else .State end // empty' 2>/dev/null || true)
if [[ "$postgres_status" != "running" ]]; then
  fail "Postgres container is not running. Start stack with: docker compose up -d"
fi

query() {
  local sql="$1"
  docker compose exec -T postgres psql -U smart_user -d smart_rental -t -A -c "$sql" | tr -d '\r' | head -n 1
}

migration_count=$(query "select count(*) from flyway_schema_history where success = true;")
(( migration_count >= 2 )) || fail "Expected at least 2 successful migrations, found $migration_count"
pass "Flyway migrations applied: $migration_count"

branch_count=$(query "select count(*) from branches;")
(( branch_count >= 2 )) || fail "Expected at least 2 branches, found $branch_count"
pass "Seed branches present: $branch_count"

car_count=$(query "select count(*) from cars;")
(( car_count >= 5 )) || fail "Expected at least 5 cars, found $car_count"
pass "Seed cars present: $car_count"

user_count=$(query "select count(*) from users;")
(( user_count >= 3 )) || fail "Expected at least 3 users, found $user_count"
pass "Users present: $user_count"

duplicate_plate_count=$(query "select count(*) from (select license_plate from cars group by license_plate having count(*) > 1) t;")
(( duplicate_plate_count == 0 )) || fail "Duplicate license plates detected: $duplicate_plate_count"
pass "No duplicate license plates"

echo "All data smoke tests passed."
