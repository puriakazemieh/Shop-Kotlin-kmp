#!/usr/bin/env bash
# Build an installable WordPress theme zip (upload via Appearance → Themes → Add New).
# Usage: ./build-theme-zip.sh [output.zip]
set -euo pipefail
cd "$( dirname "$0" )"

OUT="${1:-carmilla-theme.zip}"
rm -f "$OUT"

zip -rq "$OUT" carmilla-theme \
  -x 'carmilla-theme/preview.html' \
  -x 'carmilla-theme/preview-pages.html' \
  -x 'carmilla-theme/PARITY.md'

echo "Built $OUT"
unzip -l "$OUT" | tail -3
