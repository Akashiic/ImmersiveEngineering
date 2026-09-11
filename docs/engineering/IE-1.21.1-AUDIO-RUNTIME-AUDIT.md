# IE 1.21.1 -> 1.7.10 Audio Runtime Audit

Date: 2026-09-11
Status: DESIGN GATE A0 — INITIAL PASS WITH LEGACY REWORK REQUIRED

## Measured surface
The frozen 1.21.1 snapshot defines 54 sound events. The recovered Forbidden 0.7.9 baseline currently defines 9.

This makes audio a dedicated backport subsystem rather than a per-feature afterthought.

## Legacy 1.7.10 architecture — findings
`IESound` implements `ITickableSound` and reevaluates volume every 40 client ticks.

Its current muffler compatibility path scans TileEntities in nearby chunks looking for a class whose name ends in `TileEntitySoundMuffler`. That is acceptable only because the old client uses very few shared machine sound instances.

`ClientProxy.soundMap` is keyed only by sound name. For a given looping sound family, the old implementation effectively keeps one sound and moves it to the nearest active TileEntity of that family.

Consequences:
- CPU is bounded better than one sound object per machine;
- simultaneous spatial sources of the same family are not represented faithfully;
- naively changing the map to one sound per machine would multiply the legacy muffler scan cost;
- modern sound parity therefore requires a new bounded controller, not a key change.
## Modern 1.21.1 reference architecture
`IEBlockEntitySound` keeps a sound anchored to one block position, validates the owning block entity and computes distance/radius attenuation. Its current volume evaluation no longer scans neighboring TileEntities for mufflers.

`MultiblockSound` receives `active` and `valid` suppliers, remains instantiated while valid, and drives volume to zero when the multiblock is inactive. This separates loop lifetime from process activity and avoids repeatedly starting/stopping the source.

Modern sound families visible in `sounds.json` include machine loops, tool layers and mechanical transients such as:
- diesel generator, crusher, assembler, refinery, bottling and mixer;
- metal press piston and smash;
- drill idle/busy/fade/attack/harvest;
- buzzsaw idle/busy/fade/attack/harvest;
- sawmill startup/empty/full/shutdown;
- arc furnace and preheater;
- ore conveyor/dump and process/lift events;
- electromagnet, siren, klaxon and buzzer.

## Decision
REJECT direct multiplication of the old `IESound`/`soundMap` pattern across the modern catalog.

APPROVE design of a bounded client-side IE sound scene/controller before broad sound backports.
## Proposed 1.7.10 sound scene architecture
Client-only components:
- `IESoundSceneManager`: owns active emitter registrations and voice arbitration.
- `SoundEmitterKey`: dimension + block position + semantic role.
- `SoundEmitterState`: sound id, active/valid flags, radius, base volume, pitch and optional process phase.
- `SoundFamilyPolicy`: loop/transient behavior, voice budget and priority rules.
- `ManagedIESound`: the actual `ITickableSound`, with fade envelope and no world scans.

TileEntities/render state register or update an emitter only when relevant state changes. The manager must not discover machines through a per-tick world scan.

For repeated machine families, the manager may virtualize distant emitters and instantiate only the most relevant nearby voices. The exact per-family voice budget is intentionally not fixed until profiling proves a safe value.

Use hysteresis when replacing a selected voice so two similarly distant machines do not cause audible source thrashing.

## Networking contract
Stable loops are reconstructed client-side from synchronized TileEntity/multiblock state. Do not send a sound packet every tick.

Network events are reserved for non-derivable one-shot transients such as impact/fire/cut events when existing world sound propagation is insufficient.

A sound message, if required, contains only bounded identifiers/state and never user-controlled resource paths or arbitrary payloads.
## Mufflers and hearing protection
The legacy sound-muffler behavior must be preserved without multiplying world scans by active voice count.

Proposed rule:
- perform muffler discovery through one shared spatial cache/query service per client world;
- amortize discovery across all IE emitters;
- invalidate or refresh conservatively on chunk/lifecycle changes;
- keep third-party muffler detection behind a compatibility adapter rather than inside `ManagedIESound`;
- never iterate every nearby TileEntity once per active sound.

When modern Ear Defenders/Earmuffs are backported, player-local attenuation belongs in the scene/controller layer so one equipment lookup can affect all IE voices consistently.

Exact muffler integration remains DESIGN-PENDING until the installed muffler implementation and its available API/hooks are audited.

## DSP / sound-design policy
Prefer authored OGG layers plus cheap runtime envelopes over custom PCM processing.

Approved first-line techniques:
- attack/release fades;
- crossfades between idle/busy/full states;
- bounded pitch/volume modulation from process phase/RPM/load;
- synchronized one-shot transients for impacts, cuts and dumps;
- distance attenuation and voice priority.

Real-time filtering, convolution/reverb or custom OpenAL manipulation is not approved without a separate compatibility and CPU benchmark gate.
## Lifecycle invariants
Every loop is keyed by world/dimension + owning position + semantic sound role.

A loop must terminate when:
- the client changes world/dimension;
- the owning TileEntity/multiblock becomes invalid;
- the structure is disassembled;
- the owning chunk is no longer a valid loaded sound source;
- the feature is disabled or its sound policy changes.

Rapid active/inactive transitions should change an envelope/state flag, not allocate a new sound instance each tick.

One-shot sounds must never become persistent emitters.

## Performance gate
Profile the controller with at least 1, 16 and 64 nearby emitters, and a larger synthetic stress case when practical.

Acceptance requires evidence that:
- emitter bookkeeping remains bounded;
- active audible voice count follows the configured policy rather than total machine count;
- there is no per-emitter TileEntity/chunk scan;
- no network traffic scales per client tick for stable loops;
- allocations do not grow pathologically during steady state;
- repeated listener movement does not churn sound objects continuously.

No fixed CPU threshold is declared until a reproducible profiler baseline exists on the real Forbidden client.
## Runtime regression matrix
Minimum scenarios before the sound subsystem can become a shared dependency:
- one machine toggling active/idle repeatedly;
- 16 and 64 machines of one sound family;
- multiple sound families active simultaneously;
- two same-family machines on opposite sides of the listener;
- player walking rapidly between competing emitters;
- chunk unload/reload and dimension transition;
- multiblock disassembly while a loop is active;
- client reconnect with machines already active;
- muffler present/removed when supported by the audited integration;
- Ear Defenders equipped/removed after their backport;
- missing or malformed sound resource fails closed without crashing;
- dedicated server loads with zero client sound-class references from common/server paths.

## Approval
A0 architectural direction: **PASS WITH REQUIRED REWORK**.

Approved:
- modern catalog audit;
- a shared bounded sound scene/controller;
- state-derived loops;
- authored layered audio plus inexpensive envelopes;
- sparse semantic one-shot events.

Rejected:
- one legacy muffler world scan per emitter;
- per-tick sound packets;
- uncontrolled per-machine loop creation;
- speculative real-time DSP/OpenAL hooks before profiling.

Next gate: inspect exact machine-state synchronization paths and third-party muffler implementation before writing production sound-runtime code.
