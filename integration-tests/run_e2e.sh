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
# creates a community as that user, creates an event under that community,
# and checks that the auth user is persisted in DB.

GATEWAY_URL=${GATEWAY_URL:-http://localhost:8080}
REGISTER_PATH=${REGISTER_PATH:-/api/v1/users/register}
EXPECT_JWT_ALG=${EXPECT_JWT_ALG:-RS256}
DEBUG=${DEBUG:-0}
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-auth_db}
DB_USER=${DB_USER:-auth_user}
DB_PASS=${DB_PASS:-auth_pass}

if ! command -v jq >/dev/null 2>&1; then
  echo "Error: jq is required. Install it (brew install jq)" >&2
  exit 2
fi
if ! command -v psql >/dev/null 2>&1; then
  echo "Error: psql is required. Install Postgres client (brew install libpq) and 'export PATH=/usr/local/opt/libpq/bin:$PATH' or similar" >&2
  exit 2
fi

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
  rm -f "$HTTP_HEADERS"
  exit 3
fi
rm -f "$HTTP_HEADERS"

TOKEN=$(echo "$BODY" | jq -r '.token // .accessToken // .access_token // empty')
if [[ -z "$TOKEN" ]]; then
  echo "No token found in response: $BODY" >&2
  exit 4
fi

echo "Token received: ${TOKEN:0:40}..."

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

LOCATION_ID=$(uuidgen | tr '[:upper:]' '[:lower:]')
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
