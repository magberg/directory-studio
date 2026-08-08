<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0.
-->

# Fork release process (`modernize`)

This document replaces the ASF [RELEASE.md](./RELEASE.md) ceremony for **this fork**.  
Do **not** run ASF votes, `people.apache.org` publish, or ASF Apple ID notarization unless you intentionally mirror upstream process.

## Release matrix

| Artifact | Path pattern |
|---|---|
| macOS aarch64 archive | `product/target/products/ApacheDirectoryStudio-*-macosx.cocoa.aarch64.tar.gz` |
| macOS x86_64 archive | `product/target/products/ApacheDirectoryStudio-*-macosx.cocoa.x86_64.tar.gz` |
| macOS aarch64 DMG | `installers/macos/target/ApacheDirectoryStudio-*-macosx.cocoa.aarch64.dmg` |
| macOS x86_64 DMG | `installers/macos/target/ApacheDirectoryStudio-*-macosx.cocoa.x86_64.dmg` |
| Linux x86_64 | `product/target/products/ApacheDirectoryStudio-*-linux.gtk.x86_64.tar.gz` |
| Linux aarch64 | `product/target/products/ApacheDirectoryStudio-*-linux.gtk.aarch64.tar.gz` |
| Windows zip | `product/target/products/ApacheDirectoryStudio-*-win32.win32.x86_64.zip` |
| Windows exe (optional) | `installers/windows/64bit/target/*.exe` (`-Pwindows`) |
| SBOM | `target/bom.json` |
| Changelog | `CHANGELOG.md` |

Version coordinate today: **`3.0.0-SNAPSHOT`**. Suggested GitHub tag for this package: **`v3.0.0-modernize.0`**.  
GA `3.0.0` (dropping `-SNAPSHOT` across the reactor) is a separate cut when you are ready.

## Local release build

```bash
export JAVA_HOME="$(/usr/libexec/java_home -v 25 2>/dev/null || echo /opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home)"
export PATH="$JAVA_HOME/bin:$PATH"
export STUDIO_JAVA_HOME="$JAVA_HOME"

./tools/bootstrap-target.sh
mvn -f pom-first.xml clean install -DskipTests
mvn clean install -DskipTests

# macOS DMGs (both arches when product archives exist)
mvn -f installers/pom.xml -Pmacos -Dstudio.macos.arch=all clean package

# Optional Windows installer (needs wine/NSIS environment)
# mvn -f installers/pom.xml -Pwindows package

./tools/assemble-release.sh
```

Output: `release/<version>/` with archives, DMGs (if present), `CHANGELOG.md`, `LICENSE`, `NOTICE`, SHA-256 sums, and SBOM copy.

### Codesign / notarize (optional)

```bash
export APPLE_SIGNING_ID="Developer ID Application: Your Name (TEAMID)"
mvn -f installers/pom.xml -Pmacos -Dstudio.macos.arch=aarch64 package
# then notarytool submit … (see upstream RELEASE.md notes)
```

Without `APPLE_SIGNING_ID`, DMGs get an **ad-hoc** signature suitable for local testing.

## GitHub Release

1. Push branch `modernize` and tag, e.g. `git tag v3.0.0-modernize.0 && git push origin v3.0.0-modernize.0`
2. Workflow [`.github/workflows/release.yml`](./.github/workflows/release.yml) builds on `ubuntu-latest` (Linux archives + SBOM). macOS DMGs are best attached from a macOS runner or uploaded from `./tools/assemble-release.sh` output.
3. Create/edit the GitHub Release from the tag; attach files from `release/<version>/`.

Or manually:

```bash
gh release create v3.0.0-modernize.0 \
  --title "3.0.0-modernize.0" \
  --notes-file CHANGELOG.md \
  release/3.0.0-SNAPSHOT/*
```

## Legal

Ship `LICENSE` + `NOTICE` with every binary package (already embedded in product archives).  
State clearly: **community/modernize fork, based on Apache Directory Studio, not an ASF release.**

## Out of scope for stage 6

- JustJ / bundled JRE
- ASF vote / gitbox / Nexus staging
- Renaming the `.app` / product id (kept for installer stability)
