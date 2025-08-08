#!/usr/bin/env bash

set -euo pipefail

ORG="guavapay-limited"
PROJECT="cpg-payment-sdk-android"

SRC_FILE="foundation/src/main/java/com/guavapay/paymentsdk/metrica/MetricaUnit.kt"
MAPPING="foundation/build/outputs/mapping/release/mapping.txt"

TOKEN=$(grep -E '^sentry\.token=' local.properties | cut -d'=' -f2- || true)
[[ -z "$TOKEN" ]] && { echo "❌  sentry.token not found in local.properties"; exit 1; }
export SENTRY_AUTH_TOKEN="$TOKEN"

UUID=$(sed -n 's/.*proguardUuid[[:space:]]*=[[:space:]]*"\([0-9A-Fa-f-]\{36\}\)".*/\1/p' "$SRC_FILE" | head -n1)
[[ -z "$UUID" ]] && { echo "❌  proguardUuid not found in $SRC_FILE"; exit 1; }

echo "ℹ︎  Using UUID: $UUID"
[[ -f "$MAPPING" ]] || { echo "❌  mapping.txt not found at $MAPPING"; exit 1; }

sentry-cli upload-proguard \
  --org "$ORG" \
  --project "$PROJECT" \
  --uuid "$UUID" \
  "$MAPPING"

echo "✅  mapping uploaded for UUID=$UUID ($ORG/$PROJECT)"