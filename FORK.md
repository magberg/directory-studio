<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0.
-->

# Fork notes — Directory Studio (`modernize`)

This repository is a **private/community fork** of [Apache Directory Studio](https://github.com/apache/directory-studio), not an official ASF release.

- **Upstream:** https://github.com/apache/directory-studio  
- **Fork remote:** https://github.com/magberg/directory-studio  
- **Working branch:** `modernize`  
- **License:** Apache-2.0 (keep `LICENSE` / `NOTICE`; preserve attribution)

## Branding decision (stage 0)

Keep product name **Apache Directory Studio**; show **(modernize fork)** in the About dialog.  
Do **not** rename `.app` / product id in stage 0 (avoids installer/path churn). Revisit at stage 6 if desired.

## Version scheme

| Layer | Value | Notes |
|---|---|---|
| Maven / OSGi | `3.0.0-SNAPSHOT` | Bumped from 2.0.0 after stage 2 platform |
| Fork package tag | `v3.0.0-modernize.0` | Stage 6 — first public fork package |
| Planned GA coord | `3.0.0` | Drop `-SNAPSHOT` in a later cut |
| Fork id (pom property) | `modernize` | `studio.fork.*` in root `pom.xml` |
| Target Java | **25** | Stage 2 done — compile + runtime baseline |

Non-OSGi strings like `2.1.0-fork.0` are **not** used as `Bundle-Version`.

## Platform status (stage 2, 2026-08-04)

| Item | Status |
|---|---|
| JDK | **25** (`maven.compiler.release=25`, `osgi.requiredJavaVersion=25`) |
| Tycho | **5.0.3** |
| Eclipse | **4.37** (`eclipse/updates/4.37`) + SimRel **2025-09** (EMF/ECF) |
| Orbit | **4.37.0** aggregation (JUnit Jupiter BSNs) |
| `mvn clean install -DskipTests` | OK on JDK 25 |
| Product `macosx.cocoa.aarch64` | OK |
| Product `linux.gtk.aarch64` | OK (new) |
| NLS / Babel | Deferred (Oxygen packs incompatible) |
| Integration tests | Optional profile `-Pintegration-tests` |

Bootstrap: `./tools/bootstrap-target.sh` before first Tycho resolve.  
DocBook on Java 25: `.mvn/jvm.config` raises entity limits.

## IDE / Cursor (Problems noise)

Cursor/VS Code **Java Language Server does not understand** the Tycho target platform or OSGi `Require-Bundle` graph. Tens of thousands of “cannot be resolved” Problems are **phantom**, not product bugs.

| Rule | Detail |
|---|---|
| **Compile truth** | `./build.sh` or `mvn install -DskipTests` |
| **Ignore** | Problems panel volume / red squiggles on `org.eclipse.*` and sibling plugins |
| **Quiet settings** | [`.vscode/settings.json`](./.vscode/settings.json) — `java.autobuild.enabled=false`, `java.errors.incompleteClasspath.severity=ignore`, exclude `target/` from search/watch |
| **Deep Java navigation** | Use Eclipse RCP + initialized `.target` (README § Setup Eclipse workspace) |

Agents: see [`AGENTS.md`](./AGENTS.md) — do not treat IDE Problems as a fix list.

**P2 truth build (2026-08-08):** `./build.sh` → **BUILD SUCCESS** (all reactor modules, including `adac.ui` + product). No real compile failures to fix; IDE Problems remain phantom.

## Security deps (stage 3)

| Dependency | Was | Now |
|---|---|---|
| BouncyCastle | 1.62 `jdk15on` | **1.85** `jdk18on` (`bcprov` + `bcpkix`) |
| HttpClient / HttpCore | 4.5.12 / 4.4.13 | **4.5.14** / **4.4.16** |
| Commons Codec | 1.15 | **1.17.2** |
| Commons IO | 2.6 | **2.18.0** (BSN `org.apache.commons.commons-io`) |
| Commons Lang3 | 3.12.0 | **3.17.0** |
| Commons Pool2 | 2.9.0 | **2.12.1** |
| Commons Text | 1.9 | **1.13.0** (BSN `org.apache.commons.text`) |
| Xerces (embedded) | 2.11.0 | **2.12.2** |
| SLF4J | 1.7.36 | **1.7.36** (kept; 2.x = separate BSN risk) |
| Log4j Studio pin | 1.2.17 | **removed** (RCP uses SLF4J → `slf4j-eclipselog`) |
| LDAP API / MINA | 2.1.0 / 2.1.10 | unchanged (align with ApacheDS AM26) |
| ApacheDS | 2.0.0.AM26 | **unchanged** (separate risk) |

Residual: `apacheds-service` uberjar still embeds Log4j 1.x; adapter keeps Log4j1-format `log4j.properties` for that process only (not shipped in RCP feature).

SBOM: CycloneDX aggregate `target/bom.json` on `package`; CI uploads as `cyclonedx-sbom` artifact (does not fail on advisory noise).

## Platform matrix

| OS | Arch | Archive | Installer |
|---|---|---|---|
| macOS | **aarch64** | tar.gz | DMG (stage 1) |
| macOS | x86_64 | tar.gz | DMG (`-Dstudio.macos.arch=x86_64`) |
| Linux | x86_64 | tar.gz | — |
| Linux | **aarch64** | tar.gz | — |
| Windows | x86_64 | zip | exe (windows profile) |

## Apple Silicon JVM note

Native `arm64` launcher must not use Oracle JDK 8 x86_64 from `/Library/Java/…`.  
Helpers: [`tools/macos/fix-macos-jvm.sh`](tools/macos/fix-macos-jvm.sh), [`tools/macos/run-studio.sh`](tools/macos/run-studio.sh).  
Prefer Homebrew **OpenJDK 25** (`osgi.requiredJavaVersion=25`).

## Roadmap pointer

Stages **0–6** completed for MVP A + fork release tooling. Optional follow-ups: JustJ JRE, ADAC stage B admin ops, GA `3.0.0` version bump.

## Stage 6 — Fork release — DONE (2026-08-08)

- [CHANGELOG.md](./CHANGELOG.md) — modernize-0 notes
- [FORK_RELEASE.md](./FORK_RELEASE.md) — release matrix & process (no ASF vote)
- [`tools/assemble-release.sh`](./tools/assemble-release.sh) — collect archives/DMGs/checksums/SBOM → `release/<version>/`
- [`.github/workflows/release.yml`](./.github/workflows/release.yml) — tag-triggered build + GitHub Release attach
- Suggested tag: **`v3.0.0-modernize.0`** (artifacts remain `3.0.0-SNAPSHOT` until GA cut)

```bash
./tools/assemble-release.sh
gh release create v3.0.0-modernize.0 --notes-file CHANGELOG.md release/3.0.0-SNAPSHOT/*
```

## Stage 4 — ADAC shell — DONE (2026-08-06)

Default RCP perspective is **ADAC** (`org.apache.directory.studio.adac.ui`):

| Pane | View |
|---|---|
| Left | Navigation (containers) + Connections |
| Center | Object list + breadcrumb + filter |
| Right | Tasks (Properties / Delete / New User·Group·OU) |

Classic **LDAP Browser** perspective remains available: **Window → Perspective → Open Perspective → LDAP**.

## Stage 5 — ADAC forms + wizards — DONE (2026-08-08)

- Sectioned **Properties** dialog (Account / Organization / Member Of; Group General+Members; OU General)
- Typed **New User / Group / OU** wizards (AD vs generic OC mapping via `ServerTypeDetector`)
- Double-click leaf in Object list opens Properties; containers navigate
- **Advanced…** opens classic entry editor
