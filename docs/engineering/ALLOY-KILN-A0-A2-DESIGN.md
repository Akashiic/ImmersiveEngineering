# Alloy Kiln — A0/A1/A2 Engineering Design

Date: 2026-09-11
Status: DESIGN APPROVED; IMPLEMENTATION NOT YET PROMOTED
Target: IE 1.21.1 semantics/visuals on Forbidden IE 0.7.9 / Forge 1.7.10
Feature sequence: candidate #3 after Garden Cloche + Auto Workbench

## Mission
Backport the modern Alloy Kiln as a native 1.7.10 machine while preserving existing worlds, registries, Java 8 bytecode, server safety and the modern 1.21.1 visual identity.

The feature must not be a literal transplant of NeoForge code. The 1.21.1 branch defines current behavior and assets; the official 1.12.2 lineage is the implementation bridge because its TileEntity/GUI/multiblock architecture is much closer to Forge 1.7.10.

## Evidence hierarchy
1. Current Forbidden R2 baseline and runtime contracts.
2. Official IE 1.21.1 branch at `75a27f03e4243544243567e8d5c38d336f4f10f4`.
3. Official 1.12.2 Alloy Smelter history, including all correctness fixes after introduction.
4. Existing 1.7.10 Blast Furnace/Coke Oven patterns where they are still semantically valid.
5. Inference only after the above cannot answer the question.

## A0 — verified modern surface
- structure: 2x2x2;
- material: exactly 8 Alloy/Kiln Bricks;
- inventory: 2 inputs + 1 fuel + 1 output;
- default recipe duration: 200 ticks;
- input order is reversible;
- vanilla furnace fuels are accepted;
- machine has active/off visual states;
- GUI layout remains the historical four-slot furnace layout.
## Modern recipe surface
The audited 1.21.1 datapack contains eight Alloy Smelter recipes:
- Constantan: copper + nickel -> 2;
- Electrum: gold + silver -> 2;
- Brass: copper + zinc -> 2;
- Bronze: 3 copper + tin -> 4;
- Invar: 2 iron + nickel -> 3;
- Rose Gold: 3 copper + gold -> 4;
- Manyullyn: 3 cobalt + netherite scrap -> 4;
- Insulating Glass: 2 glass blocks + iron dust -> 2.

Recipes without an explicit `time` use the 1.21.1 serializer default of 200 ticks.

### 1.7.10 recipe policy
Constantan and Electrum are first-class baseline recipes because Forbidden IE already owns the required metals and outputs.

Brass, Bronze, Invar and Rose Gold are conditional OreDictionary recipes: they register only when all required input/output ore names resolve to real stacks. The IE backport must not fabricate another mod's metal solely to satisfy parity.

Manyullyn is **DEFERRED** until an exact Forbidden-Series material contract is audited. Minecraft 1.7.10 has no Netherite Scrap, so substituting an unrelated ingredient without evidence would be semantic invention.

Insulating Glass is approved in principle but belongs to the wider stone/wiring material modernization. The Alloy Kiln core must expose recipe support without forcing that block into the same implementation commit if its registry design is not yet approved.

## 1.12 bridge provenance
Official introduction: `f5fdf38058a203c840d5a8d412cc984ff5229184` — cheap 2x2x2 Alloy Kiln.
Correctness history that becomes mandatory regression evidence:
- `528c375b2`: multiblock break/disassembly correctness;
- `0582a0d7b`: avoid ghost-loading through TileEntity lookups;
- `0a82b4175`: missing-master capability safety;
- `5e078d5be`: burn timer/active-state semantics;
- `133df6d50`: eliminate needless ticking where the platform permits it;
- `5ae96e0c5`: processing-state correctness fix shared with Blast Furnace;
- `a17fe489f`: require both inputs before recipe lookup and preserve fuel container items.

### Explicit bridge rejection
The 1.12 `isStackValid()` implementation is not authoritative. Git blame proves the expression `slot==0 || slot==1 && isFuel` came from the original feature commit and survived unchanged, while the actual Container uses slots 0/1 as ingredients and slot 2 as fuel.

The current 1.21.1 implementation instead defines slot constraints explicitly and does not expose the inventory externally as a general item capability. Our 1.7.10 implementation therefore specifies:
- slot 0: alloy ingredient;
- slot 1: alloy ingredient;
- slot 2: vanilla furnace fuel;
- slot 3: output only;
- no external sided inventory automation in the first approved build.

## A1 — registry architecture
Do not consume free metadata in the legacy `stoneDecoration`/`stoneDevice` blocks merely because it exists.

Those blocks are compatibility islands. The full 1.21.1 program will need additional stone decorations and devices, so Alloy Kiln begins a scalable family rather than extending old identities opportunistically.

Provisional names for implementation planning:
- `stoneDecoration2`, meta 0 = Alloy/Kiln Brick;
- `stoneDevice2`, meta 0 = formed Alloy Smelter/Kiln.
These names are not production assignments until source/runtime collision audit passes. Existing block registry names/metas must remain byte-for-byte unchanged.

GUI ID 15 is the provisional Alloy Kiln GUI ID. Existing IDs 0-14 are occupied and frozen. The assignment becomes final only with source search, artifact audit and old-world load test.

TileEntity registration follows the existing `IEContent.registerTile` convention. Proposed class `TileEntityAlloySmelter` would therefore register as `ImmersiveEngineering:AlloySmelter`; collision absence must be proven before promotion.

## Structure contract
Formation uses eight identical Alloy Bricks in a 2x2x2 cube. No mirroring state is required because the geometry is rotationally symmetric; only horizontal facing determines the front texture/GUI orientation.

The formation algorithm must:
1. normalize hammer trigger to one lower corner without loading chunks;
2. validate all eight positions before mutation;
3. snapshot original block stacks;
4. replace only after full validation succeeds;
5. establish one master and seven dummies deterministically;
6. persist offsets/facing using the existing multiblock-part contract;
7. never leave a half-formed cube after an early failure.

Disassembly must restore exactly eight Alloy Bricks, preserve unrelated replacement blocks, respect `doTileDrops`, and never duplicate the master's inventory.

## A2 — processing state machine
The implementation is legacy-native, not a backport of `IMultiblockLogic` or NeoForge item handlers.

Persist on the master:
- 4-slot inventory;
- `process`;
- `processMax`;
- `burnTime`;
- `lastBurnTime`;
- active state;
- facing/formation data through the shared multiblock base.
### Tick invariants
- only the master performs recipe/fuel work;
- dummies must return immediately;
- process advancement is server-only;
- burn time decrements according to the mature furnace semantics even when a valid process is not advancing;
- starting a process never consumes ingredients;
- inputs are consumed exactly once at successful completion;
- output capacity is validated before starting/continuing a recipe;
- an output blockage pauses admission without deleting ingredients;
- if the recipe identity/required count becomes invalid mid-process, progress is cancelled rather than producing a stale result.

Fuel handling uses the vanilla 1.7.10 furnace burn-time contract. When a single consumed fuel item has a container item, that remainder must replace the consumed stack exactly once.

Recipe matching is symmetric: input0/input1 and input1/input0 are equivalent. Required stack counts must be validated before a process is admitted and again before commit.

## Rendering architecture
The modern model is `cube_two.obj`: 8 vertices, 6 quad faces, no articulated geometry. The five current 1.21.1 textures are 32x32: bottom, front-off, front-on, side and top.

A TESR is **REJECTED** for this machine. GeckoLib is **REJECTED** for this machine. Both add per-frame machinery for a static cube.

Preferred 1.7.10 path:
1. deterministically split each 32x32 face texture into four 16x16 quadrants at build/source-preparation time;
2. register the resulting icons normally;
3. use a lightweight ISBRH/`RenderBlocks` path;
4. select the proper quadrant from TileEntity local offset + facing + active state;
5. keep the formed blocks fully opaque and cache-friendly.

The converter must preserve source hashes and emit a manifest mapping every generated quadrant back to its 1.21.1 asset.
## GUI and synchronization
Use the modern/historical four-slot layout: inputs at 38/17 and 66/17, fuel at 52/53, output at 120/35. The modern `alloy_smelter.png` GUI is the visual source of truth after dimension/atlas compatibility audit.

Progress synchronization should reuse vanilla Container progress-bar integers rather than introducing new custom C2S messages. The server owns inventory, process and burn state; the client only receives display values.

The GUI must never allow the client to choose a recipe or mutate machine state through generic NBT messages. This feature therefore benefits from the separate network-hardening gate without needing a new mutable C2S protocol.

## Manual / NEI / scripting
Engineer’s Manual receives a native 1.7.10 entry describing the 2x2x2 structure and current recipes.

NEI support is required before runtime approval. Recipe display must show both input slots, fuel context and output without registering duplicate permutations for reversed inputs.

MineTweaker support is in scope before final feature approval because the current 1.7.10 baseline already exposes scripting for other industrial recipes. The intended surface is add/remove AlloyRecipe, implemented through the existing `MTHelper`/undoable-action conventions.

## Performance budget
The machine is expected to be cheaper than current rendered multiblocks. Required benchmarks:
- 1, 16, 64 and 256 formed Kilns idle;
- the same counts active where practical;
- server tick sampling with no GUI open;
- client FPS/frame-time with the structures visible and culled;
- Angelica + EntityCulling enabled in the real pack.

No per-tick allocation of recipe lists, temporary inventories or render objects is accepted. Recipe lookup should short-circuit on empty inputs and may cache the last matching recipe only if invalidation is proven correct.

No world/chunk lookup outside the eight structure positions is allowed during normal ticks.

## Security / networking gate
No Alloy Kiln packet may trust arbitrary dimension, block position, slot or recipe index from a client. GUI interaction should remain server/container-authoritative.
## Adversarial runtime matrix
A first candidate cannot be approved without all applicable cases:
- formation from every horizontal side;
- hammering upper/lower and both lateral candidate bricks;
- invalid 7/8-brick structure does nothing;
- GUI from every formed part resolves the master;
- Constantan and Electrum in both input orders;
- insufficient stack counts never begin/complete a recipe;
- blocked/full/wrong output stack pauses safely;
- remove one input at 25%, 50% and 99% progress;
- change one input to another valid alloy mid-process;
- fuel exhaustion and refill during a process;
- bucket/container-item fuel preserves the remainder where vanilla permits it;
- save/reload at multiple progress and burn values;
- break master and each dummy position;
- `doTileDrops=false` behavior;
- old Forbidden world load with zero remaps;
- dedicated server without client classes;
- NEI open/recipe lookup;
- MineTweaker add/remove/reload;
- Angelica/EntityCulling visual smoke test;
- repeated operation for item duplication/loss.

## Gates
K0 — archaeology/source hashes: PASS.
K1 — registry/ID architecture: DESIGN PASS, runtime allocation pending.
K2 — processing/NBT/render/network design: PASS.
K3 — isolated implementation branch.
K4 — compile + clean build + reobf + ABI/artifact audit.
K5 — isolated client/dedicated-server smoke.
K6 — Forbidden runtime Test 1.
K7 — adversarial fixes + Test 2.
K8 — final tag/hash/documentation freeze.

## A0/A1/A2 decision
**APPROVE implementation planning.**

This approval does not authorize merging the feature into the frozen Auto Workbench R2 candidate. Implementation must occur in its own worktree/branch and may only be promoted after the Auto Workbench A6 lineage and the required shared hardening decisions are reconciled.
