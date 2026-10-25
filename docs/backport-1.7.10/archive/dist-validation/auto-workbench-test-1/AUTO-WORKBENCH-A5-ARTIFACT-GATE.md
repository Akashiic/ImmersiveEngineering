# Auto Workbench — A5 Artifact Gate

Date: 2026-09-10
Status: PASS for pre-tag candidate; tagged candidate must repeat this gate
Branch: `feature/auto-workbench`
Reference baseline: `garden-cloche-test-2`
Audited implementation commit: `f7d638cbef4f96d1fffa7537bb9222aad0777d19`

## Purpose
A5 verifies the built/reobfuscated artifact independently of source compilation.
A successful Gradle build alone is not approval.

## Clean build gate
Command:
`gradlew clean build --no-daemon`

Result:
- BUILD SUCCESSFUL in 1m47s;
- 23 actionable tasks;
- 22 executed, 1 up-to-date;
- `compileJava`: PASS;
- `jar`: PASS;
- `reobfJar`: PASS;
- `sourcesJar`: PASS;
- `checkstyleMain`: PASS;
- `check`: PASS;
- `build`: PASS.
## Pre-tag artifact
Artifact:
`ImmersiveEngineering-garden-cloche-test-2-feature-auto-workbench.9+f7d638cbef.jar`

Evidence:
- size: 3,806,297 bytes;
- SHA-256: `3FEB972626695DD4C3221B262A65C595D34C6D17582E82BAA23B456633A31AD4`;
- archive entries: 1,405;
- class entries: 747;
- Java class majors: 747/747 = 52 (Java 8);
- duplicate ZIP entries: 0.

Identity remained unchanged:
- `mcmod.info` modid: `ImmersiveEngineering`;
- version: `1.7.10-0.7.9`;
- description: `Forbidden Series Edition`;
- Minecraft: `1.7.10`;
- manifest FMLAT: `ImmersiveEngineering_at.cfg`.

`ImmersiveEngineering.class`, manifest and `mcmod.info` are byte-identical to the approved Test 2 baseline because they do not appear in the changed-entry set.
## Baseline ZIP delta
Compared by uncompressed entry bytes against `garden-cloche-test-2`:
- added entries: 25;
- removed entries: 0;
- changed entries: 14;
- no unexpected library/package tree was added.

The 25 additions are exactly the Auto Workbench feature surface:
- 17 new `.class` entries;
- 3 converted OBJ models;
- 1 Redstone Engineering Block texture;
- 2 Auto Workbench block/blueprint textures;
- 1 Auto Workbench GUI;
- 1 dedicated recipe-button texture.

Changed classes correspond to intentional integration points only:
`BlueprintCraftingRecipe`, `ClientProxy`, `BlockRenderMetalMultiblocks`,
`CommonProxy`, `Config`, `IEContent`, `IERecipes`,
`BlockMetalDecoration2`, `BlockMetalMultiblocks` and `Lib`.

`IEContent$1`, `$2` and `$3` also changed as class files, but `javap -c -p`
proved their logical bytecode identical to the baseline; their delta is rebuild/debug metadata only.
## Dedicated-side boundary
Raw constant-pool comparison was run for all `common/` and `api/` classes.

Result:
- baseline common/api client-reference candidates: 55;
- candidate common/api client-reference candidates: 55;
- new common/api client-reference classes: 0;
- removed common/api client-reference classes: 0;
- Auto Workbench common-side classes with `net/minecraft/client/`: 0.

Direct bytecode checks also confirmed Java major 52 and zero client refs for:
- `TileEntityAutoWorkbench`;
- `MultiblockAutoWorkbench`;
- `CommonProxy`.

## Public ABI gate
`javap -public` baseline signatures were required to remain a subset of the candidate.

Missing baseline public members: **0** for:
- `BlueprintCraftingRecipe`;
- `Config`;
- `IEContent`;
- `IERecipes`;
- `BlockMetalDecoration2`;
- `BlockMetalMultiblocks`;
- `Lib`;
- `CommonProxy`.
## Asset packaging gate
All eight new runtime assets are byte-identical between source resources and the reobfuscated JAR.

SHA-256 highlights:
- `autoWorkbench.obj`: `8E811D7556D7F13CF9120E648169C96143186AC57F904F6FD16487D516114B55`;
- `autoWorkbenchAnimated.obj`: `68B20FBDEF15804F488621C9CBFE6CE2BFF5922D2726E8464E6A48F5A6705C09`;
- `autoWorkbenchMirrored.obj`: `60E494D2C863B18B9A1A6ECA5CD339D3FF38D6545C45C123F015D4BA6C5DE8D4`;
- RS Engineering texture: `B2C18643E7235D85530DA40BBD4575BFBBCBD217F9BB8DAEAE1A5853E9597DE4`;
- base Workbench texture: `1FC3BCC0D64AEC325749B1A77D4CAB367764B587C7D420DCF2554745AEE1A90C`;
- blueprint texture: `2A4BC2B90935D6D10E1545E6705ED516BB9BD8BDA2D4D087C7226BC41A743121`;
- GUI: `CDACD4D500DA31E86349E440A9089EA562AFAA93AF6C591DD87D2645AD6D24FB`;
- dedicated button sheet: `A608EF5CFC5A1AB44C5A1A2465879405145EFE40A96EEA645C4E6565A430EA06`.

The three OBJ outputs also reproduce deterministically from the official 0.12 source assets.
Geometry/UV records remain unchanged; materials are converted to Forge-1.7-visible groups and source V is preserved because Forge 1.7 performs the V flip itself.
## Structure and processing gates
`tools/audit_auto_workbench_structure.py` proves all 8 facing/mirror combinations:
- 18 unique structure coordinates;
- master = pos 13;
- energy/item input = pos 9;
- redstone control = pos 1;
- output is two blocks from the master on the correct mirrored/non-mirrored side;
- local offsets are exactly equal to the official 0.12 offset formula.

Queue semantics were checked against `TileEntityMultiblockMetal`:
- max queue length = 3;
- max processes advanced per tick = 3;
- minimum process distance = 0.4375;
- no process-stacking path is used by Auto Workbench admission;
- transformation point = 0.78;
- base time = 180 ticks;
- base energy = 23,040 RF;
- default energy/tick = 128 RF.

Upstream also calculates energy/tick by integer division. The legacy adapter preserves that behavior.
## Deliberate hardenings
These are documented deviations, not claims of exact upstream parity:
- process time is clamped to at least 1 tick, preventing the official minimum modifier from flooring 180 ticks to zero;
- total process energy is clamped to at least 1 RF;
- recipe consumption is transactional on a defensive inventory copy;
- invalid persisted process payloads fail closed instead of recreating partial state;
- broken-part drop respects `doTileDrops`;
- blueprint point hashing avoids the `java.awt.Point` collision problem later fixed upstream.

## A5 decision
**PASS** for the pre-tag artifact.

Not yet approved as a runtime release. Remaining gates require the exact tagged Test 1 JAR to prove:
- client startup and dedicated-server startup;
- real multiblock formation/disassembly;
- GUI interaction and Blueprint selection;
- live RF/item automation;
- output insertion/backpressure;
- save/reload persistence;
- normal and mirrored renderer behavior;
- Blueprint projection;
- Angelica/full-pack compatibility;
- regression smoke test of Garden Cloche and existing 0.7.9 machines.

The final tagged artifact must be rebuilt from a clean tree and re-audited; the pre-tag SHA must not be reused.
