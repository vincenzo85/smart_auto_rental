#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

BASE_URL="${BASE_URL:-http://localhost:8080}"

pass() { echo "[PASS] $1"; }
fail() { echo "[FAIL] $1" >&2; exit 1; }

command -v curl >/dev/null 2>&1 || fail "curl is required"
command -v jq >/dev/null 2>&1 || fail "jq is required"

urlencode() {
  jq -nr --arg v "$1" '$v|@uri'
}

wait_api() {
  for _ in {1..30}; do
    code=$(curl -s -o /tmp/reg_health.json -w "%{http_code}" "$BASE_URL/actuator/health" || true)
    if [[ "$code" == "200" ]]; then
      pass "API reachable"
      return
    fi
    sleep 2
  done
  fail "API not reachable"
}

request_json() {
  local method="$1"
  local path="$2"
  local token="${3:-}"
  local body="${4:-}"
  local tmp
  tmp="$(mktemp)"

  local -a args
  args=(-sS -o "$tmp" -w "%{http_code}" -X "$method" "$BASE_URL$path" -H "Content-Type: application/json")
  if [[ -n "$token" ]]; then
    args+=( -H "Authorization: Bearer $token" )
  fi
  if [[ -n "$body" ]]; then
    args+=( -d "$body" )
  fi

  RESPONSE_STATUS="$(curl "${args[@]}")"
  RESPONSE_BODY="$(cat "$tmp")"
  rm -f "$tmp"
}

wait_api

UI_CODE="$(curl -sS -o /tmp/reg_ui.html -w "%{http_code}" "$BASE_URL/")"
[[ "$UI_CODE" == "200" ]] || fail "UI home unavailable"
SWAGGER_CODE="$(curl -sS -L -o /tmp/reg_sw.html -w "%{http_code}" "$BASE_URL/swagger-ui.html")"
[[ "$SWAGGER_CODE" == "200" ]] || fail "Swagger unavailable"
pass "UI and Swagger availability"

request_json POST "/api/v1/auth/login" "" '{"email":"customer@smartauto.local","password":"WrongPass"}'
[[ "$RESPONSE_STATUS" == "401" ]] || fail "Invalid login should return 401, got $RESPONSE_STATUS"
pass "Invalid login guard"

request_json GET "/api/v1/availability?branchId=1&startTime=$(urlencode "2026-04-01T10:00:00Z")&endTime=$(urlencode "2026-04-02T10:00:00Z")"
[[ "$RESPONSE_STATUS" == "401" || "$RESPONSE_STATUS" == "403" ]] || fail "Availability without token should be 401/403, got $RESPONSE_STATUS"
pass "Auth guard on availability (status=$RESPONSE_STATUS)"

EMAIL="regression_$(date +%s)@example.com"
request_json POST "/api/v1/auth/register" "" "$(jq -nc --arg e "$EMAIL" '{email:$e,password:"Customer123!"}')"
[[ "$RESPONSE_STATUS" == "201" ]] || fail "Register user failed: $RESPONSE_STATUS $RESPONSE_BODY"
TOKEN="$(echo "$RESPONSE_BODY" | jq -r '.token')"
[[ -n "$TOKEN" && "$TOKEN" != "null" ]] || fail "Token missing after register"
pass "Regression user registration"

PAST_START="$(date -u -d '-1 day' +%Y-%m-%dT10:00:00Z)"
PAST_END="$(date -u -d '+1 day' +%Y-%m-%dT10:00:00Z)"
PAST_PAYLOAD="$(jq -nc --arg start "$PAST_START" --arg end "$PAST_END" '{carId:1,startTime:$start,endTime:$end,insuranceSelected:false,payAtDesk:false,allowWaitlist:false,forcedPaymentStatus:"SUCCESS"}')"
request_json POST "/api/v1/bookings" "$TOKEN" "$PAST_PAYLOAD"
[[ "$RESPONSE_STATUS" == "400" ]] || fail "Past booking should return 400, got $RESPONSE_STATUS"
pass "Past date validation"

START_TIME="$(date -u -d '+15 days' +%Y-%m-%dT10:00:00Z)"
END_TIME="$(date -u -d '+17 days' +%Y-%m-%dT10:00:00Z)"
request_json GET "/api/v1/availability?branchId=1&startTime=$(urlencode "$START_TIME")&endTime=$(urlencode "$END_TIME")&category=ECONOMY" "$TOKEN"
[[ "$RESPONSE_STATUS" == "200" ]] || fail "Availability failed"
CAR_ID="$(echo "$RESPONSE_BODY" | jq -r '.[0].carId')"
[[ -n "$CAR_ID" && "$CAR_ID" != "null" ]] || fail "No car available for conflict test"

BOOKING_PAYLOAD="$(jq -nc --argjson carId "$CAR_ID" --arg start "$START_TIME" --arg end "$END_TIME" '{carId:$carId,startTime:$start,endTime:$end,insuranceSelected:false,payAtDesk:false,allowWaitlist:false,forcedPaymentStatus:"SUCCESS"}')"
request_json POST "/api/v1/bookings" "$TOKEN" "$BOOKING_PAYLOAD"
[[ "$RESPONSE_STATUS" == "201" ]] || fail "First booking failed"
BOOKING_ID="$(echo "$RESPONSE_BODY" | jq -r '.id')"
pass "First booking for conflict baseline"

request_json POST "/api/v1/bookings" "$TOKEN" "$BOOKING_PAYLOAD"
[[ "$RESPONSE_STATUS" == "409" ]] || fail "Conflict booking should be 409, got $RESPONSE_STATUS"
pass "Conflict detection regression"

request_json GET "/api/v1/bookings/$BOOKING_ID/audit" "$TOKEN"
[[ "$RESPONSE_STATUS" == "200" ]] || fail "Audit endpoint failed"
AUDIT_EVENTS="$(echo "$RESPONSE_BODY" | jq 'length')"
(( AUDIT_EVENTS >= 1 )) || fail "Expected at least 1 audit event"
pass "Audit trail regression"

request_json GET "/api/v1/payments/$BOOKING_ID/transactions" "$TOKEN"
[[ "$RESPONSE_STATUS" == "200" ]] || fail "Payment transactions endpoint failed"
pass "Payment transactions endpoint"

echo "Regression test suite passed."
