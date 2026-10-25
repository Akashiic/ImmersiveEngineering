from pathlib import Path
import hashlib
import json
import re
import sys

left = Path(sys.argv[1])
right = Path(sys.argv[2])
out = Path(sys.argv[3])

def normalize(text: str) -> str:
    text = text.replace("\r\n", "\n")
    text = re.sub(r"/\*\s*\* Decompiled with CFR[^*]*\*/\s*", "", text)
    text = re.sub(r"\s+$", "", text, flags=re.MULTILINE)
    return text.strip() + "\n"

def collect(root: Path):
    result = {}
    for p in root.rglob("*.java"):
        rel = p.relative_to(root).as_posix()
        text = normalize(p.read_text(encoding="utf-8", errors="replace"))
        result[rel] = {
            "sha256": hashlib.sha256(text.encode()).hexdigest(),
            "text": text,
        }
    return result
a = collect(left)
b = collect(right)
added = sorted(set(a) - set(b))
removed = sorted(set(b) - set(a))
changed = sorted(k for k in set(a) & set(b) if a[k]["sha256"] != b[k]["sha256"])

report = {
    "left": str(left),
    "right": str(right),
    "leftJavaFiles": len(a),
    "rightJavaFiles": len(b),
    "added": added,
    "removed": removed,
    "changed": changed,
}
out.parent.mkdir(parents=True, exist_ok=True)
out.write_text(json.dumps(report, indent=2), encoding="utf-8")
print(json.dumps({
    "leftJavaFiles": len(a),
    "rightJavaFiles": len(b),
    "addedCount": len(added),
    "removedCount": len(removed),
    "changedCount": len(changed),
    "added": added[:80],
    "removed": removed[:80],
    "changedSample": changed[:120],
}, indent=2))
