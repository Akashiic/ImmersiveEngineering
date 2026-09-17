#!/usr/bin/env python3
from pathlib import Path
import argparse
import datetime as dt
import hashlib
import json
import shutil
from collections import deque

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport")
PACK = Path(r"C:\Users\User\AppData\Roaming\.technic\modpacks\forbidden-series")
MARKER = ROOT / "evidence" / "auto-workbench-a6" / "ACTIVE_SESSION.json"
JAR = PACK / "mods" / "ImmersiveEngineering-auto-workbench-test-1-r2.jar"
LOGS = [PACK / "logs" / "latest.log", PACK / "logs" / "fml-client-latest.log", PACK / "logs" / "CustomNPCs-latest.log"]
PATTERNS = ("AutoWorkbench", "ImmersiveEngineering", "ModelFormatException", "Exception", "ERROR", "FATAL")
EXPECTED_SHA = "C72F7C4AAD54491F83EA6073799DBEAFF44C260B8B4CBDE33249F1351820FA97"

def now():
    return dt.datetime.now().astimezone()

def sha256(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest().upper()
def start_session(label):
    MARKER.parent.mkdir(parents=True, exist_ok=True)
    stamp = now()
    data = {"label": label, "started": stamp.isoformat(), "jar": str(JAR)}
    MARKER.write_text(json.dumps(data, indent=2), encoding="utf-8")
    print("A6_SESSION_START=PASS")
    print("started=" + data["started"])
    print("marker=" + str(MARKER))

def filtered_copy(source, target):
    count = 0
    matched = 0
    with open(source, "r", encoding="utf-8", errors="replace") as src, open(target, "w", encoding="utf-8", newline="\n") as dst:
        for line in src:
            count += 1
            if any(pattern in line for pattern in PATTERNS):
                dst.write(line.rstrip("\r\n") + "\n")
                matched += 1
    return count, matched

def tail_copy(source, target, max_lines=500):
    with open(source, "r", encoding="utf-8", errors="replace") as src:
        lines = deque(src, maxlen=max_lines)
    target.write_text("".join(lines), encoding="utf-8")
    return len(lines)
def collect_session():
    if not MARKER.is_file():
        raise SystemExit("No active A6 session marker. Run with --start first.")
    meta = json.loads(MARKER.read_text(encoding="utf-8"))
    started = dt.datetime.fromisoformat(meta["started"])
    session_id = started.strftime("%Y%m%d-%H%M%S")
    label = meta.get("label", "test").replace(" ", "-")
    out = MARKER.parent / (session_id + "-" + label)
    out.mkdir(parents=True, exist_ok=False)

    jar_hash = sha256(JAR) if JAR.is_file() else None
    result = {"started": meta["started"], "collected": now().isoformat(), "jar_sha256": jar_hash, "expected_sha256": EXPECTED_SHA, "logs": {}, "crashes": []}
    if jar_hash != EXPECTED_SHA:
        raise SystemExit("Production IE JAR does not match R2 before collection")

    for source in LOGS:
        if not source.is_file():
            continue
        filtered = out / (source.stem + "-filtered.log")
        tail = out / (source.stem + "-tail.log")
        total, matched = filtered_copy(source, filtered)
        tail_lines = tail_copy(source, tail)
        result["logs"][source.name] = {"source_size": source.stat().st_size, "source_mtime": dt.datetime.fromtimestamp(source.stat().st_mtime).astimezone().isoformat(), "lines_scanned": total, "filtered_lines": matched, "tail_lines": tail_lines, "filtered_sha256": sha256(filtered)}
    crash_dir = PACK / "crash-reports"
    if crash_dir.is_dir():
        for crash in sorted(crash_dir.glob("*.txt")):
            modified = dt.datetime.fromtimestamp(crash.stat().st_mtime).astimezone()
            if modified >= started:
                target = out / crash.name
                shutil.copy2(crash, target)
                result["crashes"].append({"name": crash.name, "mtime": modified.isoformat(), "sha256": sha256(target)})

    manifest = out / "A6-EVIDENCE.json"
    manifest.write_text(json.dumps(result, indent=2), encoding="utf-8")
    print("A6_EVIDENCE_COLLECTION=PASS")
    print("output=" + str(out))
    print("jar_sha256=" + jar_hash)
    print("logs=" + str(len(result["logs"])))
    print("crashes=" + str(len(result["crashes"])))

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--start", action="store_true")
    group.add_argument("--collect", action="store_true")
    parser.add_argument("--label", default="auto-workbench-test-1-r2")
    args = parser.parse_args()
    start_session(args.label) if args.start else collect_session()
