#!/bin/sh
# Wrapper delegating to the Android app's gradle wrapper.
# This file allows CI or callers in the container root to run Gradle tasks.

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
APP_WRAPPER="$SCRIPT_DIR/notes_app_frontend/gradlew"

if [ ! -f "$APP_WRAPPER" ]; then
  echo "Error: App gradle wrapper not found at $APP_WRAPPER" >&2
  exit 127
fi

exec "$APP_WRAPPER" "$@"
