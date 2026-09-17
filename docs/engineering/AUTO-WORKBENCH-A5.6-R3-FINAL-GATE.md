# Auto Workbench A5.6 — R3 Final Candidate Gate

Date: 2026-09-17
Status: PASS — ready for manual installation and A6 gameplay test
Tag: `auto-workbench-test-1-r3`
Commit: `5d467a612605c1201a13bde52b54e3550477ac05`

## R3 purpose
R3 supersedes rejected R2 with two narrowly scoped state fixes:
1. mark the master tile/chunk dirty whenever one or more processes advance;
2. synchronize the 32,000 RF buffer to clients viewing the Auto Workbench GUI using the established 1.7 `ICrafting` progress-property pattern.

No recipe, queue, output, registry, multiblock, rendering, model, texture or configuration semantics were changed.

## Clean tagged build
Command:
`gradlew clean build --no-daemon`

Result:
`BUILD SUCCESSFUL in 2m 55s`

23 actionable tasks: 22 executed, 1 up-to-date.
Compile, resources, JAR, reobf, sources, checkstyle, check and build all passed.
## Final R3 artifact
JAR:
`ImmersiveEngineering-auto-workbench-test-1-r3.jar`

Size: `3,806,758` bytes
SHA-256:
`B91A12EBC1D424915CB8E3DE5F3267D5699FA564BA605FAFDD9635E61CFB0A12`

Independent JAR gate:
- 1,405 archive entries;
- 747 classes;
- 747/747 class major 52 / Java 8;
- 0 duplicate ZIP entries;
- all required Auto Workbench classes/assets present.

R3 artifact auditor:
`tools/audit_auto_workbench_r3_artifact.py`

Result:
`TOTAL_PASS=21/21`
`AUTO_WORKBENCH_R3_ARTIFACT_GATE=PASS`
## Binary delta versus R2
R2 and R3 both contain exactly 1,405 entries.
R3 adds 0 entries and removes 0 entries.
Exactly four class files differ:
- `TileEntityAutoWorkbench.class`;
- `TileEntityAutoWorkbench$AutoWorkbenchProcess.class`;
- `ContainerAutoWorkbench.class`;
- `ContainerAutoWorkbench$1.class`.

`javap -c -p` proves the two nested/anonymous classes have identical logical bytecode between R2 and R3; their binary changes are line/debug metadata inherited from edits in the enclosing source files.

Public/protected ABI removed from the two edited outer classes: 0.
Common/API client-side reference count: 55 → 55; new references: 0.
Auto Workbench common-side client references: 0.

All eight Auto Workbench/Redstone Engineering assets are byte-identical to R2, including the three OBJ models.
## R3 minimal client runtime smoke
A fresh disposable Git worktree was created directly from `auto-workbench-test-1-r3`.
Optional integration dependencies were made compile-only only inside that disposable worktree; the candidate source/JAR and production modpack were not modified.

Two client initializations reached:
`Forge Mod Loader has successfully loaded 4 mods`

The IE resource pack loaded and block/item/revolver atlases were created at final sizes.
`ModelFormatException` count in the smoke log: 0.
No Auto Workbench renderer/classloading exception occurred.

The minimal runtime again reported the three known missing baseline resources (`revolver_rose`, `metal2_skycrateDispenser`, `revolver_tesla`), already proven absent from the approved Garden Cloche baseline and unrelated to this feature.

## Decision
R3 passes A5.6.
R2 remains rejected and must not be used for A6.
R3 may proceed to manual installation and A6 gameplay validation.
The production modpack is intentionally not modified by this gate.