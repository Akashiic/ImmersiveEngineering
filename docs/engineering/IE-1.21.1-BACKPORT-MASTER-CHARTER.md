# Immersive Engineering 1.21.1 -> 1.7.10 — Master Backport Charter

Status: ACTIVE / CHIEF ENGINEERING TRACK
Date: 2026-09-11
Baseline checkpoint: `auto-workbench-test-1-r2` / `97cea596`
Planning branch: `chief/ie-1.21.1-masterplan`

## Mission
Transform the Forbidden Series Immersive Engineering 0.7.9 lineage into a modernized 1.7.10 edition that reproduces, as far as technically defensible, the gameplay, machines, multiblocks, equipment, visual identity, animation, GUI, sound, automation and quality-of-life surface of official Immersive Engineering 1.21.1.

This is not a source-level downgrade. It is a behavior-preserving architectural translation from NeoForge-era systems to Forge 1.7.10 contracts.

## Product definition
Success means high functional, visual and semantic parity with 1.21.1 while preserving the strengths and compatibility obligations of the real Forbidden Series runtime.

We explicitly target:
- modern machine and multiblock gameplay;
- modern geometry, textures and animation where assets/licensing permit;
- modern GUI/interaction semantics adapted to 1.7.10;
- modern sound behavior and machine soundscapes;
- safe multiplayer synchronization;
- old-world/registry compatibility;
- bounded CPU, GPU, allocation and network cost;
- compatibility with the actual Forbidden Series client stack.

Compilation is never an approval criterion by itself.
## Source-of-truth hierarchy
1. Real Forbidden Series runtime behavior, deployed 0.7.9 binary and current project evidence.
2. Recovered 0.7.9 source and already-approved backport checkpoints.
3. Official Immersive Engineering 1.21.1 branch for target behavior/assets/feature inventory.
4. Official historical IE commits closest to the architecture being translated.
5. Maintained 1.7.10 forks and GTNH ecosystem code after patch-level provenance review.
6. Documentation, issues and inference only when runtime/source evidence is insufficient.

## Frozen upstream snapshot for the first full audit
- repository: `BluSunrize/ImmersiveEngineering`;
- branch: `1.21.1`;
- commit: `75a27f03e4243544243567e8d5c38d336f4f10f4`;
- declared Minecraft: `1.21.1`;
- declared NeoForge: `21.1.164`;
- declared IE version: `12.4.3` build `194`.

The snapshot is evidence, not a vendored dependency. No modern NeoForge API is allowed to leak into the 1.7.10 runtime artifact.

## Current project state
The Garden Cloche backport is the approved first major feature family. The Automated Engineer's Workbench is feature #2 and currently sits at A6 user-runtime validation.

`auto-workbench-test-1-r2` is preserved unchanged while the full 1.21.1 gap audit proceeds in this isolated worktree.

No work in this branch may rewrite or silently invalidate the A6 candidate.
## First measured 1.21.1 surface
The reproducible audit tool `tools/audit_ie_1211_surface.py` currently reports:
- 398 modern block translation entries;
- 314 modern item translation entries;
- 25 modern multiblock definition classes;
- 54 modern sound events;
- 72 OBJ assets across the audited resource roots;
- 1,159 PNG assets across the audited resource roots.

The current 1.7.10 language surface contains 127 block and 129 item translation entries. Display-name comparison identifies 289 block and 183 item candidates not obviously represented in the legacy language surface.

These candidate counts are intentionally NOT treated as missing-feature counts: metadata-backed blocks, renamed features, generated variants and semantic equivalents produce false positives. Every candidate must be classified before implementation.

## Modern multiblock delta — initial classification
Already represented in the current lineage include Arc Furnace, Assembler, Auto Workbench, Blast Furnace families, Bottling Machine, Bucket Wheel, Coke Oven, Crusher, Diesel Generator, Excavator, Fermenter, Lightning Rod, Metal Press, Refinery, Sheetmetal Tank, Silo and Squeezer.

Strong missing/modernization candidates identified directly in 1.21.1 include:
- Alloy Smelter / Alloy Kiln;
- Chunk Loader;
- Feedthrough formation and modern connector semantics;
- Mixer;
- Radio Tower;
- Sawmill;
- Shelf multiblock/interaction family.

Each of these receives a separate charter before production code is written.
## Architectural translation rules
Modern concepts are translated to the smallest stable 1.7.10 equivalent rather than emulated wholesale.

| 1.21.1 concept | 1.7.10 strategy |
|---|---|
| BlockEntity + modern state | TileEntity + metadata/NBT + explicit sync |
| NeoForge capabilities | IE/Forge legacy interfaces, `IInventory`, `ISidedInventory`, RF/fluid adapters |
| Data components/attachments | versioned NBT contracts with migration guards |
| Deferred registries | existing IE registration lifecycle with collision audits |
| modern menu/data slots | legacy Container/GUI + bounded packets |
| modern baked/dynamic models | audited OBJ/TESR/ISBRH/custom model path |
| modern payload registration | explicit 1.7.10 packet discriminator + validation |
| modern recipe codecs | typed legacy registries and config/CraftTweaker bridges |

A new compatibility abstraction is allowed only when at least two feature families need it or when it measurably reduces correctness risk.

## Visual parity contract
Modern appearance is a first-class deliverable, not post-release polish.

For every visual feature we will preserve or deliberately reinterpret:
- silhouette and major proportions;
- visible mechanical parts;
- material assignment and UV intent;
- mirrored/facing transforms;
- emissive/translucent portions where technically safe;
- animation timing tied to real process state;
- inventory/item representation;
- GUI visual hierarchy.

Visual approval requires screenshots/video in the actual Forbidden Series client with Angelica enabled.
## Rendering technology policy
Angelica is a required compatibility target, not an implementation shortcut. Render code must avoid undefined OpenGL state and must be correct both with and without Angelica where practical.

GTNHLib may be evaluated for geometry/state/render helpers when it reduces bespoke code, but a hard dependency is not introduced until an explicit dependency gate proves the benefit.

GeckoLib Unofficial 1.7.10 is approved for evaluation, not blanket adoption. It is a strong candidate for genuinely skeletal/keyframed content, complex tools/entities and event-driven animation. Rigid industrial multiblocks should prefer deterministic IE-native OBJ/procedural rendering when that path is simpler and faster.

EntityCulling and other client optimizers are compatibility/performance environments, not APIs that IE should require. Dynamic IE block entities must be whitelist-tested if culling creates false negatives.

## Sound and DSP contract
The modern snapshot exposes 54 sound events versus 9 in the recovered legacy `sounds.json`; therefore sound parity is a substantial feature track.

Machine audio must be state-driven rather than packet-spammed. Loop start/stop, fades, pitch/volume modulation and one-shot mechanical transients must derive from authoritative machine state with client interpolation.

Rules:
- never send per-tick sound packets for stable loops;
- synchronize state transitions, not PCM/audio data;
- avoid starting duplicate loops after chunk reload or GUI interaction;
- define attenuation/range deliberately per machine class;
- profile simultaneous machine fields for CPU/mixer pressure;
- preserve clean stop behavior on unload, dimension change and multiblock disassembly;
- treat sound-event timing as gameplay-visible state when it accompanies an animation impact.

DSP-style processing, if introduced, must be client-local and bounded; no audio processing may affect server tick time.
## Networking and Netty contract
All multiplayer state is server-authoritative. Client packets are requests or UI intent; they are never trusted as proof of inventory, energy, ownership or machine state.

Every new packet family requires:
- fixed discriminator ownership;
- maximum payload size and collection bounds;
- side validation;
- dimension/chunk/tile existence validation;
- distance/permission checks for player-triggered actions;
- malformed-NBT rejection;
- no unbounded allocation from client-controlled lengths;
- no blocking I/O on the Netty event loop;
- packet-frequency profiling under repeated GUI interaction.

High-frequency visuals should use deterministic local reconstruction from sparse synchronized state whenever possible.

## Performance contract
Performance is designed before scale testing, not patched after release.

For each machine family measure at minimum:
- idle CPU cost;
- active CPU cost;
- allocations/tick;
- server-to-client bytes/sec;
- render calls / geometry cost;
- chunk-load and NBT deserialize cost;
- behavior at 1, 16, 64 and, where realistic, 256 instances.

No machine may scan an unbounded world area every tick. Expensive lookup work must be cached, scheduled, event-driven or amortized with explicit invalidation rules.

No renderer may rebuild invariant geometry every frame when a stable cache/key can be used safely.
## Runtime stack policy
The Forbidden Series optimization stack is treated as an execution environment with explicit compatibility gates.

- Angelica: primary modern rendering compatibility/performance target.
- GTNHLib: candidate library for modernized state, geometry, utility and selected networking helpers.
- Hodgepodge: pack-level fixes/optimizations; never assumed to be an IE API.
- UniMixins: surgical platform-hook mechanism only when clean in-mod adaptation is insufficient.
- GeckoLib Unofficial 1.7.10: optional animation engine for features that genuinely benefit from skeletal/keyframe animation.
- ArchaicFix: bugfix/performance environment; regression-test lighting, world and memory behavior.
- CoreTweaks: startup/core transformation environment; avoid transformer-order assumptions.
- EntityCulling: client performance environment; audit animated/dynamic block-entity visibility.
- ParallelMipMap: texture pipeline environment; verify large modern texture sets and mip behavior empirically.

The backport must still own its own correctness. Optimization mods are not permission to create pathological tick, allocation, rendering or packet behavior.

## Execution lanes
Lane A — preserve and finish the current Auto Workbench A6/A7/A8 chain.
Lane B — full 1.21.1 feature/asset/behavior inventory and semantic gap classification.
Lane C — reusable legacy adapters discovered by multiple backports.
Lane D — visual modernization and asset conversion pipeline.
Lane E — audio parity and state-driven sound runtime.
Lane F — performance, memory, packet and dedicated-server audits.

Lanes may progress in parallel only when they do not edit the same feature branch or invalidate a frozen test artifact.
## Roadmap hierarchy
Phase P0 — finish evidence-backed baseline and preserve world/registry compatibility.
Phase P1 — close Auto Workbench runtime validation.
Phase P2 — classify the entire 1.21.1 gameplay surface into PRESENT, MODERNIZE, BACKPORT, ADAPT, DEFER or REJECT.
Phase P3 — complete the missing 1.12-era foundations that reduce architectural distance: Alloy Kiln, Mixer and contained utilities.
Phase P4 — backport post-1.12 industrial content, prioritizing Sawmill and modern processing families.
Phase P5 — modernize equipment/tools and their rendering/audio behavior.
Phase P6 — modernize conveyors, redstone/control devices and fluid logistics as isolated subsystem projects.
Phase P7 — modern wire/connectors/feedthrough behavior with explicit network-regression gates.
Phase P8 — modern decorative/construction surface and world-facing content.
Phase P9 — Radio Tower and other architecturally distant systems after prerequisite adapters mature.
Phase P10 — full parity audit against the frozen 1.21.1 inventory, performance closure and release-candidate freeze.

The phase order is dependency-driven, not a promise that every candidate is automatically accepted.

## Feature approval state machine
`DISCOVERED -> PROVENANCE_LOCKED -> DESIGNED -> IMPLEMENTED -> STATIC_PASS -> RUNTIME_TEST_1 -> ADVERSARIAL_FIX -> RUNTIME_TEST_2 -> APPROVED`

Any failed gate returns to the smallest responsible layer.

A feature can also end in `DEFERRED` or `REJECTED` when its architecture, license, compatibility or measured cost is unacceptable.
## Chief approval gate
The approver must explicitly reject advancement when evidence is incomplete. `NOT TESTED`, `NON-CONCLUSIVE` and `ASSUMED` are never aliases for PASS.

Approval requires, as applicable:
- source provenance and license review;
- clean Java 8/reobfuscated build;
- ABI/registry delta audit;
- client and dedicated-server initialization;
- old-world load test;
- functional and adversarial runtime tests;
- Angelica visual test;
- optional-mod absence/classloading test;
- duplication/loss testing;
- NBT/restart/chunk-unload testing;
- network abuse/bounds inspection;
- scale/performance evidence;
- final artifact hash and immutable tag.

## Concurrency / workstation rule
The primary Auto Workbench worktree remains untouched by this planning branch. New experiments use dedicated branches/worktrees and uniquely named evidence/output directories.

After any remote transport failure, the last confirmed Git/filesystem state must be revalidated before a write, build, clean, move, delete or commit is repeated.

## Immediate authorized work
1. Preserve the R2 Auto Workbench candidate for user A6.
2. Complete the machine-readable 1.21.1 surface inventory.
3. Replace the old 1.12-centric gap roadmap with a classified 1.21.1 matrix without deleting historical evidence.
4. Build a reusable visual/audio/network/performance adapter plan from proven needs, not speculative abstraction.
5. Select Feature #3 only after its dependencies and blast radius are re-ranked against the full 1.21.1 target.
