#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LAUNCHER_DIR="${SINGULARITY_LAUNCHER_DIR:-$DIR}"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"
exec "$JAVA" \
  --module-path "$DIR/javafx" \
  --add-modules javafx.controls,javafx.web,javafx.graphics \
  -Dsingularity.launcher.dir="$LAUNCHER_DIR" \
  -jar "$DIR/SingularityLauncher.jar" "$@"
