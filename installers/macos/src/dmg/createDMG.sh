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

# Parse args
sign_key=""
case "$1" in
    -s|--sign)
        if [ -z "$2" ]; then
            echo "Error: $1 requires a key argument" >&2
            exit 1
        fi
        sign_key="$2"
        ;;
esac

# Validate signing setup once, before the loop
if [ -n "$sign_key" ]; then
    if [ "$(uname -s)" = "Darwin" ]; then
        if ! command -v codesign >/dev/null 2>&1; then
            echo "Error: --sign requires the 'codesign' tool, which was not found in PATH." >&2
            exit 1
        fi
    else
        # codesign is macOS-only; rcodesign exists cross-platform but is not wired up here
        echo "Warning: code signing was requested but is only supported on macOS. Skipping signing." >&2
        sign_key=""
    fi
fi

for archive in ../../../../product/target/products/ApacheDirectoryStudio-*-macosx.*.tar.gz; do

    dmg=$(echo "$archive" | sed 's/tar\.gz/dmg/g')
    echo "Building $dmg"

    # cleanup
    rm -rf dmg/* TMP*dmg

    # prepare DMG content
    mkdir -p dmg/.background/
    cp -av background.png dmg/.background/
    cp -av DS_Store dmg/.DS_Store
    ln -sv /Applications dmg/Applications

    # Copy the application
    tar -xvf $archive -C dmg

    # Codesign the App and verify
    if [ -n "$sign_key" ]; then
        echo "Signing with ID: $sign_key"
        codesign --verbose --force --deep --timestamp --options runtime --entitlements entitlements.plist -s "$sign_key" dmg/ApacheDirectoryStudio.app
        codesign -dv --verbose=4 dmg/ApacheDirectoryStudio.app
    fi

    # Creating the disk image
    hdiutil create -verbose -srcfolder dmg/ -volname "ApacheDirectoryStudio" -o TMP.dmg
    hdiutil convert -verbose -format UDZO TMP.dmg -o "$dmg"

    rm -rf dmg/* TMP*dmg

done
