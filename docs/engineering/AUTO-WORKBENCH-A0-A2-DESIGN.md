# Auto Workbench — A0/A1/A2 Engineering Gate

Date: 2026-09-09
Branch: `feature/auto-workbench`
Result: PASS — core implementation authorized

## A0 — Exact upstream archaeology
Primary feature source lineage:
- `f3ef6ffb3fd0656f56e63b14c0a17078677de1e7` — Blueprint automation energy/time, 2016-12-09.
- `0641eecbfdf2307941b3f48d3f6e11affb90a36d` — initial Auto Workbench, 2016-12-11.
- first containing release: `0.10-54`, 2016-12-26.
- `0.12-89` / `1e37e541ba9f3923b485c53af3a5025bf64d666a` — reproducible mature snapshot.

Frozen evidence archives:
- `auto-workbench-upstream-0.10-54.zip`
  SHA-256 `52C553462C529B8EE108A19066708039CAEE222C4F948F64688FCBDF42AFD691`.
- `auto-workbench-upstream-0.12-89.zip`
  SHA-256 `CD0954591D105E2FB37C784FC4E2348D7E966EBA1EF7801D1E422FAFA67D2FC6`.
- source delta: `auto-workbench-0.10-54-to-0.12-89.patch`.

Evidence root:
`C:\Users\User\Desktop\Codex\immersiveengineering-backport\evidence\auto-workbench`.
## A0 — Historical defects promoted to gates
Verified official history records:
- mirroring, GUI blueprint refresh and output-direction correction;
- JavaFX `Pair` removal after early Auto Workbench crash path (#1833);
- automation handler offset fix so insertion starts after Blueprint slot;
- Blueprint NBT desynchronization fix (#1988);
- missing-master/capability hardening;
- conveyor orientation restoration after disassembly;
- later mirrored-render and Blueprint-sync regressions.

The 0.10-54 renderer directly imports `javafx.util.Pair`; it is rejected as a copy source.
The 0.12 renderer depends on modern baked models, OBJ state and Forge model APIs; it is also rejected as a direct transplant.

Accepted renderer strategy: reconstruct on the local 1.7 `TileRenderIE` + `ModelIEObj` + `Matrix4` pipeline, using modern geometry/animation only as semantic input.

## A0 — Model/material finding
Static Auto Workbench geometry uses material families:
- `conveyor`;
- `workbench`;
- `assembler`.

Animated geometry uses groups `pressLift`, `blueprint`, `drill`, `press`, `lift`; Blueprint has its own material/texture.

Forge 1.7 `ModelIEObj` resolves texture by group name, not OBJ material. The conversion must therefore split material domains into 1.7 render groups while preserving vertices, UVs, normals and face order.
Garden Cloche established the UV rule for this renderer generation: preserve upstream V coordinates and let Forge 1.7 `WavefrontObject` perform its single V flip. Do not bake `1-v` during conversion.

## A1 — Registry collision audit
Audit sources:
- recovered source tree;
- reobfuscated `ImmersiveEngineering-garden-cloche-test-2.jar`;
- `javap -constants` on the production-form classes;
- full text/class-byte search for Auto Workbench identifiers.

Results:
- `BlockMetalMultiblocks`: metas 0–13 only.
- approved Auto Workbench multiblock meta: **14**.
- `BlockMetalDecoration2`: metas 0–2 only.
- approved Redstone Engineering Block meta: **3**.
- GUI IDs: 0–13 only.
- approved Auto Workbench GUI ID: **14**.
- exact JAR occurrences of `AutoWorkbench`, `GUIID_AutoWorkbench`, `ImmersiveEngineering:AutoWorkbench`: **0**.
- approved TileEntity ID by existing registration convention: **`ImmersiveEngineering:AutoWorkbench`**.

No existing registry/meta value is reassigned.
Existing worlds therefore retain all previous block metadata identities; the feature consumes previously unused metadata values only.

## A1 — Missing prerequisite discovered
Forbidden 0.7.9 has Light/Heavy Engineering blocks but no Redstone Engineering Block.
Official `0.8-24` defines `RS_ENGINEERING` and uses it in later multiblocks, including Auto Workbench.Official `0.8-24` recipe is verified as:
`IRI / RCR / IRI`, output 2,
where `I=ingotIron`, `C=ingotCopper`, `R=dustRedstone`.

The official texture is `metalDecoration0_redstoneEngineering.png`.

Legacy adaptation: add this block as `blockMetalDecoration2` meta 3. This is a registry-placement adaptation only; recipe and visual identity remain upstream-derived.

## A2 — Mature process contract
Verified mature defaults:
- structure 2×3×3;
- buffer 32,000 RF/IF-equivalent;
- 17 slots: Blueprint slot 0, component slots 1–16;
- queue max 3;
- process time 180 ticks;
- process energy 23,040 total = 128 RF/t;
- new queue entry allowed only when previous process progress is at least 0.4375;
- display transforms from consumed inputs to output at progress 0.78;
- up to 3 queued processes may tick in one server tick if energy allows;
- default redstone behavior: powered control position disables machine;
- hammer on control position toggles redstone inversion.

Recipe admission remains staggered every 16 ticks using the upstream coordinate-based scheduling pattern.

## A2 — Transaction model
The local Blueprint API remains ABI-compatible. Existing `consumeInputs(ItemStack[], int)` keeps its descriptor and behavior for existing callers.

A new additive automation consumption method will provide mature container-item semantics and return the exact consumed stacks for animation/persistence.Queue admission sequence:
1. resolve Blueprint category and selected recipe;
2. copy component inventory;
3. verify one full craft on the copy;
4. verify queue length and spacing without mutation;
5. compute exact consumed stacks and resulting inventory on the copy;
6. commit resulting inventory and append one process atomically;
7. persist/mark dirty and synchronize render state.

If any gate fails, component inventory and queue remain unchanged.

## A2 — Persisted process state
Each queued process persists independently:
- progress tick;
- max ticks;
- exact output ItemStack;
- exact consumed/display ItemStacks;
- transformation point;
- process data version.

This intentionally does not depend on recipe-list index surviving a restart. A process that was already admitted keeps the exact output contract captured at admission, preventing recipe-order or addon-registration changes from corrupting in-flight work.

## A2 — Output contract
At completion:
1. determine output direction from facing/mirroring;
2. attempt insertion into adjacent `IInventory`/`ISidedInventory` through the established local utility;
3. spawn only the actual remainder as an EntityItem;
4. remove process only after output ownership has transferred.

No output may be silently discarded.

## A2 — Legacy implementation boundary
Do not backport Forge Capabilities, `NonNullList`, baked-model state, or generic `TileEntityMultiblockMetal`.
Use the recovered 1.7 abstractions and Java 8 bytecode target only.