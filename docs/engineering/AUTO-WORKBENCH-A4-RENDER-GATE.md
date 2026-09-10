# Auto Workbench — A4 GUI/Render Gate

Date: 2026-09-10
Status: PASS FOR A5 ARTIFACT GATE
Branch: `feature/auto-workbench`
Runtime visual approval: NOT YET — requires Test 1

## Scope
A4 validates the client/server boundary, GUI, static and dynamic rendering architecture, converted OBJ assets, Engineer's Manual integration and historical rendering hardenings.

Passing A4 does not claim that the machine has been visually approved in a real Forbidden Series client. It authorizes construction of a Test 1 artifact.

## Reconciled candidate history
During continuation the branch was found to contain three coherent Auto Workbench commits beyond the previously frozen A3 checkpoint:
- `f2cd3dd3` — GUI and interaction layer;
- `b9aa06af` — animated renderer;
- `7a61866b` — Blueprint projection.

These commits were treated as untrusted candidates and audited against local Forge 1.7.10 source plus official IE history before acceptance.

## GUI contract
Verified/adapted behavior:
- GUI ID 14;
- 176×184 official layout;
- slot 0 accepts only an Engineer's Blueprint and has limit 1;
- slots 1–16 are component inventory;
- recipe buttons toggle selection or `-1` disabled state;
- recipe changes travel through existing `MessageTileSync`;
- server validates requested recipe index/category before accepting it;
- shift-click preserves the Blueprint/component boundary;
- sided automation at multiblock pos 9 exposes only slots 1–16.
## Forge 1.7 OBJ/parser evidence
Exact Forge 10.13.4.1614 `WavefrontObject` source was inspected.

Verified parser behavior:
- supports `v`, `vn`, `vt`, `f` and `g/o`;
- does not preserve `usemtl` as a render-selection primitive;
- flips texture V internally as `1-v`.

Therefore the converter intentionally:
- preserves source V coordinates to avoid a second flip;
- converts material boundaries into explicit 1.7 group names;
- never depends on 1.12 MTL dispatch at runtime.

All three committed converted OBJs were regenerated from official `0.12-89` source and matched byte-for-byte.

Static model groups:
- `conveyors_conveyor` → legacy Conveyor icon;
- `base_assembler` → legacy Assembler multiblock icon;
- `base_workbench` → Auto Workbench texture.

Animated model groups:
- `pressLift`, `drill`, `press`, `lift` → Auto Workbench texture;
- `blueprint` → Auto Workbench Blueprint texture.

The local `TextureStitchEvent.Post` path was inspected and proves that `ModelIEObj.getBlockIcon(groupName)` is actually used to rebind group UVs into the 1.7 atlas.
## Asset provenance
The principal client assets are exact official `0.12-89` bytes:
- `metal_multiblock_autoworkbench.png` — SHA-256 `1FC3BCC0D64AEC325749B1A77D4CAB367764B587C7D420DCF2554745AEE1A90C`;
- `metal_multiblock_autoworkbench_blueprint.png` — `2A4BC2B90935D6D10E1545E6705ED516BB9BD8BDA2D4D087C7226BC41A743121`;
- `auto_workbench.png` — `CDACD4D500DA31E86349E440A9089EA562AFAA93AF6C591DD87D2645AD6D24FB`.

The recipe-button atlas coordinates used by modern Auto Workbench did not match the legacy global `hudElements.png` bytes/region.

Replacing the global HUD would risk unrelated GUI regressions. Instead the exact official `0.12-89` `hud_elements.png` is isolated as:
`textures/gui/auto_workbench_buttons.png`
SHA-256 `A608EF5CFC5A1AB44C5A1A2465879405145EFE40A96EEA645C4E6565A430EA06`.
Independent `git show` extraction produced the same hash.

## Transform proof
`tools/audit_auto_workbench_transform.py` proves:
- NORTH 0°, SOUTH 180°, WEST +90°, EAST -90°;
- mirror transform equivalence for all four facings;
- drill offset and press axis;
- manual formed model centering;
- exploded-view Conveyor directions.

Latest result: `AUTO_WORKBENCH_TRANSFORM_GATE=PASS`.
After the manual translation the static model bounds are approximately X/Z `-1.5..1.5`, matching the three-column structure around the master.
## Engineer's Manual parity
An audit found the initial A4 candidate had accidentally disabled the formed-structure toggle and omitted special Conveyor orientation in the exploded view.

Official 0.10-54 and 0.12-89 both expose the formed view. This was restored without introducing client references into common code:
- `MultiblockAutoWorkbench` delegates render operations through the proxy;
- `CommonProxy` implementations are no-op;
- `ClientProxy` performs GL/TESR work;
- iterators 13–15 rotate Conveyor inventory render 180°;
- iterator 16 rotates it +90°;
- formed view renders the verified static Auto Workbench model.

The manual entry is registered in official `CAT_MACHINES` with a multiblock page and explanatory text page.

## Localization
The baseline had no Auto Workbench or Redstone Engineering Block language keys.

Added 1.7-native keys for both blocks and the official Auto Workbench manual text to `en_US.lang`.

Minecraft 1.7.10 `LanguageManager.onResourceManagerReload()` was inspected in the RFG-decompiled source: it always loads `en_US` first and the selected locale second. Therefore `en_US` supplies a verified fallback without duplicating English into all legacy language files.

## Dynamic item-render adaptation
Modern upstream uses its own item-model transform pipeline. Forbidden 1.7 uses `EntityItem` with `RenderItem.renderInFrame`.

Local `TileRenderMetalPress` and `TileRenderWorkbench` prove the legacy convention uses a negative X rotation for flat process items. The Auto Workbench therefore deliberately retains `-itemAngle`; this is an evidenced 1.7 adaptation, not an accidental sign difference.
## Blueprint projection hardening
Later official history was reviewed rather than freezing the first renderer implementation:
- 2019 `40668916...`: crash fixes for mixed texture resolutions — applicable and already reflected by scale-aware sampling;
- 2021 `cb5e5f5c...`: potential infinite loop — not applicable because it affected a Mod Workbench loop whose index only advanced for drawable recipes; this Auto Workbench renders one validated selected recipe;
- 2022 `3488c058...`: poor `java.awt.Point` hashing — partially applicable and fixed;
- 2025 `c4cdb8ed...`: invalid Blueprint render crash — guarded by selected-recipe/category/output validation and fail-soft resource loading.

The renderer no longer uses `java.awt.Point`; it uses immutable integer `BlueprintPoint`/`TexturePoint` keys with explicit equality and hashes. `tools/harden_auto_workbench_blueprint_points.py` is idempotent; latest result: `BLUEPRINT_POINT_GATE=ALREADY_HARDENED`.

## Teardown hardening
A post-A3 audit found the explicit Auto Workbench teardown would spawn the broken structure component even with `doTileDrops=false`.

The local `BlockMetalMultiblocks.breakBlock` already treats inventory/component drops under that gamerule. Auto Workbench now follows that contract locally while still restoring surviving structure parts. No behavior of older multiblocks was modified.

## Dedicated-side bytecode gate
After A4 hardening, `javap -verbose` reports class major 52 and zero `net/minecraft/client` references for:
- `TileEntityAutoWorkbench`;
- `MultiblockAutoWorkbench`;
- `CommonProxy`.

This preserves Java 8 and the dedicated-server common/client boundary.
## Build/static gates
Before A4 close:
- reconciled candidate clean build: PASS, 2m08s;
- manual-parity compile/checkstyle: PASS, 42s;
- BlueprintPoint hardening compile/checkstyle: PASS, 1m08s;
- final compile/checkstyle after `doTileDrops` hardening: PASS, 1m04s;
- `git diff --check`: PASS (only repository line-ending conversion warnings);
- transform gate: PASS;
- common bytecode gate: PASS.

## A4 decision
**PASS FOR A5 ARTIFACT GATE.**

What A4 proves:
- code compiles and passes checkstyle;
- client/common separation is preserved;
- source/asset provenance is traceable;
- OBJ conversion is deterministic;
- known applicable historical renderer failures have been addressed;
- Engineer's Manual and GUI surfaces are present.

What A4 does not prove:
- actual visual appearance under the Forbidden client/Angelica;
- runtime packet/GUI behavior in multiplayer;
- runtime machine formation, animation or persistence.

Those are Test 1 gates and remain explicitly open.
## Extreme-config numeric hardening
Official `0.12-89` computes Blueprint automation values directly as:
- `floor(23040*energyModifier)`;
- `floor(180*timeModifier)`.

The later config range allows a minimum modifier of `0.001`. At that boundary the upstream time expression can evaluate to 0, while the generic process constructor derives energy-per-tick by dividing by `maxTicks`.

The legacy adapter deliberately clamps configured process time and total energy to at least 1 after applying the verified minimum modifier. This prevents a zero-tick/zero-denominator process without changing default behavior.

Default behavior remains exact: 180 ticks, 23,040 RF total nominal recipe energy, 128 RF/t per active process.