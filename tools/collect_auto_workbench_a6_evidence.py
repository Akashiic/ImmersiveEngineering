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
JAR = PACK / "mods" / "ImmersiveEngineering-auto-workbench-test-1-r3.jar"
LOGS = [PACK / "logs" / "latest.log", PACK / "logs" / "fml-client-latest.log", PACK / "logs" / "CustomNPCs-latest.log"]
PATTERNS = ("AutoWorkbench", "ImmersiveEngineering", "ModelFormatException", "Exception", "ERROR", "FATAL")
EXPECTED_SHA = "B91A12EBC1D424915CB8E3DE5F3267D5699FA564BA605FAFDD9635E61CFB0A12"

def now():
    return dt.datetime.now().astimezone()

def installed_ie_jars():
    return sorted((PACK / "mods").glob("ImmersiveEngineering*.jar"), key=lambda x: x.name.lower())

def safe_label(label):
    cleaned = "".join(ch if ch.isalnum() or ch in "-_." else "-" for ch in label).strip("-")
    return cleaned or "test"

def sha256(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest().upper()

def validate_installed_candidate():
    jars = installed_ie_jars()
    if [x.name for x in jars] != [JAR.name]:
        raise SystemExit("A6 requires exactly one IE JAR: %s; found: %s" % (JAR.name, [x.name for x in jars]))
    jar_hash = sha256(JAR)
    if jar_hash != EXPECTED_SHA:
        raise SystemExit("Installed R3 SHA-256 mismatch: %s" % jar_hash)
    return jar_hash

def start_session(label):
    if MARKER.exists():
        raise SystemExit("An A6 session marker already exists. Collect or clear it before starting another session.")
    jar_hash = validate_installed_candidate()
    MARKER.parent.mkdir(parents=True, exist_ok=True)
    stamp = now()
    data = {"label": safe_label(label), "started": stamp.isoformat(), "jar": str(JAR), "jar_sha256": jar_hash}
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
    label = safe_label(meta.get("label", "test"))
    out = MARKER.parent / (session_id + "-" + label)

    if meta.get("jar") != str(JAR) or meta.get("jar_sha256") != EXPECTED_SHA:
        raise SystemExit("Active A6 marker does not describe the immutable R3 candidate")
    jar_hash = validate_installed_candidate()
    out.mkdir(parents=True, exist_ok=False)
    result = {"started": meta["started"], "collected": now().isoformat(), "jar_sha256": jar_hash, "expected_sha256": EXPECTED_SHA, "logs": {}, "crashes": []}

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
    MARKER.unlink()
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
    parser.add_argument("--label", default="auto-workbench-test-1-r3")
    args = parser.parse_args()
    start_session(args.label) if args.start else collect_session()
