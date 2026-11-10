#!/usr/bin/env bash
# Build an installable WordPress theme zip
set -euo pipefail
cd "$( dirname "$0" )"

OUT="${1:-carmilla-theme.zip}"
case "$OUT" in /*) ;; *) OUT="$PWD/$OUT" ;; esac
test -f packages/carmilla-core/Carmilla_Kernel.php
stage="$(mktemp -d)"
trap 'rm -rf "$stage"' EXIT
cp -R carmilla-theme "$stage/carmilla-theme"
mkdir -p "$stage/carmilla-theme/packages"
cp -R packages/carmilla-core "$stage/carmilla-theme/packages/carmilla-core"
mkdir -p "$(dirname "$OUT")"
rm -f "$OUT"
( cd "$stage" && zip -rq "$OUT" carmilla-theme \
  -x '*/tests/*' '*/node_modules/*' '*/.DS_Store' \
     'carmilla-theme/preview.html' \
     'carmilla-theme/preview-pages.html' \
     'carmilla-theme/PARITY.md' )
unzip -Z1 "$OUT" | grep -Fx 'carmilla-theme/packages/carmilla-core/Carmilla_Kernel.php' >/dev/null

echo "Built $OUT"
