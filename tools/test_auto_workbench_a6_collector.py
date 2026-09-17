#!/usr/bin/env python3
from pathlib import Path
import importlib.util
import json
import shutil
import tempfile
import time

HERE = Path(__file__).resolve().parent
COLLECTOR = HERE / "collect_auto_workbench_a6_evidence.py"
R3 = HERE.parent / "build" / "libs" / "ImmersiveEngineering-auto-workbench-test-1-r3.jar"
spec = importlib.util.spec_from_file_location("a6collector", str(COLLECTOR))
mod = importlib.util.module_from_spec(spec)
spec.loader.exec_module(mod)

with tempfile.TemporaryDirectory(prefix="awb-a6-") as tmp:
    root = Path(tmp)
    pack = root / "pack"
    mods = pack / "mods"
    logs = pack / "logs"
    crashes = pack / "crash-reports"
    evidence = root / "evidence"
    for p in (mods, logs, crashes, evidence):
        p.mkdir(parents=True, exist_ok=True)
    target = mods / R3.name
    shutil.copy2(R3, target)
    (logs / "latest.log").write_text("boot\n[INFO] AutoWorkbench formed\n[ERROR] synthetic-test-line\n", encoding="utf-8")
    (logs / "fml-client-latest.log").write_text("ImmersiveEngineering test\nnormal line\n", encoding="utf-8")
    mod.PACK = pack
    mod.MARKER = evidence / "ACTIVE_SESSION.json"
    mod.JAR = target
    mod.LOGS = [logs / "latest.log", logs / "fml-client-latest.log"]
    mod.start_session("r3 sandbox/label")
    assert mod.MARKER.is_file()
    marker = json.loads(mod.MARKER.read_text(encoding="utf-8"))
    assert marker["jar_sha256"] == mod.EXPECTED_SHA
    assert marker["label"] == "r3-sandbox-label"
    time.sleep(0.02)
    crash = crashes / "crash-synthetic.txt"
    crash.write_text("synthetic crash evidence\n", encoding="utf-8")
    mod.collect_session()
    assert not mod.MARKER.exists()
    outputs = [p for p in evidence.iterdir() if p.is_dir()]
    assert len(outputs) == 1, outputs
    out = outputs[0]
    manifest = json.loads((out / "A6-EVIDENCE.json").read_text(encoding="utf-8"))
    assert manifest["jar_sha256"] == mod.EXPECTED_SHA
    assert manifest["expected_sha256"] == mod.EXPECTED_SHA
    assert len(manifest["logs"]) == 2
    assert len(manifest["crashes"]) == 1
    filtered = (out / "latest-filtered.log").read_text(encoding="utf-8")
    assert "AutoWorkbench formed" in filtered
    assert "synthetic-test-line" in filtered
    assert "boot" not in filtered
    assert (out / "latest-tail.log").is_file()
    assert (out / "crash-synthetic.txt").is_file()
    print("AUTO_WORKBENCH_A6_COLLECTOR_SANDBOX=PASS")