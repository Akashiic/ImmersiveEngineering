# Modern Feature Gap Audit — Forbidden IE 0.7.9

Date: 2026-09-09
Status: PASS — Feature #2 selected
Branch: `audit/modern-feature-gap`
Baseline: `garden-cloche-test-2` / commit `ad5e6201`

## Objective
Identify which later Immersive Engineering features are genuinely absent, which already exist under legacy names/implementations, and which are worth backporting next without destabilizing the approved Forbidden 0.7.9 baseline.

This audit is a planning gate. It does not authorize bulk merging of later IE code.

## Evidence sources
1. Recovered Forbidden 0.7.9 source currently used by the project.
2. Official BluSunrize/ImmersiveEngineering Git history.
3. Official tagged snapshot `0.12-89`, commit `1e37e541ba9f3923b485c53af3a5025bf64d666a`.
4. Earlier official 0.10/0.11 commits when they are architecturally closer to 1.7.10.
5. Post-0.12-89 1.12 changelog/fix history where later fixes change correctness.
6. Selected post-1.12 official features only as strategic candidates, not as blind source targets.

## Important reference nuance
`0.12-89` remains the reproducible mature source snapshot used by this audit, but it was not the end of all 1.12.2 development. Later 0.12 builds and changelog entries exist. Per-feature bug archaeology must therefore continue past tag 89 when relevant.
## Classification model
- `ALREADY PRESENT`: behavior exists sufficiently in Forbidden 0.7.9.
- `PRESENT BUT LEGACY`: same gameplay family exists, but later IE has meaningful improvements.
- `MISSING`: genuine later feature with no equivalent in the baseline.
- `PREMIUM BACKPORT`: missing feature with strong value/identity and acceptable isolation.
- `HIGH RISK`: valuable but touches a central subsystem or broad multiplayer state.
- `DEFER`: poor next-step value or architectural distance too high.

## Baseline already stronger than a naive version diff suggests
Forbidden 0.7.9 already contains Arc Furnace, Assembler, Bottling Machine, Metal Press, Crusher, Excavator, Diesel Generator, Refinery, Fermenter, Squeezer, Tesla Coil, Sample Drill, Charging Station, Floodlight, fluid pipes/pump, railgun, chemthrower, revolver, drill, skyhook, blueprints and the established wire/energy network.

Garden Cloche/Belljar is now also present through the approved backport.

This removes many superficially attractive items from the missing-feature list.

## Semantic false positives rejected
- `TileEntityBelljar` in 1.12 is not missing: it maps to our approved `TileEntityGardenCloche` implementation.
- `TileEntityLightningrod` vs local `TileEntityLightningRod` is primarily naming/evolution, not proof of a missing Lightning Rod.
- 1.12 `TileEntitySorter` is not a wholly missing Item Router. Local `TileEntityConveyorSorter` already implements the ancestral six-sided filter/router semantics with OreDictionary, NBT and fuzzy matching.
- Modern `TileEntityConveyorVertical` does not prove vertical transport is absent; the local conveyor has legacy up/down behavior.
- Generic later classes such as `TileEntityMultiblockMetal` and datafixer/wrapper classes are architecture changes, not player features.
## Verified 1.12-era feature gaps
### Missing multiblocks
- Automated Engineer's Workbench / Auto Workbench.
- Alloy Kiln (`Alloy Smelter` tile in source naming).
- Mixer.

### Missing or materially newer machine/device families
- Fluid Placer / Floodgate.
- Chemthrower Turret and Gun Turret.
- Redstone Wire Connector family, later Probe Connector and Feedthrough behavior.
- Razor Wire, including electrification semantics.
- Fluid Sorter.
- Turntable.
- Core Sample item/display workflow for the already-present Sample Drill.

### Missing player equipment / utility
- Capacitor Backpack / Powerpack.
- Ear Defenders/Earmuffs.
- Heavy Plated Shield.
- Faraday armor family.
- Speedloader as the later standalone workflow.
- Maintenance Kit.
- Fluorescent Tube.

### Lower-priority construction/decor gaps
Strip Curtains, Shader Banners, Cushion, reinforced/gunpowder crate variants, placeable toolbox behavior, later concrete variants, insulating glass, alloy brick and additional metal decorative variants.
## Prioritization model
Every candidate is rated 1–5 on:
- Player value — weight 25%.
- IE identity / visual-gameplay impact — 15%.
- Reuse of the current 0.7.9 foundation — 20%.
- Historical maturity — 15%.
- Implementation complexity — inverse, 15%.
- Regression blast radius — inverse, 10%.

Formula:
`Score = Value*5 + Identity*3 + Reuse*4 + Maturity*3 + (6-Complexity)*3 + (6-Risk)*2`

The score is an engineering priority index, not an objective measure of fun. Ties are resolved by scope containment and strategic sequence.

## Ranked candidates
| Rank | Candidate | Score | Classification |
|---:|---|---:|---|
| 1 | Automated Engineer's Workbench | **92** | PREMIUM BACKPORT — SELECTED |
| 2 | Alloy Kiln | **83** | PREMIUM BACKPORT |
| 3 | Mixer | **83** | PREMIUM BACKPORT, larger dependency surface |
| 4 | Core Sample item/display modernization | **82** | MODERNIZATION CANDIDATE |
| 5 | Turntable | **79** | CONTAINED BACKPORT |
| 6 | Fluid Placer | **79** | CONTAINED BACKPORT |
| 7 | Razor Wire | **78** | CONTAINED BACKPORT |
| 8 | Turret family | **76** | HIGH VALUE / HIGH RISK |
| 9 | Fluid Sorter | **75** | BACKPORT / router-family extension || 10 | Powerpack / Capacitor Backpack | **74** | PLAYER-EQUIPMENT BACKPORT |
| 11 | Conveyor family modernization | **72** | HIGH VALUE / HIGH RISK |
| 12 | Redstone wire/probe/feedthrough family | **72** | HIGH VALUE / HIGH RISK |
| 13 | Buzzsaw (post-1.12) | **72** | FUTURE PREMIUM CANDIDATE |
| 14 | Sawmill (post-1.12) | **70** | FUTURE PREMIUM CANDIDATE |
| 15 | Radio Tower (much newer) | **45** | DEFER |

## Why Auto Workbench wins
The Auto Workbench is the best second proof of the backport framework because it advances us from a single-block machine backport to a visually complex automated multiblock while reusing systems already proven in 0.7.9.

Current baseline reuse is unusually high:
- `BlueprintCraftingRecipe` already exists and already handles blueprint categories, OreDictionary inputs, `getMaxCrafted` and ingredient consumption.
- Engineers Blueprint item/workbench gameplay already exists.
- Multiblock formation/disassembly patterns already exist.
- RF storage/input patterns already exist.
- Conveyor blocks and item insertion/output already exist.
- GUI packet sync patterns already exist.
- Metal Press and Assembler provide close 1.7.10 examples for processing, inventory, energy and output behavior.

By contrast, Mixer requires a new recipe/tank processing family; Turrets require targeting/ownership/persistence/security; redstone wires and conveyors touch central network/transport infrastructure.

## Auto Workbench provenance
- `f3ef6ffb3fd0656f56e63b14c0a17078677de1e7` — 2016-12-09: energy/time variables added to Blueprint recipes for automation.
- `0641eecbfdf2307941b3f48d3f6e11affb90a36d` — 2016-12-11: initial Automated Workbench implementation.
- First release containing it: `0.10-54` (2016-12-26).
- `3df3d764d709eede70fd1f3f76affa8fd31fcfe0` — mirroring, GUI blueprint updates, corrected output directions.- `5d1d10a09` — 2017-01-09: removed JavaFX Pair usage, closing the early recipe-selection crash path (#1833).
- `d6f4f386d43af2c070d2a675eaceeb14bffc20c6` — 2017-03-28: fixed input handler offset and Blueprint NBT desync (#1988).
- `0a82b4175` — 2017-08-02: hardened capability access when multiblock masters are missing.
- `37c7e913a` — 2017-10-14: fixed conveyor orientation after disassembly.
- Later official history also contains blueprint-sync and mirrored-rendering fixes; those are regression evidence even when their modern implementation is not directly portable.

## Mature 1.12 semantics to preserve
From `0.12-89`:
- 2×3×3 formed structure.
- 32,000 IF internal energy buffer.
- 17 inventory slots: 1 Blueprint + 16 component/input slots.
- Recipe selection can be disabled (`selectedRecipe = -1`).
- Up to 3 queued processes.
- Automatic item insertion targets the 16 component slots, never the blueprint slot.
- Crafted output is sent to the machine side determined by facing/mirroring, then dropped only if inventory insertion leaves a remainder.
- Blueprint recipe automation defaults: 23,040 IF total energy and 180 ticks per craft.
- Redstone can disable processing.
- Structure may be mirrored.

## Auto Workbench backport rule
Do not transplant `TileEntityMultiblockMetal`, Forge Capabilities, `NonNullList`, modern blockstates, or 1.12 rendering APIs into 1.7.10.

Reconstruct the machine on top of the recovered 1.7.10 abstractions:
- `TileEntityMultiblockPart`-style master/dummy contract;
- `IInventory` / `ISidedInventory` for automation;
- CoFH RF `EnergyStorage` / `IEnergyReceiver`;
- existing 1.7.10 packet/NBT conventions;
- existing conveyor tile semantics;
- client-only renderer classes and Java 8 bytecode.
## Why Alloy Kiln is #2
The Alloy Kiln is architecturally compact and historically mature. It was introduced in `f5fdf38058a203c840d5a8d412cc984ff5229184` on 2017-05-20 as a 2×2×2 alloying multiblock.

Later fixes define its edge cases: multiblock breaking, ghostloading avoidance, fuel countdown semantics, recipe lookup only with both inputs, and preservation of fuel container items. It is a strong low-risk follow-up but has less transformational player impact than the Auto Workbench.

## Why Mixer is #3
The Mixer was introduced in `1a9e6b54e5c7eb5d66e6f8409f14061cc56ccefd` on 2017-02-21. It is visually and mechanically premium, but adds a dedicated recipe type, fluid mixing/tank state, GUI fluid visualization, output-mode semantics and larger CraftTweaker/compat concerns.

A 2018 fix also documents a server crash when the Mixer runs faster than usual or processes large fluid quantities. That makes throughput and recipe bounds explicit test gates.

## High-risk families intentionally deferred
### Turrets
High player value, but official history contains ownership, UUID persistence, ownerless tameable crashes, block-breaking permissions, target reset and line-of-fire/non-target collision fixes. This is multiplayer/security-sensitive state, not the right second backport.

### Redstone wire / Probe / Feedthrough
Very useful, but it is coupled to the wire network. Official history includes weak-signal bugs, update bugs, lag fixes, feedthrough formation/breaking issues and wire-system changes. A failure can regress all existing IE wiring.

### Conveyor modernization
Splitting, covered, uncontrolled, extracting and covered variants are valuable, but the later system is registry-driven and replaces core conveyor assumptions. Official history contains rotation, insertion, magnet-protection, cooldown and entity-behavior fixes. Treat this as a dedicated subsystem modernization, not a small feature patch.
## Post-1.12 strategic candidates
Later official IE history contains attractive features that are deliberately outside the first comparison baseline.

### Buzzsaw
Added in 2020. Later history includes attack/fuel behavior fixes, crashes, fluid sync, model/normal fixes and blade handling. Good future premium item, but farther from 1.7.10 than the selected 1.10/1.12-era targets.

### Sawmill
Added in 2020 as a modern multiblock. Later fixes cover secondary outputs, blade damage/drop, item synchronization, comparator output and recipe expansion. Visually excellent, but the modern multiblock architecture is much farther from Forbidden 0.7.9.

### Radio Tower
Introduced in 2024 and rapidly iterated through GUI, frequencies, energy/control and a structure/model overhaul. Architectural distance and young semantics make it a deferred target.

These features remain candidates after the 1.12-era gap is selectively harvested; newer does not automatically mean higher priority.

## Separate expansion track: Immersive Petroleum
Immersive Petroleum is not part of core IE and is therefore excluded from this score. Its Pumpjack/Distillation ecosystem should be audited as an addon/fork integration project, not silently mixed into the core feature-gap list.

It remains a strategically attractive future branch because it expands the industrial progression rather than merely modernizing an existing machine.

## Decision
**APPROVE Feature #2: Automated Engineer's Workbench.**

Recommended sequence after it:
1. Auto Workbench.
2. Alloy Kiln or Mixer depending on whether the next goal is low-risk breadth or premium fluid processing.
3. Contained utilities (Core Sample workflow, Turntable, Fluid Placer, Razor Wire).
4. Turrets only after an explicit multiplayer/security design review.
5. Conveyor and redstone-wire modernization only as isolated subsystem projects.
6. Post-1.12 Sawmill/Buzzsaw and the Immersive Petroleum track after the core backport framework has multiple successful multiblocks.