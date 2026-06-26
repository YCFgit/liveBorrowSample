#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

request() {
  local path="$1"
  local response
  response="$(curl -fsS "${BASE_URL}${path}")"
  if ! printf '%s' "$response" | grep -q '"code":"0"'; then
    echo "Smoke test failed for ${path}" >&2
    echo "$response" >&2
    exit 1
  fi
  echo "ok ${path}"
}

request "/api/v1/health"
request "/api/v1/returns/virtual-stores"
request "/api/v1/borrow/tasks?pageNo=1&pageSize=5"

html="$(curl -fsS "${BASE_URL}/dingtalk/borrow-assistant.html")"
if ! printf '%s' "$html" | grep -q '<title>直播借样助手</title>'; then
  echo "Smoke test failed for /dingtalk/borrow-assistant.html" >&2
  exit 1
fi
echo "ok /dingtalk/borrow-assistant.html"
