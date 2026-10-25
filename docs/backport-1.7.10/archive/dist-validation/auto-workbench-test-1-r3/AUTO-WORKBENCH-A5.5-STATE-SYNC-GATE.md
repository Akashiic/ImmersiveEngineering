# Auto Workbench A5.5 — State Persistence and GUI Sync Gate

Date: 2026-09-17
Status: R2 REJECTED; R3 source patch PASS pre-tag

## Why R2 was reopened
After A5.4, an adversarial review focused on mutable process state that compile/startup gates cannot prove.
Two defects were confirmed in `auto-workbench-test-1-r2`.

## Defect 1 — active process state did not re-dirty the chunk
R2 advances `processTick` and extracts RF every active server tick.
However, it only called `markMasterForUpdate()` when queue membership/ticked-process count changed.
During a stable process, mutable NBT state could continue changing without calling `TileEntity.markDirty()`.

Minecraft 1.7.10 source explicitly documents `markDirty()` as the mechanism ensuring the containing chunk is saved later rather than skipped as unchanged.
`ChunkProviderServer.saveChunks()` only saves chunks for which `Chunk.needsSaving(...)` returns true and clears `chunk.isModified` after saving.
Normal chunk unload unconditionally saves the chunk, but autosave/forced save selection still depends on the chunk dirty state.
Therefore an autosave can clear `isModified`, followed by Auto Workbench progress/energy changes that are not marked dirty, leaving a later save eligible to persist stale process state.

Mature IE `0.12-89` calls `updateMasterBlock(null, true)` for every process tick. The historical commit adding this behavior is `33713b964b6b9120644b763968891193e70b24e2` (2017-10-24), titled `Fixed multiblocks not updating visually when running off the internal buffer, closes #2579`.
Its stated purpose was visual synchronization, but `updateMasterBlock` also calls `master.markDirty()`.

### R3 correction
When at least one process actually advances, `tickServerProcesses()` now calls `markDirty()` exactly once for that server tick.
It does not force a block update every process tick.
This preserves process progress and RF state while retaining the lighter custom client animation sync already used by the 1.7 backport.
## Defect 2 — GUI energy display had no continuous sync channel
`GuiAutoWorkbench` renders its RF bar from the client tile's `EnergyStorage`.
R2 energy input only called `markDirty()`, and `ContainerAutoWorkbench` had no progress-property synchronization.
An idle charging machine could therefore display stale or zero RF until another tile description update occurred.

The approved Garden Cloche already provides the correct 1.7 pattern: `Container.detectAndSendChanges()` sends changed energy through `ICrafting.sendProgressBarUpdate`, and the client writes it with `EnergyStorage.setEnergyStored()`.
The CoFH API documents `setEnergyStored()` specifically for server-to-client synchronization.

`ICrafting` warns that properties are truncated to signed shorts in non-local SMP. Auto Workbench capacity is 32,000 RF, below `Short.MAX_VALUE` 32,767, so one property safely carries the full value.

### R3 correction
`ContainerAutoWorkbench` now sends property `0` only when energy changes while the GUI is open.
`updateProgressBar(0, value)` applies that value to the client `EnergyStorage`.
No global block update is required for idle charging.
## Process-index sync review
R2 synchronizes only the number of processes that advanced during the last server tick; the client advances the first N queued processes.
A theoretical mismatch requires processes in the same queue to have different RF/t costs.

The exact 0.7.9 source mutates `autoWorkbench_energyModifier` and `autoWorkbench_timeModifier` only inside `Config.init()`; no internal hot-reload/mutator path exists.
Processes created during one supported runtime therefore share the same configured cost semantics.
No per-index bitmask is added in R3 because the mismatch is not reachable through the supported configuration flow.

## Gates already passed on R3 source patch
- `compileJava + processResources + checkstyleMain`: PASS (`BUILD SUCCESSFUL in 50s`);
- state-sync source auditor: 16/16 PASS;
- `TileEntityAutoWorkbench`: class major 52, 0 client refs;
- `ContainerAutoWorkbench`: class major 52, 0 client refs;
- recipe/queue/output/registry/render semantics unchanged.

## Decision
`auto-workbench-test-1-r2` is no longer authorized for A6 gameplay testing.
R2 remains immutable evidence and must not have its tag moved.
The next candidate is R3 and must repeat clean tagged build, artifact audit and startup/runtime smoke before A6 is reopened.