# Mixer — A0/A1/A2 Archaeology and Backport Architecture

Date: 2026-09-12
Target semantic line: official Immersive Engineering `1.21.1`
Implementation bridge: official/community-preserved IE 1.12.2 source snapshot
Legacy runtime target: Forbidden Series / Forge 1.7.10
Status: A0/A1 evidence captured; A2 architecture approved for implementation branch

## Mission
Backport the modern Mixer as a complete machine family, not a decorative shell.

The target includes:
- modern 3x3x3 multiblock structure and mirrored formation;
- 8 item input slots;
- 8,000 mB multi-fluid tank;
- 16,000 RF energy buffer;
- queue/process semantics;
- modern fluid recipes and sized item requirements;
- fluid input/output ports;
- redstone control;
- `outputAll` mode;
- GUI, NEI and scripting integration;
- modern rigid geometry, rotating agitator and fluid surface rendering;
- active Mixer sound and fluid particles under bounded client cost;
- persistence, teardown and performance gates.

The legacy `metalMultiblock` metadata family will not be extended for this feature.

## Provenance
The frozen modern source is commit:
`75a27f03e4243544243567e8d5c38d336f4f10f4`.

The modern line contains dedicated Mixer logic, recipe, GUI, renderer, sound, manual, model assets, multiblock template and computer integration.

The 1.12.2 bridge contains a complete earlier implementation:
- `MixerRecipe`;
- `TileEntityMixer`;
- `MultiblockMixer`;
- GUI and Container;
- JEI and CraftTweaker integration;
- potion mixing;
- OpenComputers driver;
- static/mirrored OBJ plus separate agitator model.

The current Forbidden/R2 source contains no Mixer implementation. The feature is therefore a true addition, not a modernization of an existing registry identity.

## Modern recipe surface
Current generated static Mixer recipes are:
- Concrete: 500 mB water + 2 sand + 1 gravel + 1 clay -> 500 mB concrete, 3,200 total energy;
- Herbicide: 500 mB ethanol + sulfur dust + copper dust -> 500 mB herbicide, 3,200 total energy;
- Redstone Acid: 250 mB water + redstone dust -> 250 mB redstone acid, 1,600 total energy.

Modern potion mixing remains a dynamic/special recipe family and is a separate acceptance item rather than silently omitted.

## Visual architecture
The modern static Mixer OBJ has:
- 183 vertices;
- 153 UV coordinates;
- 118 faces;
- 118 quads;
- zero triangles.

SHA-256:
`24FE37E551E2D641CCE049EDFBACC132EFD47E5BC6C27DC28DC20743F2F20E8E`.

The separate agitator OBJ has:
- 56 vertices;
- 11 UV coordinates;
- 29 faces;
- 29 quads;
- zero triangles.

SHA-256:
`85EB5611FFA7DDB8A113BC027B3099A2BAC300FB84B446864B4F283CD2EE8D4F`.

Decision:
- static body: audited OBJ/static multiblock renderer;
- agitator: procedural rigid Y rotation matching the modern 9 degrees/tick active animation;
- fluids: dynamic layered surface renderer only;
- GeckoLib: **REJECTED for this machine** because no skeletal animation is required.

## Structure and registry plan
The established 1.12 structure bridge requires:
- 5 Steel Scaffolding;
- 3 Fluid Pipes;
- 1 Redstone Engineering Block;
- 4 Light Engineering Blocks;
- 4 Iron Sheetmetal blocks;
- 1 Steel Fence.

These component families already exist in the cumulative 1.7.10 line, including the Redstone Engineering Block introduced for Auto Workbench.

The formed Mixer will use a new scalable multiblock registry family, provisionally `metalMultiblock2`, meta 0.

Reasons:
- legacy `metalMultiblock` already occupies metas 0-14;
- meta 15 is a strategic reserve and is not consumed opportunistically;
- future modern machines require more than one additional multiblock slot;
- new registry identity avoids remapping existing worlds.

Formation must support all four horizontal facings and the upstream mirrored structure.
Full structure validation must finish before any world mutation; conversion must be transactional with rollback.

## Fluid foundation
The current 1.7.10 branch has Forge `IFluidHandler`/`FluidTank` support but no `MultiFluidTank` abstraction.

Mixer requires an ordered multi-fluid tank because input fluid, partially processed fluid and produced fluid may coexist during queued processing.

A small IE-owned `MultiFluidTank` will therefore be backported before the machine itself. Requirements:
- total capacity 8,000 mB;
- ordered list of non-empty FluidStacks;
- no duplicate adjacent/equivalent entries after normalization;
- fill/drain simulation must never mutate state;
- per-fluid drain must preserve order;
- bounded fluid-type count derived from non-empty stacks, not stale NBT entries;
- NBT parser validates amount >0 and known Fluid;
- all arithmetic uses checked/clamped positive integers;
- no chunk/world access inside tank operations.

This abstraction is considered reusable platform infrastructure for later modern fluid machines.

## Processing semantics
Modern constants:
- energy buffer: 16,000 RF;
- item slots: 8;
- tank: 8,000 mB;
- process queue capacity: 8.

Each Mixer recipe consumes a sized fluid ingredient plus zero or more sized item ingredients and produces fluid.

Item matching must consume requested quantities across the available component stacks without double-counting a slot. Queued processes reserve their chosen input slots so a second process cannot consume the same items.

The process record must persist:
- recipe identity or a reconstructible recipe signature;
- process tick/max ticks;
- total/remaining energy accounting;
- input slot indices;
- input tank association.

Recipe mutation through scripting invalidates caches and must not deserialize an old process into a different recipe silently.

All inventory/fluid/energy commits occur server-side and are transactional at process boundaries.

## Historical defects that become regression gates
The 1.12 history contains a dedicated Mixer crash fix (`a93f4c77b`, issue #3131) for accelerated processing / large fluid amounts.

That class of failure is explicitly in scope. Tests must cover process speeds and recipe quantities that cross more than one unit/tick; no array/list/process calculation may assume a single mB/t or one process step per server tick.

Other inherited history includes:
- output-all mode addition (`30173e58a`);
- CraftTweaker integration (`64275c03c`);
- potion-fluid/Mixer recipe work (`767fa2cf2`);
- capability/master validity fixes (`0a82b4175`);
- ghost-loading-related random/world access fixes (`ee93f35d0`).

These commits are treated as bug-history evidence, not optional polish.

## Fluid output throughput
The late 1.12 implementation outputs at most 80 mB/t.
Current 1.21.1 logic allows up to one bucket (1,000 mB) per tick total.

Target semantic behavior is the modern 1,000 mB/t ceiling, implemented with a simulation-first fill and exact executed drain. Throughput must be configurable only if a later compatibility need is proven; the default will not silently retain the 1.12 bottleneck.

## New fluid surface
The modern static recipes require three fluid outputs not present in the current 1.7.10 branch:
- Concrete;
- Herbicide;
- Redstone Acid.

They will receive stable 1.7.10 FluidRegistry identities matching modern names where collision-free.

Concrete is not a cosmetic fluid. Modern `ConcreteFluid`:
- ticks every 20 ticks;
- carries a drying/decay state;
- eventually hardens into concrete or a concrete slab according to fluid level;
- avoids drying a source while adjacent concrete fluid is still feeding flow;
- applies a concrete-feet effect to entities caught as it solidifies.

A Mixer that only creates a colored generic fluid would therefore be incomplete. Concrete lifecycle is a dependent subfeature and must be designed/ported before Mixer is called complete.

Herbicide and Redstone Acid require their modern block/entity/chemthrower semantics to be audited before approval; registration alone is insufficient.

## GUI and network ownership
The 1.12 Mixer toggled `outputAll` through the generic `MessageTileSync` C2S path. That path is already rejected for expansion by the legacy-network audit.

Mixer will not add a free-form NBT packet for this boolean.

Preferred implementation order:
1. verify Forge/Minecraft 1.7.10 Container button action (`Container.enchantItem` plus the vanilla window action packet) can represent the toggle;
2. if proven, use it so the server's current open Container is the authorization boundary and mutation occurs on the game thread;
3. only if that API cannot express the action, use the separately hardened C2S queue with exact Mixer container/master validation.

The client never supplies world coordinates as authority for `outputAll`.

The GUI receives energy, fluids, process state and output mode through bounded container synchronization / description state; no per-tick full NBT payload is acceptable.

## Sound and particles
Modern Mixer sound is an active loop at nominal volume `0.075` and is tied to actual processing state.

The sound must use the bounded sound-scene architecture, not one legacy nearby-TileEntity muffler scan per Mixer instance.

Client effects:
- agitator advances 9 degrees/tick only while active;
- bubble/fluid-splash emission is probabilistic and bounded;
- particle fluid/color is derived from the visible tank layer;
- particles stop immediately when the machine is inactive/unloaded.

No sound or particle packet is sent every tick; process/activity state is sufficient for client reconstruction.

## Performance budgets
Primary scale gates:
- 64 idle Mixers: no recipe scan or neighbor lookup when tank/energy/input state makes work impossible;
- 64 active Mixers: bounded queue work, no per-process allocation storm;
- 256 idle Mixers: near-flat cost outside TileEntity dispatch;
- 256 active Mixers: profiling required before approval.

Recipe lookup must gain positive/negative caching keyed to fluid + item-state + recipe revision rather than linear-scanning unchanged inputs every tick.
Fluid output must use existing-neighbor access only and must never force-load chunks.

## Implementation gates
### M0 — provenance / surface
PASS when modern source/assets/recipes, 1.12 bridge and historical fixes are frozen with hashes/commit IDs.

### M1 — reusable fluid/recipe foundation
Implement and unit/static-audit `MultiFluidTank`, `MixerRecipe`, recipe-revision cache and fluid registrations. No multiblock yet.

### M2 — multiblock / registry
Create `metalMultiblock2` family, transactional 3x3x3 formation, mirror support, exact material restoration and no legacy metadata remap.

### M3 — process / IO
Add 16k RF, 8k multi-fluid storage, 8 slots, queue/reservation, redstone, safe 1000 mB/t output and persistence. Explicitly regression-test #3131-class accelerated/large-fluid cases.

### M4 — GUI / network / integration
Server-authoritative output mode; GUI tank layers; NEI and MineTweaker; potion recipes; no generic unsafe TileSync expansion.

### M5 — modern visual/audio
Static body, procedural agitator, layered fluids, particles and bounded active loop sound; Angelica and culling tests.

### M6 — adversarial/runtime
Real Forbidden pack, restarts, chunk unloads, blocked outputs, multiple fluid types, queue saturation, script reload, 64/256-machine profiling and dedicated-server validation.

No gate passes solely because the feature compiles.
