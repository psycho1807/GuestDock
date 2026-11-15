#!/bin/sh
#
# Gradle start up script for UNIX
# Licensed under the Apache License, Version 2.0

set -e

APP_HOME=$( cd "${APP_HOME:-./}" && pwd -P ) || exit
APP_NAME="Gradle"
APP_BASE_NAME="${0##*/}"

# Default JVM options
DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"

warn() { echo "$*" >&2; }
die() { echo; echo "$*"; echo; exit 1; } >&2

cygwin=false; darwin=false; nonstop=false
case "$( uname )" in
  CYGWIN* ) cygwin=true ;;
  Darwin*  ) darwin=true ;;
  NONSTOP* ) nonstop=true ;;
esac

CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar

# Determine JAVA command
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
    if [ ! -x "$JAVACMD" ] ; then
        die "ERROR: JAVA_HOME is set to an invalid directory: $JAVA_HOME"
    fi
else
    JAVACMD=java
    command -v java >/dev/null 2>&1 || die "ERROR: 'java' not found in PATH."
fi

# Increase max file descriptors
if ! "$cygwin" && ! "$darwin" && ! "$nonstop" ; then
    MAX_FD=$(ulimit -H -n 2>/dev/null) || MAX_FD=256
    case $MAX_FD in
      '' | soft ) :;;
      * ) ulimit -n "$MAX_FD" 2>/dev/null || true ;;
    esac
fi

exec "$JAVACMD" \
    $DEFAULT_JVM_OPTS \
    $JAVA_OPTS \
    $GRADLE_OPTS \
    "-Dorg.gradle.appname=$APP_BASE_NAME" \
    -classpath "$CLASSPATH" \
    org.gradle.wrapper.GradleWrapperMain \
    "$@"
