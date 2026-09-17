# Auto Workbench A5.4 — Reproducible R2 Regression Gate

Date: 2026-09-17
Status: PASS — 20/20 checks
Candidate: `auto-workbench-test-1-r2`

## Purpose
A5.4 consolidates the previously separate static/artifact checks into one deterministic read-only command.
It does not replace A6 gameplay testing.

Tool:
`tools/run_auto_workbench_r2_regression.py`

The tool does not modify the production modpack. It only reads Git state, audited artifacts, the production JAR, ZIP contents and existing source-side audit tools.

## Candidate identity checks
PASS:
- distribution JAR exists;
- production R2 JAR exists;
- tag resolves to `97cea596eed2254050f602428853007caade286d`;
- tag is an ancestor of current HEAD;
- distribution SHA-256 matches the immutable R2 hash;
- production SHA-256 matches the same hash;
- production and distribution JARs are byte-identical;
- exactly one `ImmersiveEngineering*.jar` exists in the production mods directory.
## Artifact checks
PASS:
- 1,405 ZIP entries with zero duplicate names;
- all required Auto Workbench classes/assets present;
- all packaged classes use class major 52 / Java 8;
- `mcmod.info` remains `1.7.10-0.7.9` and `Forbidden Series Edition`;
- normal OBJ SHA-256: `263C39C0861D841A8A593672062DFB8C3A7ACF3F2B3409C18C76B9F290412434`;
- mirrored OBJ SHA-256: `D2DD6FFC9616B253E2C17F995A5844F9C301EA732179B5C59E21DB61D96B80F0`;
- animated OBJ SHA-256: `68B20FBDEF15804F488621C9CBFE6CE2BFF5922D2726E8464E6A48F5A6705C09`.

## Source-side audit bundle
PASS:
- `audit_auto_workbench_obj_topology.py`;
- `audit_auto_workbench_transform.py`;
- `audit_auto_workbench_structure.py`;
- `harden_auto_workbench_blueprint_points.py`.

Combined result:
`TOTAL_PASS=20/20`
`AUTO_WORKBENCH_R2_REGRESSION_GATE=PASS`

## Decision
A5.4 PASS. R2 remains the only candidate authorized for A6.
A6 must still demonstrate actual formation, GUI, Blueprint selection, RF processing, queue behavior, automation, mirroring, output, teardown and save/reload in gameplay.