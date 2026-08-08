#!/usr/bin/env python3
# SPDX-License-Identifier: Apache-2.0
"""Remove geronimo-stax-api from materialized Studio products.

Shipping geronimo-stax next to Equinox causes a javax.xml.stream uses-constraint
violation that prevents org.apache.directory.studio.connection.core from resolving.
"""
from __future__ import annotations

import sys
from pathlib import Path


def main(products_dir: Path) -> int:
    if not products_dir.is_dir():
        print(f"No products dir: {products_dir}", file=sys.stderr)
        return 1

    removed_jars = 0
    for jar in products_dir.rglob("org.apache.geronimo.specs.geronimo-stax-api*.jar"):
        print(f"Removing {jar}")
        jar.unlink()
        removed_jars += 1

    patched_infos = 0
    for info in products_dir.rglob("bundles.info"):
        if info.parent.name != "org.eclipse.equinox.simpleconfigurator":
            continue
        text = info.read_text(encoding="utf-8")
        lines = [ln for ln in text.splitlines() if "org.apache.geronimo.specs.geronimo-stax-api" not in ln]
        new_text = "\n".join(lines) + ("\n" if text.endswith("\n") else "")
        if new_text != text:
            info.write_text(new_text, encoding="utf-8")
            print(f"Patched {info}")
            patched_infos += 1

    print(f"Done: removed {removed_jars} jar(s), patched {patched_infos} bundles.info")
    return 0


if __name__ == "__main__":
    root = Path(sys.argv[1] if len(sys.argv) > 1 else "target/products")
    raise SystemExit(main(root.resolve()))
