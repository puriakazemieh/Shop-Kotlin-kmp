#!/usr/bin/env bash
# Build an installable WordPress plugin zip
set -euo pipefail
cd "$( dirname "$0" )"

OUT="${1:-carmilla-bridge.zip}"
case "$OUT" in /*) ;; *) OUT="$PWD/$OUT" ;; esac
test -f packages/carmilla-core/Carmilla_Kernel.php
stage="$(mktemp -d)"
trap 'rm -rf "$stage"' EXIT
cp -R carmilla-bridge "$stage/carmilla-bridge"
mkdir -p "$stage/carmilla-bridge/packages"
cp -R packages/carmilla-core "$stage/carmilla-bridge/packages/carmilla-core"
mkdir -p "$(dirname "$OUT")"
rm -f "$OUT"
( cd "$stage" && zip -rq "$OUT" carmilla-bridge \
  -x '*/tests/*' '*/node_modules/*' '*/.DS_Store' )
unzip -Z1 "$OUT" | grep -Fx 'carmilla-bridge/packages/carmilla-core/Carmilla_Kernel.php' >/dev/null

echo "Built $OUT"
