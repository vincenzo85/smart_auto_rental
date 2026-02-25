#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

BASE_URL="${BASE_URL:-http://localhost:8080}"
INTEGRATION_KEY="${INTEGRATION_KEY:-integration-docker-key}"

pass() { echo "[PASS] $1"; }
fail() { echo "[FAIL] $1" >&2; exit 1; }

command -v curl >/dev/null 2>&1 || fail "curl is required"
command -v jq >/dev/null 2>&1 || fail "jq is required"

urlencode() {
  jq -nr --arg v "$1" '$v|@uri'
}

wait_api() {
  for _ in {1..30}; do
    code=$(curl -s -o /tmp/e2e_health.json -w "%{http_code}" "$BASE_URL/actuator/health" || true)
    if [[ "$code" == "200" ]]; then
      pass "API reachable"
      return
    fi
    sleep 2
  done
  fail "API not reachable at $BASE_URL"
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

request_json POST "/api/v1/auth/login" "" '{"email":"customer@smartauto.local","password":"Customer123!"}'
[[ "$RESPONSE_STATUS" == "200" ]] || fail "Customer login failed: $RESPONSE_STATUS $RESPONSE_BODY"
CUSTOMER_TOKEN="$(echo "$RESPONSE_BODY" | jq -r '.token')"
[[ -n "$CUSTOMER_TOKEN" && "$CUSTOMER_TOKEN" != "null" ]] || fail "Customer token missing"
pass "Customer login"

request_json POST "/api/v1/auth/login" "" '{"email":"admin@smartauto.local","password":"Admin123!"}'
[[ "$RESPONSE_STATUS" == "200" ]] || fail "Admin login failed: $RESPONSE_STATUS $RESPONSE_BODY"
ADMIN_TOKEN="$(echo "$RESPONSE_BODY" | jq -r '.token')"
[[ -n "$ADMIN_TOKEN" && "$ADMIN_TOKEN" != "null" ]] || fail "Admin token missing"
pass "Admin login"

for offset in 5 8 11 14 17 20 23 26 29 32; do
  START_TIME="$(date -u -d "+${offset} days" +%Y-%m-%dT10:00:00Z)"
  END_TIME="$(date -u -d "+$((offset + 2)) days" +%Y-%m-%dT10:00:00Z)"
  START_ENC="$(urlencode "$START_TIME")"
  END_ENC="$(urlencode "$END_TIME")"

  request_json GET "/api/v1/availability?branchId=1&startTime=$START_ENC&endTime=$END_ENC&category=ECONOMY" "$CUSTOMER_TOKEN"
  [[ "$RESPONSE_STATUS" == "200" ]] || fail "Availability search failed: $RESPONSE_STATUS $RESPONSE_BODY"
  AVAIL_COUNT="$(echo "$RESPONSE_BODY" | jq 'length')"

  if (( AVAIL_COUNT > 0 )); then
    CAR_ID="$(echo "$RESPONSE_BODY" | jq -r '.[0].carId')"
    [[ -n "$CAR_ID" && "$CAR_ID" != "null" ]] || fail "carId not found"
    pass "Availability search ($AVAIL_COUNT cars) window +${offset}d"
    break
  fi
done

if [[ -z "${CAR_ID:-}" || "${CAR_ID:-}" == "null" ]]; then
  fail "No cars available for E2E in tested windows"
fi

BOOKING_PAYLOAD="$(jq -nc \
  --argjson carId "$CAR_ID" \
  --arg start "$START_TIME" \
  --arg end "$END_TIME" \
  '{carId:$carId,startTime:$start,endTime:$end,insuranceSelected:true,couponCode:"WELCOME10",payAtDesk:false,allowWaitlist:false}')"

request_json POST "/api/v1/bookings" "$CUSTOMER_TOKEN" "$BOOKING_PAYLOAD"
[[ "$RESPONSE_STATUS" == "201" ]] || fail "Create booking failed: $RESPONSE_STATUS $RESPONSE_BODY"
BOOKING_ID="$(echo "$RESPONSE_BODY" | jq -r '.id')"
BOOKING_STATUS="$(echo "$RESPONSE_BODY" | jq -r '.status')"
PAYMENT_STATUS="$(echo "$RESPONSE_BODY" | jq -r '.paymentStatus')"
[[ "$BOOKING_STATUS" == "CONFIRMED" ]] || fail "Expected CONFIRMED, got $BOOKING_STATUS"
[[ "$PAYMENT_STATUS" == "SUCCESS" ]] || fail "Expected SUCCESS, got $PAYMENT_STATUS"
pass "Booking created and confirmed (id=$BOOKING_ID)"

request_json GET "/api/v1/bookings/me" "$CUSTOMER_TOKEN"
[[ "$RESPONSE_STATUS" == "200" ]] || fail "My bookings failed: $RESPONSE_STATUS $RESPONSE_BODY"
MATCH="$(echo "$RESPONSE_BODY" | jq --argjson id "$BOOKING_ID" '[.[] | select(.id == $id)] | length')"
(( MATCH == 1 )) || fail "Booking id=$BOOKING_ID not found in /bookings/me"
pass "Booking visible in /bookings/me"

request_json GET "/api/v1/payments/$BOOKING_ID/transactions" "$CUSTOMER_TOKEN"
[[ "$RESPONSE_STATUS" == "200" ]] || fail "Payment transactions failed: $RESPONSE_STATUS $RESPONSE_BODY"
TX_COUNT="$(echo "$RESPONSE_BODY" | jq 'length')"
(( TX_COUNT >= 1 )) || fail "Expected at least 1 payment transaction"
pass "Payment transaction history"

request_json GET "/api/v1/admin/reports/top-rented?limit=3" "$ADMIN_TOKEN"
[[ "$RESPONSE_STATUS" == "200" ]] || fail "Admin top-rented failed: $RESPONSE_STATUS $RESPONSE_BODY"
pass "Admin report top-rented"

request_json GET "/api/v1/admin/reports/utilization?branchId=1&from=$(urlencode "2026-03-01T00:00:00Z")&to=$(urlencode "2026-03-31T00:00:00Z")" "$ADMIN_TOKEN"
[[ "$RESPONSE_STATUS" == "200" ]] || fail "Admin utilization failed: $RESPONSE_STATUS $RESPONSE_BODY"
pass "Admin report utilization"

INT_CODE="$(curl -sS -o /tmp/e2e_integration.json -w "%{http_code}" -H "X-API-KEY: $INTEGRATION_KEY" "$BASE_URL/api/v1/integrations/availability?branchId=1&startTime=$START_ENC&endTime=$END_ENC&category=ECONOMY")"
[[ "$INT_CODE" == "200" ]] || fail "Integration availability failed: $INT_CODE $(cat /tmp/e2e_integration.json)"
pass "Integration availability with API key"

UI_CODE="$(curl -sS -o /tmp/e2e_ui.html -w "%{http_code}" "$BASE_URL/")"
[[ "$UI_CODE" == "200" ]] || fail "UI home failed: $UI_CODE"
SWAGGER_CODE="$(curl -sS -L -o /tmp/e2e_swagger.html -w "%{http_code}" "$BASE_URL/swagger-ui.html")"
[[ "$SWAGGER_CODE" == "200" ]] || fail "Swagger failed: $SWAGGER_CODE"
pass "UI and Swagger reachable"

echo "E2E test suite passed."
