#!/usr/bin/env bash
#
# Build a double-clickable native app for Shredder.
#
#   ./build-app.sh          # builds Shredder.app under dist/
#   ./build-app.sh dmg      # also builds a Shredder-<version>.dmg installer
#
# Requires a JDK 17+ (for jpackage) and Maven. If `mvn` is not on your PATH,
# set MVN to a Maven binary, e.g.  MVN=/path/to/mvn ./build-app.sh
set -euo pipefail

cd "$(dirname "$0")"

APP_NAME="Shredder"
MAIN_CLASS="com.shredder.Launcher"
VERSION="1.0.0"
MVN="${MVN:-mvn}"
DIST_DIR="dist"

# Pick the JavaFX native classifier for this machine so the fat jar bundles the
# right .dylib/.so/.dll set.
OS="$(uname -s)"
ARCH="$(uname -m)"
case "$OS" in
  Darwin) [ "$ARCH" = "arm64" ] && PLATFORM="mac-aarch64" || PLATFORM="mac" ;;
  Linux)  [ "$ARCH" = "aarch64" ] && PLATFORM="linux-aarch64" || PLATFORM="linux" ;;
  *)      PLATFORM="win" ;;
esac

echo "==> Building fat jar (javafx.platform=$PLATFORM)"
"$MVN" -q -Djavafx.platform="$PLATFORM" clean package

JAR="target/shredder.jar"
[ -f "$JAR" ] || { echo "Fat jar not found at $JAR"; exit 1; }

# jpackage type: default to an .app image; pass "dmg" for an installer.
TYPE="app-image"
[ "${1:-}" = "dmg" ] && TYPE="dmg"

echo "==> Packaging $APP_NAME ($TYPE) into $DIST_DIR/"
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"

jpackage \
  --type "$TYPE" \
  --name "$APP_NAME" \
  --app-version "$VERSION" \
  --input target \
  --main-jar "$(basename "$JAR")" \
  --main-class "$MAIN_CLASS" \
  --dest "$DIST_DIR"

echo "==> Done."
if [ "$TYPE" = "dmg" ]; then
  ls -1 "$DIST_DIR"/*.dmg
else
  echo "Open it with:  open \"$DIST_DIR/$APP_NAME.app\""
fi
