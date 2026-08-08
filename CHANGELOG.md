<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0.
-->

# Changelog — Directory Studio modernize fork

All notable changes in this fork relative to [Apache Directory Studio](https://github.com/apache/directory-studio).  
License remains **Apache-2.0**. This is **not** an official ASF release.

## [3.0.0-SNAPSHOT] — modernize-0 (2026-08-08)

First fork release package (Maven/OSGi still `3.0.0-SNAPSHOT`; Git tag recommendation: `v3.0.0-modernize.0`).

### Added
- ADAC-like default RCP perspective (`plugins/adac.ui`): Navigation | Object list + breadcrumb | Tasks
- Sectioned property forms for User / Group / OU (AD + generic LDAP mapping)
- Typed New User / Group / OU wizards from the Tasks pane
- Apple Silicon (`macosx.cocoa.aarch64`) product archive + DMG
- Linux `aarch64` product archive
- CycloneDX SBOM artifact in CI
- Fork docs: `FORK.md`, `FORK_RELEASE.md`, `tools/assemble-release.sh`, macOS JVM helpers

### Changed
- Java baseline **25** (compile + runtime `osgi.requiredJavaVersion`)
- Tycho **5.0.3**, Eclipse **4.37** / SimRel 2025-09
- Security dependency bumps (BouncyCastle 1.85, HttpClient 4.5.14, Commons\*, Xerces 2.12.2)
- Removed Studio Log4j 1.x pin (RCP uses SLF4J → `slf4j-eclipselog`)
- About dialog branding: **(modernize fork)**

### Known limitations
- NLS / Babel packs deferred
- Integration UI tests optional / WIP on 4.37
- No JustJ bundled JRE yet (use system JDK 25+)
- ADAC stage B (reset password, enable/disable, Move, Find) not included
- Official Apple Developer ID notarization is optional (ad-hoc codesign by default)

### Attribution
Based on Apache Directory Studio. See `NOTICE` and `LICENSE`.
