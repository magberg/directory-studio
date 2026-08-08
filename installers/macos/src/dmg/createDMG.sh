#!/bin/sh
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

set -e

# Maven resource filtering fills ${version}. Optional override:
#   STUDIO_MACOS_ARCH=aarch64 ./createDMG.sh
#   STUDIO_MACOS_ARCH=all ./createDMG.sh   # both x86_64 and aarch64 when present
# Default: aarch64 (Apple Silicon). Use x86_64 for Intel DMG.
VERSION="${version}"
PRODUCTS_DIR="../../../product/target/products"
ARCH_REQUEST="${STUDIO_MACOS_ARCH:-aarch64}"

create_one_dmg() {
  arch="$1"
  archive="${PRODUCTS_DIR}/ApacheDirectoryStudio-${VERSION}-macosx.cocoa.${arch}.tar.gz"

  if [ ! -f "${archive}" ]; then
    echo "ERROR: missing product archive: ${archive}"
    echo "Build the product first (mvn clean install), then re-run the macOS installer."
    exit 1
  fi

  echo "### Creating macOS DMG for arch=${arch}"

  rm -rf "dmg-${arch}" "TMP-${arch}.dmg"
  mkdir "dmg-${arch}"
  mkdir -p "dmg-${arch}/.background"

  tar -xf "${archive}" -C "dmg-${arch}"

  cp "dmg-${arch}/ApacheDirectoryStudio.app/Contents/Eclipse/LICENSE" "dmg-${arch}/"
  cp "dmg-${arch}/ApacheDirectoryStudio.app/Contents/Eclipse/NOTICE" "dmg-${arch}/"

  cp background.png "dmg-${arch}/.background/"
  cp DS_Store "dmg-${arch}/.DS_Store"

  ln -s /Applications "dmg-${arch}/Applications"

  APP_PATH="dmg-${arch}/ApacheDirectoryStudio.app"
  INI_PATH="${APP_PATH}/Contents/Eclipse/ApacheDirectoryStudio.ini"

  # Prefer an explicit JDK for local/fork builds so Apple Silicon does not
  # pick up an old x86_64 JDK 8 from /Library/Java/JavaVirtualMachines.
  RESOLVED_JAVA=""
  if [ -n "${STUDIO_JAVA_HOME}" ] && [ -x "${STUDIO_JAVA_HOME}/bin/java" ]; then
    RESOLVED_JAVA="${STUDIO_JAVA_HOME}/bin/java"
  elif [ -x /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home/bin/java ]; then
    RESOLVED_JAVA=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home/bin/java
  elif [ -n "${JAVA_HOME}" ] && [ -x "${JAVA_HOME}/bin/java" ]; then
    case "${JAVA_HOME}" in
      *jdk1.8*|*jdk-8*) ;;
      *) RESOLVED_JAVA="${JAVA_HOME}/bin/java" ;;
    esac
  fi

  if [ -n "${RESOLVED_JAVA}" ] && [ -f "${INI_PATH}" ]; then
    echo "### Injecting -vm ${RESOLVED_JAVA} into product ini"
    python3 - "${INI_PATH}" "${RESOLVED_JAVA}" <<'PY'
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

  if [ -n "${APPLE_SIGNING_ID}" ]; then
    echo "### Codesigning with APPLE_SIGNING_ID=${APPLE_SIGNING_ID}"
    codesign --force --deep --timestamp --options runtime --entitlements entitlements.plist \
      -s "${APPLE_SIGNING_ID}" "${APP_PATH}"
    codesign -dv --verbose=4 "${APP_PATH}"
  else
    echo "### APPLE_SIGNING_ID not set — ad-hoc codesign for local use"
    codesign --force --deep -s - "${APP_PATH}" || true
  fi

  hdiutil create -srcfolder "dmg-${arch}/" -volname "ApacheDirectoryStudio" -o "TMP-${arch}.dmg"
  hdiutil convert -format UDZO "TMP-${arch}.dmg" \
    -o "ApacheDirectoryStudio-${VERSION}-macosx.cocoa.${arch}.dmg"

  rm -f "TMP-${arch}.dmg"
  rm -rf "dmg-${arch}"
  echo "### Created ApacheDirectoryStudio-${VERSION}-macosx.cocoa.${arch}.dmg"
}

case "${ARCH_REQUEST}" in
  all)
    create_one_dmg x86_64
    create_one_dmg aarch64
    ;;
  x86_64|aarch64)
    create_one_dmg "${ARCH_REQUEST}"
    ;;
  *)
    echo "ERROR: STUDIO_MACOS_ARCH must be aarch64, x86_64, or all (got: ${ARCH_REQUEST})"
    exit 1
    ;;
esac
