import argparse
import json
from collections import Counter
from pathlib import Path


def audit_obj(path: Path):
    counters = Counter()
    arity = Counter()
    groups = []
    materials = []
    for raw in path.read_text(encoding="utf-8", errors="replace").splitlines():
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        token = line.split(maxsplit=1)[0]
        if token in {"v", "vt", "vn", "f", "g", "o", "usemtl", "mtllib"}:
            counters[token] += 1
        if token == "f":
            arity[len(line.split()) - 1] += 1
        elif token == "g":
            groups.append(line[2:].strip())
        elif token == "usemtl":
            materials.append(line[7:].strip())
    return {
        "path": str(path),
        "bytes": path.stat().st_size,
        "records": dict(counters),
        "face_arity": {str(k): v for k, v in sorted(arity.items())},
        "groups": groups,
        "materials": materials,
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("paths", nargs="+", type=Path)
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()

    report = [audit_obj(path) for path in args.paths]
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(report, indent=2), encoding="utf-8")

    for entry in report:
        r = entry["records"]
        print(
            f"{Path(entry['path']).name}: bytes={entry['bytes']} "
            f"v={r.get('v', 0)} vt={r.get('vt', 0)} vn={r.get('vn', 0)} "
            f"faces={r.get('f', 0)} groups={r.get('g', 0)} "
            f"materials={r.get('usemtl', 0)} arity={entry['face_arity']}"
        )


if __name__ == "__main__":
    main()
