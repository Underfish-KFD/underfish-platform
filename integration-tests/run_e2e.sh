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

GATEWAY_URL=${GATEWAY_URL:-http://localhost:8080}
REGISTER_PATH=${REGISTER_PATH:-/api/auth/register}
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

# decode payload
PAYLOAD=$(echo "$TOKEN" | awk -F. '{print $2}')
if [[ -z "$PAYLOAD" ]]; then
  echo "Invalid JWT format" >&2
  exit 5
fi
PAD=$(( (4 - ${#PAYLOAD} % 4) % 4 ))
for i in $(seq 1 "$PAD"); do PAYLOAD="$PAYLOAD="; done
DECODED_PAYLOAD=""
if DECODED_PAYLOAD=$(echo "$PAYLOAD" | tr '_-' '/+' | base64 --decode 2>/dev/null); then
  :
elif DECODED_PAYLOAD=$(echo "$PAYLOAD" | tr '_-' '/+' | base64 -D 2>/dev/null); then
  :
else
  DECODED_PAYLOAD=""
fi
if [[ -z "$DECODED_PAYLOAD" ]]; then
  echo "Failed to decode JWT payload" >&2
  exit 6
fi
echo "JWT payload: $DECODED_PAYLOAD"

EMAIL_IN_TOKEN=$(echo "$DECODED_PAYLOAD" | jq -r '.sub // .email // empty')
if [[ "$EMAIL_IN_TOKEN" != "$EMAIL" ]]; then
  echo "Email in token ('$EMAIL_IN_TOKEN') != expected ('$EMAIL')" >&2
  exit 7
fi

echo "Waiting 2s for DB entry..."
sleep 2

export PGPASSWORD="$DB_PASS"
PSQL_CMD=(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -Atc)

SQL_COUNT="SELECT count(*) FROM users WHERE email = '$EMAIL'"

echo "Checking DB: ${PSQL_CMD[*]} -> $SQL_COUNT"
COUNT=$("${PSQL_CMD[@]}" "$SQL_COUNT") || {
  echo "DB query failed" >&2
  exit 8
}

if [[ "$COUNT" -eq 1 ]]; then
  echo "User persisted in DB: $EMAIL"
  exit 0
else
  echo "User not found in DB (count=$COUNT)" >&2
  exit 9
fi

