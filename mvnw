#!/bin/sh

set -e

BASE_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"

if [ -n "$JAVA_HOME" ]; then
  JAVA_EXEC="$JAVA_HOME/bin/java"
else
  JAVA_EXEC="java"
fi

exec "$JAVA_EXEC" \
  -Dmaven.multiModuleProjectDirectory="$BASE_DIR" \
  -cp "$BASE_DIR/.mvn/wrapper/maven-wrapper.jar" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
