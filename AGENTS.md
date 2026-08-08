<!--
SPDX-License-Identifier: Apache-2.0

Licensed to the Apache Software Foundation (ASF) under the Apache License, Version 2.0.
See the umbrella threat model and SECURITY.md for security guidance.
-->

# Agent Guide for directory-studio

## Security

Security model: [SECURITY.md](./SECURITY.md) -> the Apache Directory umbrella threat
model at https://github.com/apache/directory-server/blob/master/THREAT_MODEL.md

This repository is the Eclipse-based LDAP client tool (desktop). Agents scanning it should consult the umbrella threat
model (client-tooling note) before reporting issues.

## Compile / IDE truth (P0)

This is a **Tycho / OSGi / Eclipse RCP** tree. Cursor’s Java Language Server cannot resolve the Eclipse target platform or `Require-Bundle` graph, so the Problems panel often shows **tens of thousands of phantom “cannot be resolved” diagnostics**.

- **Source of truth for compile errors:** Maven/Tycho — `./build.sh` or `mvn install -DskipTests` (after `./tools/bootstrap-target.sh` / `pom-first` as needed).
- **Do not** treat IDE Problems as merge blockers or fix lists.
- **Do not** try to clear unresolved `org.eclipse.*` / sibling-plugin imports in Cursor one-by-one.
- Prefer tasks by **module + goal** (e.g. “fix TLS dialog UX in connection.ui”); validate with the build and a product run.
- Workspace settings that quiet JDT noise live in [`.vscode/settings.json`](./.vscode/settings.json); see [FORK.md](./FORK.md) § IDE / Cursor.
