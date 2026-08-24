#!/usr/bin/env bash
# Build an installable WordPress plugin zip (upload via Plugins → Add New → Upload).
set -euo pipefail
cd "$( dirname "$0" )"

OUT="carmilla-bridge.zip"
rm -f "$OUT"

zip -rq "$OUT" carmilla-bridge

echo "Built $OUT"
