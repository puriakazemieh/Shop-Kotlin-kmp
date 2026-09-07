#!/usr/bin/env bash
# Build an installable WordPress plugin zip
set -euo pipefail
cd "$( dirname "$0" )"

OUT="carmilla-bridge.zip"
rm -f "$OUT"

# Package shared core
mkdir -p carmilla-bridge/packages
cp -R packages/carmilla-core carmilla-bridge/packages/

zip -rq "$OUT" carmilla-bridge

# Clean up shared core from src
rm -rf carmilla-bridge/packages

echo "Built $OUT"
