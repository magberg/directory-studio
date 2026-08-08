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

# Assemble fork release directory from Tycho / installer outputs.
# Usage: ./tools/assemble-release.sh [VERSION]
# Default VERSION is read from root pom.xml (e.g. 3.0.0-SNAPSHOT).

set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if [ -n "$1" ]; then
  VERSION="$1"
else
  VERSION="$(sed -n 's/.*<version>\(3\.[0-9.]*-SNAPSHOT\)<\/version>.*/\1/p' pom.xml | head -1)"
fi

if [ -z "$VERSION" ]; then
  echo "ERROR: could not determine VERSION (pass as arg or check pom.xml)"
  exit 1
fi

PRODUCTS="$ROOT/product/target/products"
MAC_DMG="$ROOT/installers/macos/target"
WIN_EXE="$ROOT/installers/windows/64bit/target"
OUT="$ROOT/release/$VERSION"

echo "### Assembling fork release $VERSION → $OUT"
rm -rf "$OUT"
mkdir -p "$OUT"

copy_if() {
  src="$1"
  if [ -f "$src" ]; then
    cp -f "$src" "$OUT/"
    echo "  + $(basename "$src")"
  else
    echo "  - missing (skip): $src"
  fi
}

# Product archives
copy_if "$PRODUCTS/ApacheDirectoryStudio-${VERSION}-macosx.cocoa.aarch64.tar.gz"
copy_if "$PRODUCTS/ApacheDirectoryStudio-${VERSION}-macosx.cocoa.x86_64.tar.gz"
copy_if "$PRODUCTS/ApacheDirectoryStudio-${VERSION}-linux.gtk.x86_64.tar.gz"
copy_if "$PRODUCTS/ApacheDirectoryStudio-${VERSION}-linux.gtk.aarch64.tar.gz"
copy_if "$PRODUCTS/ApacheDirectoryStudio-${VERSION}-win32.win32.x86_64.zip"

# macOS DMGs
copy_if "$MAC_DMG/ApacheDirectoryStudio-${VERSION}-macosx.cocoa.aarch64.dmg"
copy_if "$MAC_DMG/ApacheDirectoryStudio-${VERSION}-macosx.cocoa.x86_64.dmg"

# Windows installer (optional)
if ls "$WIN_EXE"/ApacheDirectoryStudio-"${VERSION}"-*.exe >/dev/null 2>&1; then
  cp -f "$WIN_EXE"/ApacheDirectoryStudio-"${VERSION}"-*.exe "$OUT/"
  echo "  + windows exe"
fi

# Docs / legal
cp -f "$ROOT/CHANGELOG.md" "$OUT/"
cp -f "$ROOT/LICENSE" "$OUT/" 2>/dev/null || cp -f "$ROOT/product/LICENSE" "$OUT/" 2>/dev/null || true
cp -f "$ROOT/NOTICE" "$OUT/" 2>/dev/null || cp -f "$ROOT/product/NOTICE" "$OUT/" 2>/dev/null || true
cp -f "$ROOT/FORK.md" "$OUT/"
cp -f "$ROOT/FORK_RELEASE.md" "$OUT/"

# SBOM
if [ -f "$ROOT/target/bom.json" ]; then
  cp -f "$ROOT/target/bom.json" "$OUT/bom.json"
  echo "  + bom.json"
elif [ -f "$ROOT/eclipse-trgt-platform/target/bom.json" ]; then
  cp -f "$ROOT/eclipse-trgt-platform/target/bom.json" "$OUT/bom.json"
  echo "  + bom.json (from eclipse-trgt-platform)"
fi

# Checksums
cd "$OUT"
if command -v shasum >/dev/null 2>&1; then
  shasum -a 256 \
    ApacheDirectoryStudio-* \
    CHANGELOG.md LICENSE NOTICE bom.json 2>/dev/null \
    > SHA256SUMS || true
elif command -v sha256sum >/dev/null 2>&1; then
  sha256sum ApacheDirectoryStudio-* CHANGELOG.md LICENSE NOTICE bom.json 2>/dev/null \
    > SHA256SUMS || true
fi

echo
echo "### Release contents"
ls -lh "$OUT"
echo
echo "Done. Upload with: gh release create v3.0.0-modernize.0 --notes-file CHANGELOG.md $OUT/*"
