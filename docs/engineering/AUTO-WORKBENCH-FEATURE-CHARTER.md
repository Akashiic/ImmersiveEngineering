# Feature Charter — Automated Engineer's Workbench

Date: 2026-09-09
Status: APPROVED FOR IMPLEMENTATION PLANNING
Feature number: #2 after Garden Cloche
Selected by: `MODERN-FEATURE-GAP-AUDIT.md`

## Mission
Backport the official Automated Engineer's Workbench to the Forbidden Series Immersive Engineering 0.7.9 baseline while preserving existing worlds, registry semantics, Java 8 compatibility and the existing 1.7.10 Blueprint ecosystem.

The result must feel native to Forbidden 0.7.9 rather than like a transplanted Forge 1.12 implementation.

## Source contract
Use sources in this order:
1. Current recovered 0.7.9 source and runtime behavior for platform contracts.
2. Official `0.10-54` implementation for the feature generation closest to the legacy architecture.
3. Official fixes through 0.10/0.11 for known early failures.
4. Official `0.12-89` for mature behavior, GUI, structure and processing semantics.
5. Later official history only for bug/edge-case lessons applicable to the backport.

Never import a modern API solely to make copied source compile.

## Verified upstream milestones
- `f3ef6ffb3fd0656f56e63b14c0a17078677de1e7` — Blueprint automation time/energy semantics.
- `0641eecbfdf2307941b3f48d3f6e11affb90a36d` — initial Automated Workbench.
- `3df3d764d709eede70fd1f3f76affa8fd31fcfe0` — mirroring/GUI/output direction.
- `5d1d10a09` — crash-path cleanup associated with issue #1833.
- `d6f4f386d43af2c070d2a675eaceeb14bffc20c6` — item handler offset and Blueprint NBT fix.
- `37c7e913a` — disassembly conveyor orientation fix.
## Mature gameplay semantics
Target defaults from the mature implementation:
- multiblock footprint: 2×3×3;
- internal energy capacity: 32,000;
- blueprint automation energy: 23,040 per craft;
- blueprint automation time: 180 ticks per craft;
- inventory: 1 Blueprint slot + 16 component slots;
- selected recipe: index or `-1` for disabled;
- maximum process queue: 3;
- redstone control can stop new processing;
- automatic component insertion must exclude the Blueprint slot;
- structure supports mirrored formation;
- output direction follows facing/mirroring;
- output insertion precedes world-item fallback.

## Verified structure materials
Mature 1.12 structure totals 18 blocks:
- 5 Steel Scaffolding;
- 2 Treated Wood Slabs;
- 1 Redstone Engineering Block;
- 4 Light Engineering Blocks;
- 2 Heavy Engineering Blocks;
- 4 normal Conveyors.

The exact 1.7.10 block/meta mapping must be resolved against the local registry before implementation. OreDictionary aliases should be used only where the existing multiblock convention already does so.

## Baseline systems to reuse
- `BlueprintCraftingRecipe` category/recipe registry.
- `ItemEngineersBlueprint` and existing Blueprint NBT conventions.
- `TileEntityMultiblockPart` master/dummy patterns.
- CoFH RF `EnergyStorage` / `IEnergyReceiver`.
- `IInventory` / `ISidedInventory` automation.- existing conveyor tiles and inventory insertion utilities;
- existing GUI/container/network patterns;
- existing manual/multiblock renderer conventions;
- Metal Press and Assembler as 1.7.10 reference implementations for process state, NBT and output handling.

## Required legacy adaptation
### Blueprint recipe
Do not globally replace the legacy `BlueprintCraftingRecipe` API with the 1.12 `MultiblockRecipe` hierarchy.

Add only the automation semantics needed by this feature, with compatibility for existing callers. Existing manual Blueprint crafting must remain behaviorally unchanged.

### Processing
Implement a legacy-native process queue rather than backporting the full generic `TileEntityMultiblockMetal` framework as a prerequisite.

Process state must survive world save/reload without duplicating consumed inputs or output.

### Inventory automation
Forge 1.12 item capabilities are not a 1.7.10 dependency. Use sided inventory contracts already used by Forbidden machines.

The machine must simulate/validate a craft before committing ingredient consumption. Failed queue insertion or unavailable recipe must not consume inputs.

### Output safety
Prefer transactional semantics:
1. determine valid craft;
2. reserve/consume required inputs exactly once;
3. process using persisted state;
4. attempt destination inventory insertion;
5. drop only the true remainder.

Never delete output because the adjacent inventory is full or missing.
## Registry and save compatibility gate
Current `BlockMetalMultiblocks` exposes metas 0–13. Source audit found no current `case 14` and no direct multiblock stack use at meta 14.

Therefore meta 14 is a **provisional candidate**, not yet an assigned production ID.

Before code registration:
- search source/resources/configs for hidden/reserved use of metas 14/15;
- inspect production/recovered registry behavior;
- document the registry delta;
- prove no existing meta is remapped;
- verify an existing Forbidden world loads without missing/remapped IE blocks.

TileEntity registration should follow the current `IEContent.registerTile` convention, which derives IDs as `ImmersiveEngineering:<ClassNameWithoutTileEntity>`.

A final TileEntity ID is approved only after checking collision absence in the exact runtime.

## NBT contract
At minimum persist on the master:
- formed/master-part metadata through existing multiblock base contract;
- facing and mirror state;
- 17-slot inventory;
- selected recipe;
- energy storage;
- active process queue including progress and immutable recipe identity/input reservation data;
- redstone-disabled state only if the local control model requires explicit persistence.

Use a feature data-version key if new NBT shape may need migration before public release.

Dummy parts must not become independent owners of inventory/process data.

## Client/server boundary
All GUI/model/render code must remain client-only under the same 1.7.10 side rules used elsewhere in Forbidden IE.

Dedicated-server bytecode audit is mandatory before Test 1. No common/server path may require `net.minecraft.client.*`, renderer classes, `IIcon` client use, or other client-only initialization.
## Rendering contract
The Auto Workbench is intentionally a visual upgrade over Garden Cloche complexity: formed multiblock geometry plus moving recipe components.

Rendering work must be split into two proofs:
1. structural/model parity and mirrored orientation;
2. process-item animation correctness.

Angelica is an explicit smoke-test target. No modern baked-model assumption may be imported unless the 1.7 renderer is deliberately adapted.

The historical mirrored-render and output-direction fixes are regression cases, not optional polish.

## Known historical failure gates
Test 1 must explicitly challenge:
- selecting a Blueprint recipe with zero matching ingredients — no crash;
- invalid or removed Blueprint category — no crash/consumption;
- removing/replacing Blueprint while a process is active;
- selecting/deselecting recipes rapidly;
- Blueprint NBT persistence and GUI synchronization;
- automated insertion into component slots without touching slot 0;
- missing master/dummy access during chunk unload or block break;
- mirrored formation and mirrored output direction;
- disassembly restoring conveyor orientation correctly;
- full/blocked destination inventory;
- save/reload at multiple process progress values;
- three queued processes and a fourth attempted insertion;
- redstone disable/reenable while queued/active;
- repeated operation for duplication or loss.

## Performance gate
Profile idle and active cost with at least 1, 16 and 64 formed machines.

The 16-tick scheduling behavior from mature IE is a reference optimization. Any deviation requires measured evidence that it improves 1.7 behavior rather than merely changing timing.
## Explicit non-goals for Feature #2
- Mixer or Alloy Kiln in the same branch.
- Conveyor-system overhaul.
- Redstone wire-network overhaul.
- Replacing `IInventory` with a home-grown Capability clone.
- Rewriting the Assembler.
- Changing existing Blueprint recipes or their manual crafting results unless a proven bug requires it.
- Adding unrelated 1.12 decorations/equipment while touching registry files.
- Automatically copying the candidate JAR into the production modpack.

## Development gates
A0 — Source/asset archaeology and exact 0.10-54 → mature delta.
A1 — Registry/meta/TileEntity/GUI-ID design approved.
A2 — Legacy architecture + NBT/process transaction design approved.
A3 — Core machine implementation compiles.
A4 — Renderer/GUI/assets compile and dedicated-server bytecode audit passes.
A5 — Clean `reobfJar` + artifact audit.
A6 — Auto Workbench Test 1 runtime matrix.
A7 — Adversarial fix round and Test 2 candidate.
A8 — Runtime approval, tag, SHA-256, documentation freeze.

Each failed gate returns to the smallest responsible layer.

## Definition of success
Feature #2 is approved only when a real Forbidden Series runtime demonstrates correct Blueprint selection, automated ingredient intake, energy/time processing, mirrored formation, safe output, save/reload persistence and repeated operation with no visible item loss/duplication or client/server exceptions.

Compilation alone is not success.

## Immediate next action
Create the implementation branch only after this charter and the feature-gap audit are committed. Begin A0 by extracting the exact 0.10-54 source/assets and constructing a patch-by-patch delta through the mature implementation before writing production code.