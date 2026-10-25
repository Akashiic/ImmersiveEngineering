# Alloy Kiln — Runtime Test 1

Candidate status: K4 passed; K5 gameplay homologation required.

## Installation
1. Back up the test world.
2. Remove the previous Immersive Engineering test JAR from `mods`.
3. Install only the Alloy Kiln Test 1 JAR.
4. Never run two `ImmersiveEngineering` JARs simultaneously.
5. Keep Angelica and the normal Forbidden Series runtime enabled for the primary pass.

## T0 — Boot and identity
- client reaches main menu;
- existing world loads;
- no MissingMappings/remap prompt for old IE content;
- no new classloading/resource/model exception;
- confirm only one IE JAR is loaded.

## T1 — Kiln Brick
- craft 2 Kiln Bricks using sandstone + bricks in the diagonal 2x2 recipe;
- verify item name `Kiln Brick`;
- verify modern texture;
- verify ordinary placement/break/drop before formation.

## T2 — Formation geometry
Build an exact 2x2x2 cube of 8 Kiln Bricks.

For each NORTH/SOUTH/EAST/WEST orientation:
- form with Engineer's Hammer;
- test hammering a lower brick;
- test hammering the corresponding upper brick;
- test both lateral columns;
- verify all 8 blocks form exactly once;
- verify the front face follows the expected player orientation.

## T3 — Failed formation is transactional
Repeat with one brick missing or replaced.

Expected:
- formation returns false/no machine forms;
- all original Kiln Bricks remain;
- no formed-device block is left behind;
- no item duplicate/drop appears.

Also obstruct/alter a part during formation only if a safe controlled repro is available; preserve logs if any rollback path is exercised.

## T4 — GUI from every part
Right-click every one of the 8 formed blocks while not sneaking.

Expected:
- the same master GUI opens from every part;
- two input slots, fuel slot and output slot are present;
- shift-click does not place arbitrary items into output;
- normal furnace fuel routes to the fuel slot;
- recognized alloy ingredients route only to input slots.

## T5 — Native alloys
Test both input orders:
- Copper + Nickel -> 2 Constantan;
- Gold + Silver -> 2 Electrum.

Expected:
- duration is 200 ticks;
- fuel is consumed according to vanilla furnace burn time;
- ingredients are consumed only when the craft completes;
- reverse input order produces the identical output.

## T6 — Sized OreDictionary alloys
If the corresponding ores exist in the pack, test:
- 3 Copper + 1 Tin -> 4 Bronze;
- 2 Iron + 1 Nickel -> 3 Invar;
- 1 Copper + 1 Zinc -> 2 Brass;
- 3 Copper + 1 Gold -> 4 Rose Gold.

For every sized recipe:
- inserting fewer items must not start a process;
- building the stack one item at a time must be allowed by the GUI slot;
- exact quantity must be consumed at completion;
- output must be the pack-preferred OreDictionary implementation.

Manyullyn is intentionally absent in Test 1; do not report its absence as a regression.

## T7 — Insulating Glass compatibility
Test the new modern route:
- 2 glass blocks + 1 iron dust -> 2 Insulating Glass.

Also verify the legacy crafting recipe still works.

Expected:
- existing Insulating Glass registry/meta is preserved;
- old world stacks remain valid;
- modern texture appears on existing Insulating Glass;
- no duplicate Insulating Glass item/block exists.

## T8 — Fuel remainder
Use at least one valid furnace fuel with a container/remainder if present in the pack.

Expected:
- fuel stack decreases once;
- container/remainder is preserved exactly once;
- no duplication and no item deletion.

## T9 — Output backpressure
Fill the output slot so the result cannot fit.

Expected:
- no new recipe starts when output cannot accept the result;
- no input/fuel is consumed for a blocked start;
- clearing enough output space allows processing to resume normally.

## T10 — Mid-process input mutation
Start a recipe, then remove or replace either input before completion.

Expected:
- current progress cancels safely;
- no ingredient is consumed by the cancelled process;
- no stale output is produced;
- a subsequently valid recipe starts from its full duration.

## T11 — Persistence
Save/restart while:
- idle with inventory;
- burning but not currently processing;
- around 25%, 50% and 90% process progress;
- output is blocked.

Expected: inventory, process, processMax, burnTime, lastBurnTime, facing and active state restore coherently without dupes or resets outside modern semantics.

## T12 — Dismantle and drops
Test breaking:
- master;
- each dummy part;
- while idle;
- while processing;
- with inventory present.

Expected:
- seven surviving parts restore to Kiln Bricks;
- broken part drops one Kiln Brick when `doTileDrops=true`;
- master inventory drops exactly once;
- no unrelated nearby Kiln belongs to the dismantled master;
- no ghost formed block remains.

Repeat once with `doTileDrops=false`; no prohibited drops should appear.

## T13 — Modern visual / Angelica
For all four facings inspect all external faces while OFF and ON.

Expected:
- 2x2 textures join without quadrant rotation/mirroring errors;
- front switches between modern `off` and `on` artwork;
- top, bottom and sides use the correct modern surfaces;
- no z-fighting/internal-face artifact;
- no TESR-related FPS or Angelica issue is introduced.

## T14 — NEI
Verify Alloy Kiln recipe category shows:
- both sized inputs;
- correct output count;
- recipe time;
- usage lookup from either input;
- crafting lookup from output.

Check Bronze/Invar quantities visually when available.

## T15 — MineTweaker
In a disposable test script:
- add one recipe using an OreDictionary ingredient with amount >1;
- confirm it processes the exact amount;
- remove the recipe by output;
- reload scripts;
- confirm recipe cache invalidates after add/remove/reload.

No stale cached recipe may remain after script mutation.

## T16 — Existing feature regression
Retest minimally:
- Garden Cloche formation/GUI/one processing cycle;
- Auto Workbench formation/GUI/one blueprint recipe;
- Blast Furnace and Coke Oven GUI/open/process;
- existing Insulating Glass stack from an older world.

## T17 — Performance / scale
Measure a controlled baseline and then:
- 64 formed idle Kilns;
- 64 actively processing Kilns;
- 256 formed idle Kilns;
- 256 actively processing Kilns if the test machine remains stable.

Capture Spark/Java sampler evidence when practical.

Primary inspection targets:
- recipe lookup CPU;
- TileEntity tick cost;
- block-update traffic;
- memory/GC growth;
- client FPS with many visible Kilns.

The implementation intentionally caches successful and failed recipe lookups and only sends block visual updates when active state changes; any contrary runtime behavior is a regression.

## T18 — Evidence return
Preserve:
- client `fml-client-latest.log`/latest log;
- dedicated/server log when applicable;
- crash report if any;
- screenshots of OFF/ON facings and GUI;
- MineTweaker script used;
- performance capture;
- exact candidate SHA-256.

Classify every test as `PASS`, `FAIL`, or `NOT CONCLUSIVE`; do not silently skip a test.
