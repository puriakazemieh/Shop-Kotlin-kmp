#!/usr/bin/env bash
# Build an installable WordPress theme zip
set -euo pipefail
cd "$( dirname "$0" )"

OUT="${1:-carmilla-theme.zip}"
rm -f "$OUT"

# Package shared core
mkdir -p carmilla-theme/packages
cp -R packages/carmilla-core carmilla-theme/packages/

zip -rq "$OUT" carmilla-theme \
  -x 'carmilla-theme/preview.html' \
  -x 'carmilla-theme/preview-pages.html' \
  -x 'carmilla-theme/PARITY.md'

# Clean up shared core from src
rm -rf carmilla-theme/packages

echo "Built $OUT"
unzip -l "$OUT" | tail -3
