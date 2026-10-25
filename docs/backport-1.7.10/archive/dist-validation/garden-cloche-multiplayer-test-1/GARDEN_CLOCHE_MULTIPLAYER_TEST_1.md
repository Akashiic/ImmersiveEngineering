# Garden Cloche — Multiplayer Test 1

Use the multiplayer-hardening candidate on both client and server. Do not mix it with `garden-cloche-test-2` during this test.

## T1 — Two observers
1. Player A opens the Cloche GUI.
2. Player B watches the crop model without opening the GUI.
3. A inserts/removes soil, seed and fertilizer repeatedly.
4. B must see soil/crop changes without reconnecting or reloading chunks.
5. After removing seed/soil, no ghost item/plant may remain for B.

## T2 — Two open GUIs
1. A and B open the same Cloche simultaneously.
2. A shift-clicks seed/fertilizer/soil in and out.
3. B's GUI must update to the same slot contents.
4. No duplicated stack, negative stack size or rollback after closing either GUI.

## T3 — Server authority
1. Give the machine RF, valid soil/seed and water.
2. Observe one full harvest with both players online.
3. Compare output before/after; one server harvest must create exactly one output transaction.
4. Disconnect/reconnect one client mid-cycle; server growth/output must continue unchanged.
## T4 — 16-bit boundary regression
1. In a test server config, set fertilizer duration above 32767, preferably `70000`.
2. Leave the client config at its normal/default value.
3. Restart server/client as required by the config system.
4. Open the Cloche GUI remotely.
5. Fertilizer percent/bar must remain non-negative and coherent as fertilizer ticks down through 65536 and 32768.
6. No negative/corrupted multiplier or GUI bar is acceptable.
7. Restore the production config after testing.

## T5 — Client/server config mismatch
1. Set server Garden Cloche growth modifier to a clearly different test value from the client's local config.
2. Player B watches crop animation without opening the GUI.
3. Player A opens the GUI and checks the total-growth tooltip.
4. Both clients must reflect the server value, not their local config.

## T6 — Automation + players
1. Feed fertilizer/water through automation while both players have the GUI open.
2. Extract outputs from the rear using the normal pack automation.
3. Fill the destination inventory so ejection is partially/fully blocked.
4. Harvest must pause safely when all output slots cannot accept a transaction.
5. Free space and confirm normal resume with no deletion/duplication.
## T7 — Break/creative/explosion
1. With inventory contents present, break bottom, middle and top in separate tests.
2. Each machine must produce one machine drop plus one copy of stored inventory, never duplicates.
3. Repeat in creative: machine drop must remain suppressed as before.
4. Repeat while another player has the GUI open; the remote container must close/invalidates normally and no item may be cloned.

## T8 — Restart/chunk boundary
1. Leave a Cloche active, another partially blocked, and another with fertilizer state near a 16-bit boundary.
2. Restart the server.
3. Reconnect two players and verify inventory, tank, growth, fertilizer state and output behavior.
4. Unload/reload the chunk and repeat the observation.

## T9 — Optional custom fertilizer fluid
Only if the pack has an addon that registers a non-water `FluidFertilizerHandler`:
1. Fill the Cloche with that fluid.
2. Open the GUI from a remote client.
3. The tank must show the real registered fluid, not water.
4. Its NBT/tag data and amount must remain intact while the amount changes.

## Evidence to return
Send client logs from both players, dedicated-server log and screenshots/video if any visual disagreement occurs. A failure must include which test number reproduced it.