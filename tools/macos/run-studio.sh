#!/bin/sh
# Launch Apache Directory Studio with an Apple Silicon–compatible JDK.
# Prefer Homebrew OpenJDK 25; fall back to newer Homebrew JDKs / JAVA_HOME.

set -e

find_java() {
  if [ -n "$STUDIO_JAVA_HOME" ] && [ -x "$STUDIO_JAVA_HOME/bin/java" ]; then
    echo "$STUDIO_JAVA_HOME/bin/java"
    return
  fi
  if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    # Reject known-bad Oracle 8 x86_64 on arm64 hosts
    case "$JAVA_HOME" in
      *jdk1.8*|*jdk-8*) ;;
      *) echo "$JAVA_HOME/bin/java"; return ;;
    esac
  fi
  for candidate in \
    /opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home \
    /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
    /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home \
    /opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home
  do
    if [ -x "$candidate/bin/java" ]; then
      echo "$candidate/bin/java"
      return
    fi
  done
  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    for v in 25 21 17; do
      home=$(/usr/libexec/java_home -v "$v" 2>/dev/null || true)
      if [ -n "$home" ] && [ -x "$home/bin/java" ]; then
        echo "$home/bin/java"
        return
      fi
    done
  fi
  return 1
}

ROOT="$(cd "$(dirname "$0")" && pwd)"
APP="$ROOT/ApacheDirectoryStudio.app"
if [ ! -d "$APP" ]; then
  APP="/Applications/ApacheDirectoryStudio.app"
fi
if [ ! -d "$APP" ]; then
  echo "ApacheDirectoryStudio.app not found next to this script or in /Applications"
  exit 1
fi

JAVA_BIN=$(find_java) || {
  echo "No suitable JDK 11+ (arm64) found."
  echo "Install: brew install openjdk@17"
  echo "Then:    sudo ln -sfn \$(brew --prefix openjdk@17)/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk"
  exit 1
}

# Ensure architecture matches the native arm64 launcher
ARCH=$(file -b "$JAVA_BIN" | tr ' ' '\n' | grep -E 'arm64|x86_64' | head -1)
APP_ARCH=$(file -b "$APP/Contents/MacOS/ApacheDirectoryStudio" | tr ' ' '\n' | grep -E 'arm64|x86_64' | head -1)
if [ "$APP_ARCH" = "arm64" ] && [ "$ARCH" = "x86_64" ]; then
  echo "Refusing x86_64 JVM ($JAVA_BIN) for arm64 Studio."
  echo "Install arm64 JDK: brew install openjdk@17"
  exit 1
fi

INI="$APP/Contents/Eclipse/ApacheDirectoryStudio.ini"
# Keep ini in sync so Finder launches also work
if [ -f "$INI" ] && [ -w "$INI" ]; then
  python3 - "$INI" "$JAVA_BIN" <<'PY'
import sys
from pathlib import Path
ini = Path(sys.argv[1])
java = sys.argv[2]
lines = ini.read_text().splitlines()
out = []
i = 0
while i < len(lines):
    if lines[i].strip() == "-vm":
        i += 1
        if i < len(lines) and not lines[i].startswith("-"):
            i += 1
        continue
    out.append(lines[i])
    i += 1
final = []
inserted = False
for line in out:
    if line.strip() == "-vmargs" and not inserted:
        final.append("-vm")
        final.append(java)
        inserted = True
    final.append(line)
if not inserted:
    final.extend(["-vm", java])
ini.write_text("\n".join(final) + "\n")
PY
fi

exec "$APP/Contents/MacOS/ApacheDirectoryStudio" "$@"
