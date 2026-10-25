# Auto Workbench A6 — Evidence Capture Protocol

Status: READY — gameplay not yet executed
Candidate: `auto-workbench-test-1-r3`
Expected SHA-256: `B91A12EBC1D424915CB8E3DE5F3267D5699FA564BA605FAFDD9635E61CFB0A12`

## Purpose
A6 remains a real gameplay gate. Automated startup/static evidence cannot replace formation, GUI, crafting, RF, queue, automation, mirroring, teardown and save/reload validation.

The evidence helper is:
`tools/collect_auto_workbench_a6_evidence.py`

It does not modify the modpack, world or JAR.

## Fail-fast candidate gate
Before creating a session marker, `--start` requires all of the following:
- exactly one `ImmersiveEngineering*.jar` in the Forbidden Series `mods` directory;
- exact filename `ImmersiveEngineering-auto-workbench-test-1-r3.jar`;
- exact SHA-256 shown above;
- no existing active A6 marker.

If any condition fails, the command exits non-zero and creates no marker.
As of the 2026-09-17 engineering check, the real modpack still contains R2, so A6 intentionally refuses to start until the user manually installs R3.
## Before the gameplay session
After manually installing R3, run from the project root:
`python tools/collect_auto_workbench_a6_evidence.py --start --label auto-workbench-test-1-r3`

The marker records timestamp, exact JAR path and exact JAR SHA-256 outside the modpack.

## During gameplay
Execute the authoritative matrix:
`docs/AUTO_WORKBENCH_TEST_1.md`

Record manual observations for each T0–T18 item, especially any visual defect, unexpected item movement, wrong recipe/output, duplication/loss, queue anomaly or save/reload discrepancy.

Do not change IE JARs during the session.
Do not load two Immersive Engineering JARs simultaneously.

## After exiting Minecraft
Run:
`python tools/collect_auto_workbench_a6_evidence.py --collect`

Before creating an evidence directory, collection revalidates the marker and the currently installed R3 filename/hash. A mismatch aborts without creating false evidence.

The collector streams the current client logs, extracts Auto Workbench/IE/error/exception lines, stores the last 500 lines of each source log and copies crash reports created after the session marker.
Evidence is written under:
`evidence/auto-workbench-a6/<session-timestamp>-<label>/`

A JSON manifest records source sizes/times, filtered-line counts, crash hashes and the exact R3 SHA-256. On successful collection the active marker is removed; on failure it remains available for diagnosis/recovery.

## Collector self-test
The sandbox regression test is:
`python tools/test_auto_workbench_a6_collector.py`

Verified result on 2026-09-17:
`AUTO_WORKBENCH_A6_COLLECTOR_SANDBOX=PASS`

The sandbox test uses a temporary pack, an exact copy of the R3 JAR, synthetic logs and a synthetic crash report. It verifies start, hash gating, label sanitization, filtering, tail capture, crash capture, manifest creation and marker cleanup without touching the real modpack.

## Interpretation rule
Filtered logs prove only what they explicitly contain. Absence of exceptions is not proof that gameplay semantics are correct.
Manual T0–T18 observations and resulting world behavior remain required evidence.

A6 can pass only after both are reviewed:
1. manual gameplay results;
2. captured logs/crash evidence for the same session.

If either side conflicts with the other, A6 is rejected and the discrepancy becomes an A7 defect to reproduce and fix.