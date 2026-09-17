# Auto Workbench A5.2 — R2 Final Tagged Artifact Gate

Date: 2026-09-16
Status: PASS — A6 USER RUNTIME TEST READY

## Immutable candidate identity
- tag: `auto-workbench-test-1-r2`
- tag target: `97cea596eed2254050f602428853007caade286d`
- JAR: `ImmersiveEngineering-auto-workbench-test-1-r2.jar`
- size: 3,806,303 bytes
- SHA-256: `C72F7C4AAD54491F83EA6073799DBEAFF44C260B8B4CBDE33249F1351820FA97`

The original `auto-workbench-test-1` remains rejected and immutable.
R2 is a new candidate; no tag was moved or overwritten.

## Clean build gate
The exact R2 tag was rebuilt with `gradlew clean build --no-daemon`.
Result: `BUILD SUCCESSFUL in 1m46s`.
26 tasks were considered; 25 executed and 1 was up-to-date.
`compileJava`, `processResources`, `jar`, `reobfJar`, `sourcesJar`, test/check and build all passed.
## Artifact structure gate
Akashic `jar_gate.py` PASS on both `build/libs` and the copied `dist` JAR.
- archive entries: 1,405
- classes: 747
- class major: 747/747 = 52 (Java 8)
- duplicate ZIP entries: 0
- required Auto Workbench classes/assets: present

`mcmod.info` remains:
- mod: Immersive Engineering
- version: `1.7.10-0.7.9`
- description: `Forbidden Series Edition`

Manifest still carries only the expected `FMLAT: ImmersiveEngineering_at.cfg` integration entry.
## Delta against approved Garden Cloche Test 2
The R2 artifact remains tightly scoped:
- baseline entries: 1,380
- R2 entries: 1,405
- added: 25
- removed: 0
- changed: 14
- added classes: 17

The 25 additions are exclusively Auto Workbench classes/assets plus the Redstone Engineering Block texture.
No library/package shading or unrelated subsystem addition appeared.

The 14 changed entries are the expected integration points: Blueprint recipe API, proxies, IEContent/IERecipes/Config/Lib, multiblock/decor block classes, renderer registration and `en_US.lang`.
## Final OBJ/resource gate
Every new packaged asset is byte-identical to its audited source resource.

Corrected static OBJ hashes inside the final JAR:
- normal: `263C39C0861D841A8A593672062DFB8C3A7ACF3F2B3409C18C76B9F290412434`
- mirrored: `D2DD6FFC9616B253E2C17F995A5844F9C301EA732179B5C59E21DB61D96B80F0`
- animated: `68B20FBDEF15804F488621C9CBFE6CE2BFF5922D2726E8464E6A48F5A6705C09`

Topology inside final JAR:
- normal: 338 faces, all arity 4
- mirrored: 338 faces, all arity 4
- animated: 98 faces, all arity 4

This closes the Forge 1.7 mixed triangle/quad parser failure recorded in A5.1.
## Common/client and ABI gates
Compared with the approved Garden Cloche Test 2 baseline:
- common/API classes with `net.minecraft.client.*` references: 55 -> 55
- new common/API client references: 0
- Auto Workbench common-side client references: 0

Public ABI comparison on all touched public integration classes found zero removed public members/signatures.
The automation extension is additive rather than an ABI replacement.

Registry identity remains append-only:
- Metal Press meta 13 -> Auto Workbench meta 14
- Steel Sheet meta 2 -> Redstone Engineering Block meta 3
- Garden Cloche GUI 13 -> Auto Workbench GUI 14

No previously assigned meta or GUI ID moved.
## Corrected runtime smoke evidence
The corrected minimal runtime log contains no `ModelFormatException` and no `[ERROR]` entries.
It records:
- `Forge Mod Loader has successfully loaded 4 mods`;
- successful IE resource initialization and atlas creation;
- `FMLServerStartingEvent` for Immersive Engineering;
- `FMLServerStartedEvent` for Immersive Engineering;
- `FMLServerStoppingEvent` for Immersive Engineering;
- `FMLServerStoppedEvent` for Immersive Engineering.

This demonstrates lifecycle/classloading/resource-render initialization for the corrected candidate.
It does not replace gameplay Test 1: formation, GUI interaction, RF processing, queue timing, automation, mirrored output, teardown and save/reload still require A6 evidence.
## Decision
**A5.2 PASS.**

`auto-workbench-test-1-r2` is approved as the sole Auto Workbench candidate for **A6 user runtime Test 1**.
It is not a stable/release candidate yet.
A7 and A8 remain blocked until A6 gameplay evidence is reviewed.

Distribution package:
`C:\Users\User\Desktop\Codex\immersiveengineering-backport\dist\auto-workbench-test-1-r2`

The production Forbidden Series `mods` directory is not modified by this gate.