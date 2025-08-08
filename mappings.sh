#!/usr/bin/env bash

set -euo pipefail

ORG="guavapay"
PROJECT="payment-sdk-android"

MAPPING="foundation/build/outputs/mapping/release/mapping.txt"
UUID_FILE="$(dirname "$MAPPING")/proguard.map.id"

TOKEN="$(grep -E '^sentry\.token=' local.properties | cut -d'=' -f2-)"

if [[ -z "$TOKEN" ]]; then
  echo "❌  sentry.token not found in local.properties"
  exit 1
fi

UUID="$(uuidgen)"
echo "$UUID" > "$UUID_FILE"

export SENTRY_AUTH_TOKEN="$TOKEN"

sentry-cli upload-proguard \
  --org "$ORG" \
  --project "$PROJECT" \
  --uuid "$UUID" \
  "$MAPPING" \
  "$@"

echo "✅  mapping uploaded; UUID=$UUID"