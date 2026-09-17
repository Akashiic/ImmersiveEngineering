# Garden Cloche — Multiplayer Hardening Audit

Date: 2026-09-16
Baseline: `garden-cloche-test-2` / commit `ad5e6201`
Branch: `hardening/garden-cloche-multiplayer`
Scope: multiplayer authority, state sync, automation, break/drop safety and dedicated-side boundaries.

## Decision model
Only proven multiplayer defects are patched. Later IE gameplay changes are not imported merely because they are newer.

Evidence order:
1. exact recovered 1.7.10 source/runtime;
2. Forge 1.7.10 source;
3. official IE history and mature implementations;
4. static reasoning only after the above.

## Server authority — PASS
`TileEntityGardenCloche.updateEntity()` runs production only on the server.
The client branch returns after visual `renderGrowth` interpolation.
RF extraction, fertilizer/water consumption, output creation and automatic ejection are therefore server-authoritative.

The Cloche does not override `receiveMessageFromClient`; generic IE tile-sync packets cannot mutate Cloche state through a custom client payload.
## Multi-player inventory observation — PASS before patch
A 2022 upstream fix (`bea0e9dd8`) addressed a case where one player changed the Cloche menu and another observer retained stale slot contents.

The 1.7 backport already avoids that failure:
- `Utils.readInventory()` allocates a new seven-slot array for every description-NBT read, so emptied slots become null;
- `onInventoryChanged()` calls both `markDirty()` and `markForUpdate()`.

No additional slot-clearing patch was applied.

## Break/drop path — PASS before patch
The three-block machine resolves to the master on break.
`master.breaking` is set before sibling blocks are removed, sibling parts are marked breaking, and master inventory is cleared after one drop pass.
Creative harvesting sets `suppressMachineDrop` on the master.
No duplicate-drop path was proven, so no break logic was changed.

## Placement/rotation — PASS before patch
The 1.7 ItemBlock preflights both upper positions before forming the three-block Cloche. This already contains the historical placement fix from `b3cdfd490`.
`rotateFacing()` changes all three parts server-side, covering the later dummy-rotation issue fixed upstream.
## Patch 1 — 1.7 window-property truncation
Forge 1.7.10 `S31PacketWindowProperty` serializes both property ID and value using signed 16-bit shorts.

The Test 2 Container sent `fertilizerAmount` and a scaled fertilizer multiplier as ordinary ints. Server configs/addons can exceed 32767, causing a remote client to receive negative/corrupted values.

The multiplayer patch transports full 32-bit values as high/low 16-bit halves:
- fertilizer amount: signed int round-trip;
- fertilizer modifier: exact `Float.floatToIntBits` round-trip;
- server fertilizer duration: signed int round-trip;
- server growth modifier: exact float-bit round-trip.

High halves are sent before low halves; receiving the low half commits the reconstructed value.
Energy (16000 max), tank amount (4000 max), normalized growth and active flag remain direct properties because their valid domains fit in a signed short.
## Patch 2 — server configuration is authoritative for client visuals
Test 2 GUI/render code read `gardenCloche_fertilizerDuration` and the global growth modifier from the local client config.
A client with a different config could therefore display an incorrect fertilizer percentage and interpolate crop growth at a different rate than the server.

Description NBT now carries:
- `clocheSyncFertilizerDuration`;
- `clocheSyncGrowthModifier`.

The open Container also refreshes both values using the 32-bit-safe property encoding.
Server calculations continue to read the actual server config; only client display/interpolation uses the synchronized values.

## Patch 3 — fluid type is never guessed
Test 2 Container reconstructed every synchronized tank amount as WATER.
`BelljarHandler.registerFluidFertilizer` is public, so valid addons may register other fertilizer fluids.

Forge 1.7 `FluidStack.writeToNBT` serializes `FluidName`, and Cloche tank mutations already call `markForUpdate()`.
The description packet therefore remains the authority for fluid type and NBT tags.
Container amount updates now preserve the client's currently synchronized FluidStack instead of fabricating water or relying on numeric fluid IDs.
## Historical fixes explicitly checked
- `6bee65120`: output handler must resolve master — covered by this backport's master-routed inventory access.
- `0667b39a1`: reject unsupported fluids — covered by `BelljarHandler.getFluidFertilizerHandler` in fill/canFill.
- `20cbdfe10`: seed/output loss — covered by atomic staged output commit introduced before Test 2.
- `bea0e9dd8`: two-player stale menu/slot sync — already covered by new-array inventory reads plus `markForUpdate`.
- `5036946d3`: fertilizer client sync for smoother growth — Test 2 already description-syncs fertilizer state; this patch hardens its 1.7 Container transport.
- `6d6c137f9`: active-state sync — the backport already sends an update on active/inactive transitions.
- `23180b3d3`: dedicated-server client-classloading — re-audited after this patch.

## Performance decision
The staggered 32-tick growth correction packet is retained. The official 1.12.2 Belljar uses the same 32-tick coordinate-staggered sync cadence.
Automatic output remains up to 16 items per output slot every eight ticks; this also matches the mature 1.12.2 implementation.
No speculative throughput reduction was applied.

## Static gates
`audit_garden_cloche_multiplayer.py` validates signed-short reconstruction, exact float-bit reconstruction, unique sync IDs and the Forge packet contract.
`compileJava + checkstyleMain` passes after the patch.
Common-side bytecode contains zero references to `net.minecraft.client.*` in the modified TileEntity and Container.