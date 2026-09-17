# Auto Workbench A6 — Evidence Capture Protocol

Status: READY — gameplay not yet executed
Candidate: `auto-workbench-test-1-r2`
Expected SHA-256: `C72F7C4AAD54491F83EA6073799DBEAFF44C260B8B4CBDE33249F1351820FA97`

## Purpose
A6 remains a real gameplay gate. Automated startup/static evidence cannot replace formation, GUI, crafting, RF, queue, automation, mirroring, teardown and save/reload validation.

The helper below makes the gameplay session auditable without adding diagnostic code to the R2 JAR:
`tools/collect_auto_workbench_a6_evidence.py`

It does not modify the modpack, world or JAR.

## Before the gameplay session
Run from the project root:
`python tools/collect_auto_workbench_a6_evidence.py --start --label auto-workbench-test-1-r2`

The command first records a timestamp marker outside the modpack.
The installed R2 JAR is later required to match the immutable expected hash before evidence collection can succeed.
## During gameplay
Execute the authoritative matrix:
`docs/AUTO_WORKBENCH_TEST_1.md`

Record manual observations for each T0–T18 item, especially any visual defect, unexpected item movement, wrong recipe/output, duplication/loss, queue anomaly or save/reload discrepancy.

Do not change IE JARs during the session.
Do not load two Immersive Engineering JARs simultaneously.

## After exiting Minecraft
Run:
`python tools/collect_auto_workbench_a6_evidence.py --collect`

The collector streams the current client logs, extracts Auto Workbench/IE/error/exception lines, stores the last 500 lines of each source log and copies crash reports created after the session marker.

Evidence is written under:
`evidence/auto-workbench-a6/<session-timestamp>-<label>/`

A JSON manifest records source sizes/times, filtered-line counts, crash hashes and the exact production JAR SHA-256.
## Interpretation rule
Filtered logs prove only what they explicitly contain. Absence of exceptions is not proof that gameplay semantics are correct.
Manual T0–T18 observations and resulting world behavior remain required evidence.

A6 can pass only after both are reviewed:
1. manual gameplay results;
2. captured logs/crash evidence for the same session.

If either side conflicts with the other, A6 is rejected and the discrepancy becomes an A7 defect to reproduce and fix.