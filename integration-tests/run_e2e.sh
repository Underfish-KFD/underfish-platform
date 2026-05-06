#!/usr/bin/env bash
set -euo pipefail

# Simple E2E test script for local stack
# Requirements: curl, jq, psql (Postgres client)
# Usage:
#   export GATEWAY_URL=http://localhost:8080
#   export DB_HOST=localhost
#   export DB_PORT=5432
#   export DB_NAME=auth_db
#   export DB_USER=auth_user
#   export DB_PASS=auth_pass
#   ./integration-tests/run_e2e.sh
#
# The script registers a user, validates that the JWT is RS256,
# verifies login and refresh-token endpoints, creates a community as that user,
# creates an event under that community, and checks that the auth user is persisted in DB.

GATEWAY_URL=${GATEWAY_URL:-http://localhost:8080}
REGISTER_PATH=${REGISTER_PATH:-/api/v1/users/register}
LOGIN_PATH=${LOGIN_PATH:-/api/v1/users/login}
REFRESH_PATH=${REFRESH_PATH:-/api/v1/tokens/refresh}
EXPECT_JWT_ALG=${EXPECT_JWT_ALG:-RS256}
DEBUG=${DEBUG:-0}
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-auth_db}
DB_USER=${DB_USER:-auth_user}
DB_PASS=${DB_PASS:-auth_pass}
READINESS_TIMEOUT_SECONDS=${READINESS_TIMEOUT_SECONDS:-90}
READINESS_INTERVAL_SECONDS=${READINESS_INTERVAL_SECONDS:-2}
SKIP_READINESS_CHECKS=${SKIP_READINESS_CHECKS:-0}

if [[ "$REGISTER_PATH" == "/api/auth/register" ]]; then
  echo "REGISTER_PATH=/api/auth/register is obsolete; using /api/v1/users/register" >&2
  REGISTER_PATH=/api/v1/users/register
fi

dump_container_logs() {
  if ! command -v docker >/dev/null 2>&1; then
    return 0
  fi

  for container in "$@"; do
    echo "--- docker logs --tail 120 $container ---" >&2
    docker logs --tail 120 "$container" >&2 || true
  done
}

check_required_containers() {
  if ! command -v docker >/dev/null 2>&1; then
    return 0
  fi

  local missing=0
  local failed_containers=()
  for container in \
    uf_gateway \
    uf_auth \
    uf_profile \
    uf_community \
    uf_event \
    uf_location \
    uf_pg_auth \
    uf_pg_profile \
    uf_pg_community \
    uf_pg_event \
    uf_pg_geo
  do
    local state
    state=$(docker inspect -f '{{.State.Status}}' "$container" 2>/dev/null || true)
    if [[ "$state" != "running" ]]; then
      if [[ -z "$state" ]]; then
        echo "Required container does not exist: $container" >&2
      else
        echo "Required container is not running: $container (state=$state)" >&2
      fi
      failed_containers+=("$container")
      missing=1
    fi
  done

  if [[ "$missing" -ne 0 ]]; then
    echo "Required E2E containers are not ready." >&2
    echo "If you recently ran 'docker compose ... down -v', do not restart only app containers with --no-deps." >&2
    echo "Start the full stack instead: docker compose -f docker-compose-infra.yml -f docker-compose-services.yml up -d" >&2
    dump_container_logs "${failed_containers[@]}"
    exit 2
  fi
}

wait_for_http() {
  local name="$1"
  local url="$2"
  local deadline=$((SECONDS + READINESS_TIMEOUT_SECONDS))

  echo "Waiting for $name readiness: $url"
  while (( SECONDS < deadline )); do
    if curl -fsS --max-time 2 "$url" >/dev/null 2>&1; then
      echo "$name is ready"
      return 0
    fi
    sleep "$READINESS_INTERVAL_SECONDS"
  done

  echo "$name did not become ready within ${READINESS_TIMEOUT_SECONDS}s: $url" >&2
  return 1
}

wait_for_tcp() {
  local name="$1"
  local host="$2"
  local port="$3"
  local deadline=$((SECONDS + READINESS_TIMEOUT_SECONDS))

  echo "Waiting for $name port: $host:$port"
  while (( SECONDS < deadline )); do
    if (echo >"/dev/tcp/$host/$port") >/dev/null 2>&1; then
      echo "$name port is accepting connections"
      return 0
    fi
    sleep "$READINESS_INTERVAL_SECONDS"
  done

  echo "$name port did not open within ${READINESS_TIMEOUT_SECONDS}s: $host:$port" >&2
  return 1
}

check_readiness() {
  if [[ "$SKIP_READINESS_CHECKS" == "1" ]]; then
    return 0
  fi

  check_required_containers

  local failed=0
  wait_for_http gateway "$GATEWAY_URL/actuator/health" || failed=1
  wait_for_http auth-service "http://localhost:8091/actuator/health" || failed=1
  wait_for_http profile-service "http://localhost:8082/actuator/health" || failed=1
  wait_for_http community-service "http://localhost:8084/actuator/health" || failed=1
  wait_for_http event-service "http://localhost:8083/actuator/health" || failed=1
  wait_for_http location-service "http://localhost:8081/actuator/health" || failed=1

  if [[ "$failed" -ne 0 ]]; then
    echo "One or more services are not ready." >&2
    echo "If you recently ran 'docker compose ... down -v', do not restart only app containers with --no-deps." >&2
    echo "Start the full stack instead: docker compose -f docker-compose-infra.yml -f docker-compose-services.yml up -d" >&2
    dump_container_logs uf_gateway uf_auth uf_profile uf_community uf_event uf_location
    exit 2
  fi
}

if ! command -v jq >/dev/null 2>&1; then
  echo "Error: jq is required. Install it (brew install jq)" >&2
  exit 2
fi
if ! command -v psql >/dev/null 2>&1; then
  echo "Error: psql is required. Install Postgres client (brew install libpq) and 'export PATH=/usr/local/opt/libpq/bin:$PATH' or similar" >&2
  exit 2
fi

check_readiness

EMAIL="e2e-$(uuidgen | tr '[:upper:]' '[:lower:]')@example.com"
PASSWORD=${E2E_PASSWORD:-P@ssw0rd123}

URL="$GATEWAY_URL$REGISTER_PATH"
echo "Registering user $EMAIL -> $URL"

REQ=$(jq -n --arg email "$EMAIL" --arg password "$PASSWORD" --arg name "E2E Test" '{email:$email, password:$password, name:$name}')

HTTP_RESPONSE=$(mktemp)
HTTP_HEADERS=$(mktemp)

if [[ "$DEBUG" == "1" ]]; then
  set -x
  HTTP_STATUS=$(curl -v -sS -X POST -H 'Content-Type: application/json' -d "$REQ" -D "$HTTP_HEADERS" -w '%{http_code}' "$URL" -o "$HTTP_RESPONSE" )
  set +x
else
  HTTP_STATUS=$(curl -sS -X POST -H 'Content-Type: application/json' -d "$REQ" -D "$HTTP_HEADERS" -w '%{http_code}' "$URL" -o "$HTTP_RESPONSE" )
fi
BODY=$(cat "$HTTP_RESPONSE")
rm -f "$HTTP_RESPONSE"

echo "HTTP $HTTP_STATUS"
if [[ $HTTP_STATUS -lt 200 || $HTTP_STATUS -ge 300 ]]; then
  echo "Registration failed, body:" >&2
  echo "$BODY" >&2
  echo "Response headers:" >&2
  cat "$HTTP_HEADERS" >&2
  if [[ "$HTTP_STATUS" == "401" ]]; then
    echo "Hint: try REGISTER_PATH=/api/v1/users/register and check auth-service logs: docker logs --tail 200 uf_auth" >&2
  fi
  dump_container_logs uf_gateway uf_auth
  rm -f "$HTTP_HEADERS"
  exit 3
fi
rm -f "$HTTP_HEADERS"

TOKEN=$(echo "$BODY" | jq -r '.token // .accessToken // .access_token // empty')
REFRESH_TOKEN=$(echo "$BODY" | jq -r '.refreshToken // .refresh_token // empty')
if [[ -z "$TOKEN" ]]; then
  echo "No token found in response: $BODY" >&2
  exit 4
fi
if [[ -z "$REFRESH_TOKEN" ]]; then
  echo "No refresh token found in response: $BODY" >&2
  exit 4
fi

echo "Token received: ${TOKEN:0:40}..."
echo "Refresh token received: ${REFRESH_TOKEN:0:40}..."

decode_base64url() {
  local value="$1"
  local pad=$(( (4 - ${#value} % 4) % 4 ))
  for _ in $(seq 1 "$pad"); do value="$value="; done

  if printf '%s' "$value" | tr '_-' '/+' | base64 --decode 2>/dev/null; then
    return 0
  fi
  printf '%s' "$value" | tr '_-' '/+' | base64 -D 2>/dev/null
}

HEADER=$(echo "$TOKEN" | awk -F. '{print $1}')
PAYLOAD=$(echo "$TOKEN" | awk -F. '{print $2}')
if [[ -z "$HEADER" || -z "$PAYLOAD" ]]; then
  echo "Invalid JWT format" >&2
  exit 5
fi

DECODED_HEADER=$(decode_base64url "$HEADER") || {
  echo "Failed to decode JWT header" >&2
  exit 6
}
echo "JWT header: $DECODED_HEADER"

ALG_IN_TOKEN=$(echo "$DECODED_HEADER" | jq -r '.alg // empty')
if [[ -n "$EXPECT_JWT_ALG" && "$ALG_IN_TOKEN" != "$EXPECT_JWT_ALG" ]]; then
  echo "JWT alg ('$ALG_IN_TOKEN') != expected ('$EXPECT_JWT_ALG')" >&2
  exit 7
fi

DECODED_PAYLOAD=$(decode_base64url "$PAYLOAD") || {
  echo "Failed to decode JWT payload" >&2
  exit 8
}
echo "JWT payload: $DECODED_PAYLOAD"

EMAIL_IN_TOKEN=$(echo "$DECODED_PAYLOAD" | jq -r '.sub // .email // empty')
if [[ "$EMAIL_IN_TOKEN" != "$EMAIL" ]]; then
  echo "Email in token ('$EMAIL_IN_TOKEN') != expected ('$EMAIL')" >&2
  exit 9
fi

USER_ID=$(echo "$DECODED_PAYLOAD" | jq -r '.userId // empty')
if [[ -z "$USER_ID" ]]; then
  echo "Numeric userId not found in access token payload: $DECODED_PAYLOAD" >&2
  exit 20
fi

LOGIN_URL="$GATEWAY_URL$LOGIN_PATH"
echo "Logging in user $EMAIL -> $LOGIN_URL"
LOGIN_REQ=$(jq -n --arg email "$EMAIL" --arg password "$PASSWORD" '{email:$email, password:$password}')
LOGIN_RESPONSE=$(mktemp)
LOGIN_HEADERS=$(mktemp)
if [[ "$DEBUG" == "1" ]]; then
  set -x
  LOGIN_STATUS=$(curl -v -sS -X POST -H 'Content-Type: application/json' -d "$LOGIN_REQ" -D "$LOGIN_HEADERS" -w '%{http_code}' "$LOGIN_URL" -o "$LOGIN_RESPONSE")
  set +x
else
  LOGIN_STATUS=$(curl -sS -X POST -H 'Content-Type: application/json' -d "$LOGIN_REQ" -D "$LOGIN_HEADERS" -w '%{http_code}' "$LOGIN_URL" -o "$LOGIN_RESPONSE")
fi
LOGIN_BODY=$(cat "$LOGIN_RESPONSE")
rm -f "$LOGIN_RESPONSE"
echo "Login HTTP $LOGIN_STATUS"
if [[ $LOGIN_STATUS -lt 200 || $LOGIN_STATUS -ge 300 ]]; then
  echo "Login failed, body:" >&2
  echo "$LOGIN_BODY" >&2
  echo "Response headers:" >&2
  cat "$LOGIN_HEADERS" >&2
  dump_container_logs uf_gateway uf_auth
  rm -f "$LOGIN_HEADERS"
  exit 15
fi
rm -f "$LOGIN_HEADERS"

LOGIN_TOKEN=$(echo "$LOGIN_BODY" | jq -r '.token // .accessToken // .access_token // empty')
LOGIN_REFRESH_TOKEN=$(echo "$LOGIN_BODY" | jq -r '.refreshToken // .refresh_token // empty')
if [[ -z "$LOGIN_TOKEN" || -z "$LOGIN_REFRESH_TOKEN" ]]; then
  echo "Login response does not contain access and refresh tokens: $LOGIN_BODY" >&2
  exit 16
fi
TOKEN="$LOGIN_TOKEN"
REFRESH_TOKEN="$LOGIN_REFRESH_TOKEN"
echo "Login token received: ${TOKEN:0:40}..."

BAD_LOGIN_REQ=$(jq -n --arg email "$EMAIL" --arg password "wrong-password" '{email:$email, password:$password}')
BAD_LOGIN_STATUS=$(curl -sS -X POST -H 'Content-Type: application/json' -d "$BAD_LOGIN_REQ" -w '%{http_code}' "$LOGIN_URL" -o /dev/null)
if [[ "$BAD_LOGIN_STATUS" != "401" ]]; then
  echo "Bad login returned HTTP $BAD_LOGIN_STATUS instead of 401" >&2
  exit 17
fi

REFRESH_URL="$GATEWAY_URL$REFRESH_PATH"
echo "Refreshing token -> $REFRESH_URL"
REFRESH_REQ=$(jq -n --arg refreshToken "$REFRESH_TOKEN" '{refreshToken:$refreshToken}')
REFRESH_RESPONSE=$(mktemp)
if [[ "$DEBUG" == "1" ]]; then
  set -x
  REFRESH_STATUS=$(curl -v -sS -X POST -H 'Content-Type: application/json' -d "$REFRESH_REQ" -w '%{http_code}' "$REFRESH_URL" -o "$REFRESH_RESPONSE")
  set +x
else
  REFRESH_STATUS=$(curl -sS -X POST -H 'Content-Type: application/json' -d "$REFRESH_REQ" -w '%{http_code}' "$REFRESH_URL" -o "$REFRESH_RESPONSE")
fi
REFRESH_BODY=$(cat "$REFRESH_RESPONSE")
rm -f "$REFRESH_RESPONSE"
echo "Refresh HTTP $REFRESH_STATUS"
if [[ $REFRESH_STATUS -lt 200 || $REFRESH_STATUS -ge 300 ]]; then
  echo "Token refresh failed, body:" >&2
  echo "$REFRESH_BODY" >&2
  dump_container_logs uf_gateway uf_auth
  exit 18
fi
TOKEN=$(echo "$REFRESH_BODY" | jq -r '.token // .accessToken // .access_token // empty')
if [[ -z "$TOKEN" ]]; then
  echo "Refresh response does not contain access token: $REFRESH_BODY" >&2
  exit 19
fi


PROFILE_RESPONSE=$(mktemp)
PROFILE_HEADERS=$(mktemp)
if [[ "$DEBUG" == "1" ]]; then
  set -x
  PROFILE_STATUS=$(curl -v -sS -H "Authorization: Bearer $TOKEN" -D "$PROFILE_HEADERS" -w '%{http_code}' "$GATEWAY_URL/api/v1/users/$USER_ID" -o "$PROFILE_RESPONSE")
  set +x
else
  PROFILE_STATUS=$(curl -sS -H "Authorization: Bearer $TOKEN" -D "$PROFILE_HEADERS" -w '%{http_code}' "$GATEWAY_URL/api/v1/users/$USER_ID" -o "$PROFILE_RESPONSE")
fi
PROFILE_BODY=$(cat "$PROFILE_RESPONSE")
rm -f "$PROFILE_RESPONSE"
echo "Profile HTTP $PROFILE_STATUS"
if [[ $PROFILE_STATUS -lt 200 || $PROFILE_STATUS -ge 300 ]]; then
  echo "Profile fetch failed, body:" >&2
  echo "$PROFILE_BODY" >&2
  echo "Response headers:" >&2
  cat "$PROFILE_HEADERS" >&2
  dump_container_logs uf_gateway uf_profile
  rm -f "$PROFILE_HEADERS"
  exit 21
fi
rm -f "$PROFILE_HEADERS"

PROFILE_EMAIL=$(echo "$PROFILE_BODY" | jq -r '.email // empty')
PROFILE_ID=$(echo "$PROFILE_BODY" | jq -r '.user_id // .userId // empty')
if [[ "$PROFILE_EMAIL" != "$EMAIL" || "$PROFILE_ID" != "$USER_ID" ]]; then
  echo "Profile response mismatch, expected id=$USER_ID email=$EMAIL, body: $PROFILE_BODY" >&2
  exit 22
fi


TAG_REQ=$(jq -n --arg name "музыка-$USER_ID" '{name:$name}')
TAG_RESPONSE=$(mktemp)
TAG_HEADERS=$(mktemp)
if [[ "$DEBUG" == "1" ]]; then
  set -x
  TAG_STATUS=$(curl -v -sS -X POST -H 'Content-Type: application/json; charset=utf-8' -H "Authorization: Bearer $TOKEN" -d "$TAG_REQ" -D "$TAG_HEADERS" -w '%{http_code}' "$GATEWAY_URL/api/v1/tags" -o "$TAG_RESPONSE")
  set +x
else
  TAG_STATUS=$(curl -sS -X POST -H 'Content-Type: application/json; charset=utf-8' -H "Authorization: Bearer $TOKEN" -d "$TAG_REQ" -D "$TAG_HEADERS" -w '%{http_code}' "$GATEWAY_URL/api/v1/tags" -o "$TAG_RESPONSE")
fi
TAG_BODY=$(cat "$TAG_RESPONSE")
rm -f "$TAG_RESPONSE"
echo "Tag HTTP $TAG_STATUS"
if [[ $TAG_STATUS -lt 200 || $TAG_STATUS -ge 300 ]]; then
  echo "Cyrillic tag creation failed, body:" >&2
  echo "$TAG_BODY" >&2
  echo "Response headers:" >&2
  cat "$TAG_HEADERS" >&2
  dump_container_logs uf_gateway uf_event
  rm -f "$TAG_HEADERS"
  exit 23
fi
rm -f "$TAG_HEADERS"

COMMUNITY_NAME="E2E Community $EMAIL"
COMMUNITY_REQ=$(jq -n \
  --arg name "$COMMUNITY_NAME" \
  --arg description "Community created by integration test" \
  '{name:$name, description:$description, isPrivate:false}')

COMMUNITY_RESPONSE=$(mktemp)
COMMUNITY_HEADERS=$(mktemp)
if [[ "$DEBUG" == "1" ]]; then
  set -x
  COMMUNITY_STATUS=$(curl -v -sS -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d "$COMMUNITY_REQ" -D "$COMMUNITY_HEADERS" -w '%{http_code}' "$GATEWAY_URL/api/v1/communities" -o "$COMMUNITY_RESPONSE")
  set +x
else
  COMMUNITY_STATUS=$(curl -sS -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d "$COMMUNITY_REQ" -D "$COMMUNITY_HEADERS" -w '%{http_code}' "$GATEWAY_URL/api/v1/communities" -o "$COMMUNITY_RESPONSE")
fi
COMMUNITY_BODY=$(cat "$COMMUNITY_RESPONSE")
rm -f "$COMMUNITY_RESPONSE"

echo "Community HTTP $COMMUNITY_STATUS"
if [[ $COMMUNITY_STATUS -lt 200 || $COMMUNITY_STATUS -ge 300 ]]; then
  echo "Community creation failed, body:" >&2
  echo "$COMMUNITY_BODY" >&2
  echo "Response headers:" >&2
  cat "$COMMUNITY_HEADERS" >&2
  dump_container_logs uf_gateway uf_community
  rm -f "$COMMUNITY_HEADERS"
  exit 10
fi
rm -f "$COMMUNITY_HEADERS"

COMMUNITY_ID=$(echo "$COMMUNITY_BODY" | jq -r '.communityId // .community_id // empty')
if [[ -z "$COMMUNITY_ID" ]]; then
  echo "No community id found in response: $COMMUNITY_BODY" >&2
  exit 11
fi
echo "Community created: $COMMUNITY_ID"

LOCATION_ID=${E2E_LOCATION_ID:-1}
EVENT_REQ=$(jq -n \
  --arg title "E2E Event $EMAIL" \
  --arg description "Event created by integration test" \
  --arg start_datetime "2030-01-01T10:00:00" \
  --arg end_datetime "2030-01-01T12:00:00" \
  --arg location_id "$LOCATION_ID" \
  --arg community_id "$COMMUNITY_ID" \
  '{
    title:$title,
    description:$description,
    start_datetime:$start_datetime,
    end_datetime:$end_datetime,
    location_id:$location_id,
    community_id:$community_id,
    price:0,
    currency:"RUB",
    status:"PUBLISHED",
    max_participants:10,
    is_online:true
  }')

EVENT_RESPONSE=$(mktemp)
EVENT_HEADERS=$(mktemp)
if [[ "$DEBUG" == "1" ]]; then
  set -x
  EVENT_STATUS=$(curl -v -sS -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d "$EVENT_REQ" -D "$EVENT_HEADERS" -w '%{http_code}' "$GATEWAY_URL/api/v1/events" -o "$EVENT_RESPONSE")
  set +x
else
  EVENT_STATUS=$(curl -sS -X POST -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" -d "$EVENT_REQ" -D "$EVENT_HEADERS" -w '%{http_code}' "$GATEWAY_URL/api/v1/events" -o "$EVENT_RESPONSE")
fi
EVENT_BODY=$(cat "$EVENT_RESPONSE")
rm -f "$EVENT_RESPONSE"

echo "Event HTTP $EVENT_STATUS"
if [[ $EVENT_STATUS -lt 200 || $EVENT_STATUS -ge 300 ]]; then
  echo "Event creation failed, body:" >&2
  echo "$EVENT_BODY" >&2
  echo "Response headers:" >&2
  cat "$EVENT_HEADERS" >&2
  dump_container_logs uf_gateway uf_event
  rm -f "$EVENT_HEADERS"
  exit 12
fi
rm -f "$EVENT_HEADERS"

EVENT_ID=$(echo "$EVENT_BODY" | jq -r '.event_id // .eventId // empty')
EVENT_COMMUNITY_ID=$(echo "$EVENT_BODY" | jq -r '.community_id // .communityId // empty')
if [[ -z "$EVENT_ID" ]]; then
  echo "No event id found in response: $EVENT_BODY" >&2
  exit 13
fi
if [[ "$EVENT_COMMUNITY_ID" != "$COMMUNITY_ID" ]]; then
  echo "Event community id ('$EVENT_COMMUNITY_ID') != expected ('$COMMUNITY_ID')" >&2
  exit 14
fi
echo "Event created: $EVENT_ID for community: $COMMUNITY_ID"

echo "Waiting 2s for DB entry..."
sleep 2

export PGPASSWORD="$DB_PASS"
PSQL_CMD=(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -Atc)

SQL_COUNT="SELECT count(*) FROM users WHERE email = '$EMAIL'"

echo "Checking DB: ${PSQL_CMD[*]} -> $SQL_COUNT"
COUNT=$("${PSQL_CMD[@]}" "$SQL_COUNT") || {
  echo "DB query failed" >&2
  exit 15
}

if [[ "$COUNT" -eq 1 ]]; then
  echo "User persisted in DB: $EMAIL"
  exit 0
else
  echo "User not found in DB (count=$COUNT)" >&2
  exit 16
fi

