# Network Hardening Gate 1 — Generic Tile GUI C2S

Date: 2026-09-11
Branch: `hardening/network-gate`
Baseline: `auto-workbench-test-1-r2` / `97cea596`
Status: STATIC + ARTIFACT PASS; RUNTIME NOT YET APPROVED

## Scope
This gate hardens the legacy generic `MessageTileSync` path currently used by:
- Conveyor Sorter GUI configuration;
- Assembler GUI / NEI pattern synchronization;
- Automated Engineer's Workbench recipe selection.

The frozen R2 gameplay candidate is not modified.

## Proven defects addressed
Exact FML 1.7.10 source proves `IMessageHandler.onMessage()` is called from the Netty channel callback. The legacy handler previously resolved worlds/TileEntities and mutated game state there.

The legacy envelope also trusted client-provided dimension/coordinates/NBT without sender identity, proximity, current-container authorization or unloaded-chunk rejection.

Assembler `patternSync` additionally trusted recipe and slot indices. Sorter accepted an arbitrary `sideConfig` array shape/mask.
## Implemented server ownership boundary
Netty now retains only a bounded encoded payload plus primitive target envelope. It does not touch World or TileEntity state.

`ServerNetworkTaskQueue` owns C2S execution on `ServerTickEvent.START` with:
- global pending cap: 64;
- per-player pending cap: 4;
- maximum work drained per tick: 16 actions;
- aggregated/rate-limited drop telemetry;
- exception isolation per task.

Server-thread validation requires:
- authenticated `EntityPlayerMP` from `MessageContext`;
- sender's current dimension equals target dimension;
- target within 64 squared blocks;
- target chunk already loaded;
- target is still the addressed `TileEntityIEBase`;
- machine-specific current-container authorization.

No client request in this path may force-load a chunk.

## NBT boundary
Wire compatibility is retained: outgoing clients still use the historical dimension/x/y/z + FML NBT encoding.

Inbound server decoding now stores at most 32,769 encoded bytes on the network callback. NBT inflation is deferred to the server tick and uses a 256 KiB `NBTSizeTracker` limit instead of the legacy 2 MiB generic decoder ceiling.
## Consumer-specific validation
### Auto Workbench
Recipe-selection packets require the sender to have `ContainerAutoWorkbench` open for the exact master TileEntity. Existing blueprint/category/index validation remains authoritative.

### Assembler
Packets require `ContainerAssembler` for the exact TileEntity. `patternSync` now bounds the recipe index, caps iteration to pattern inventory size and validates every supplied slot before indexing.

### Conveyor Sorter
Packets require `ContainerSorter` for the exact TileEntity. `sideConfig` must contain exactly six integers and every value is restricted to the supported three flag bits.

## Build evidence
Current hardened state passed:
- `compileJava`;
- clean `build`;
- `reobfJar`;
- `checkstyleMain`;
- `check`.

Clean build result: `BUILD SUCCESSFUL in 2m 19s`, 23 actionable tasks, 22 executed and 1 up-to-date.

## Artifact evidence
Compared to exact R2 baseline:
- 1,410 entries / 1,410 unique / zero ZIP duplicates;
- zero removed entries;
- zero non-class runtime-resource delta;
- 752/752 classes are Java major 52;
- no newly introduced common-side client references;
- mod identity and FMLAT preserved;
- all three Auto Workbench OBJ hashes remain byte-identical to R2.
## Reproducibility fix
Windows worktree checkout previously converted audited OBJ assets to CRLF even though the Git blobs/R2 artifact used LF. `.gitattributes` now pins `*.obj` and `*.mtl` to LF so future clean worktrees reproduce asset hashes deterministically.

## Decision
**PASS — static/build/artifact only.**

This does not authorize production merge or replace gameplay/runtime testing. Required runtime follow-up:
- Sorter toggles and ghost filters;
- Assembler GUI buttons and NEI transfer;
- Auto Workbench recipe selection;
- rapid legitimate clicks under quota;
- reconnect/close-container race;
- malformed packet rejection;
- dedicated-server stability and thread audit.

Further C2S handlers (`MessageRequestBlockUpdate`, minecart shader request) remain outside this gate and are still pending hardening.
