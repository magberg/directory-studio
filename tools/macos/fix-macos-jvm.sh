#!/bin/sh
# Apply -vm pointing at a suitable JDK into an installed Studio .app
# Usage: ./fix-macos-jvm.sh [/path/to/ApacheDirectoryStudio.app]

set -e

APP="${1:-/Applications/ApacheDirectoryStudio.app}"
INI="$APP/Contents/Eclipse/ApacheDirectoryStudio.ini"

if [ ! -f "$INI" ]; then
  echo "Not found: $INI"
  exit 1
fi

JAVA_BIN=""
for candidate in \
  ${STUDIO_JAVA_HOME:+$STUDIO_JAVA_HOME} \
  ${JAVA_HOME:+$JAVA_HOME} \
  /opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home \
  /opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home \
  /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
do
  [ -z "$candidate" ] && continue
  if [ -x "$candidate/bin/java" ]; then
    JAVA_BIN="$candidate/bin/java"
    break
  fi
done

if [ -z "$JAVA_BIN" ]; then
  echo "Install arm64 JDK first: brew install openjdk@25"
  exit 1
fi

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
print(f"Wrote -vm {java}\n into {ini}")
PY

# Optional: register JDK for Finder / java_home (may ask for password)
JDK_BUNDLE=$(cd "$(dirname "$JAVA_BIN")/../.." && pwd)
if [ "$(basename "$JDK_BUNDLE")" = "Home" ]; then
  JDK_BUNDLE=$(cd "$JDK_BUNDLE/../.." && pwd)
fi
if [ -d "$JDK_BUNDLE/Contents/Home" ]; then
  echo
  echo "Recommended (one-time, needs admin) so Finder also finds JDK 25:"
  echo "  sudo ln -sfn \"$JDK_BUNDLE\" /Library/Java/JavaVirtualMachines/openjdk-25.jdk"
fi

echo
echo "Now open Studio again (or: open \"$APP\")"
