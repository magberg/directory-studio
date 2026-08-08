#!/bin/sh
# Generate eclipse-trgt-platform/*.target from template before Tycho 5 resolves it.
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
python3 - "$ROOT" <<'PY'
import re, sys
from pathlib import Path
root = Path(sys.argv[1])
pom = (root / 'pom.xml').read_text()
props = dict(re.findall(r'<([a-zA-Z0-9_.-]+)>([^<]+)</\1>', pom))
tpl_path = root / 'eclipse-trgt-platform/template/org.apache.directory.studio.eclipse-trgt-platform.template'
out_path = root / 'eclipse-trgt-platform/org.apache.directory.studio.eclipse-trgt-platform.target'
tpl = tpl_path.read_text()
def repl(m):
    return props.get(m.group(1), m.group(0))
out = re.sub(r'\$\{([^}]+)\}', repl, tpl)
basedir = (root / 'eclipse-trgt-platform').resolve().as_uri()
out = out.replace('${basedirUri}', basedir)
out_path.write_text(out)
print(f'Wrote {out_path}')
PY
