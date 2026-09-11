# Immersive Engineering 1.21.1 -> Forbidden 1.7.10 — Gap Matrix

Date: 2026-09-11
Status: ROUND 1 — SOURCE-BASED CLASSIFICATION
Upstream snapshot: `75a27f03e4243544243567e8d5c38d336f4f10f4`

Legend:
- PRESENT: same gameplay family exists; parity still requires later audit.
- BACKPORTED: added by this project.
- BACKPORT: clearly absent and suitable for reconstruction.
- MODERNIZE: legacy equivalent exists but modern behavior/visuals are materially richer.
- HIGH RISK: backportable, but coupled to central systems/security/world state.
- DEFER: not an early dependency target.

## Multiblocks
| 1.21.1 family | Current classification | Next engineering action |
|---|---|---|
| Coke Oven | PRESENT | semantic/visual parity audit |
| Crude Blast Furnace | PRESENT | semantic/visual parity audit |
| Improved Blast Furnace | PRESENT | parity + preheater audit |
| Arc Furnace | PRESENT | modernization + sound/render audit |
| Assembler | PRESENT | modernization audit |
| Bottling Machine | PRESENT | modernization audit |
| Bucket Wheel / Excavator | PRESENT | coupled visual/performance audit |
| Crusher | PRESENT | modernization + sound audit |
| Diesel Generator | PRESENT | modernization + sound audit |
| Fermenter | PRESENT | modernization + sound audit |
| Lightning Rod | PRESENT | behavior parity audit |
| Metal Press | PRESENT | modernization + sound/render audit |
| Refinery | PRESENT | modernization + sound audit |
| Sheetmetal Tank | PRESENT | parity audit |
| Silo | PRESENT | parity audit |
| Squeezer | PRESENT | modernization + sound audit |
| Garden Cloche | BACKPORTED | preserve approved baseline |
| Auto Workbench | BACKPORTED / A6 | finish runtime gates |
| Alloy Smelter / Alloy Kiln | BACKPORT | strong Feature #3 candidate |
| Mixer | BACKPORT | fluid-process adapter + renderer/audio gate |
| Sawmill | BACKPORT | post-1.12 premium machine; animation/sound-heavy |
| Chunk Loader / Resonanz Observer | HIGH RISK | chunk-ticket policy, quota and cleanup design first |
| Feedthrough | HIGH RISK | defer until wire-network modernization |
| Radio Tower | HIGH RISK | wireless redstone + range/performance/security design |
| Shelf | BACKPORT | storage/NBT/render/automation design; 24 crate positions upstream |

## Single-block machine/device families
Direct modern-source candidates include:
- Fluid Placer;
- Fluid Sorter;
- Item Batcher;
- Turntable;
- Circuit Table / Engineer's Circuit Workbench;
- Logic Unit;
- Machine Interface;
- Electromagnet;
- Energy Meter;
- Pipe Valve;
- Siren;
- Redstone State Cell;
- Redstone Switchboard;
- Redstone Timer;
- Chemthrower Turret and Gun Turret;
- Razor Wire;
- modern chutes and metal ladder/catwalk families.

Initial rule: contained devices may move ahead of central-network rewrites, but each receives a registry/meta and packet audit before implementation.
## Logistics / wire / redstone modernization
The 1.21.1 source has dedicated families for:
- LV/MV/HV connectors and relays;
- redstone wire connector;
- redstone probe connector;
- bundled redstone connector;
- feedthrough;
- post transformer;
- modern transformer behavior;
- extracting, splitting, dropping, vertical, redstone-controlled and covered conveyor variants.

This surface is valuable but high blast-radius because legacy IE already has working wire and conveyor assumptions.

Policy:
1. do not bulk replace legacy networks;
2. introduce adapters/variants behind dedicated registry gates;
3. preserve existing saves/connections;
4. fuzz chunk unload/reload and malformed connection state;
5. benchmark entity/conveyor collision and insertion paths;
6. execute full regression tests against all pre-existing wire types.

## Tools, equipment and player-facing systems
Direct modern-source candidates absent or materially newer include:
- Buzzsaw + Sawblade + Rockcutter + Grinding Disk;
- Survey Tools;
- Collapsible Glider;
- Speedloader workflow;
- Ear Defenders;
- Faraday Suit;
- Fluorescent Tube;
- Accumulator/Powerpack;
- Heavy Plated Shield;
- Maintenance Kit;
- Logic Circuit Board;
- modern fertilizer workflow;
- Robot Wolf and newer hostile IE entities.
## Construction / decoration surface
The modern block registry is substantially broader than 0.7.9. Source-visible families include:
- alloy brick;
- clinker brick/sill/quoin;
- expanded concrete/hempcrete variants;
- insulating and slag glass;
- catwalks and catwalk stairs;
- expanded scaffolding;
- windows, posts, wallmounts and warning signs;
- cage lamp and lighting variants;
- chutes in multiple sheetmetal/material variants;
- signs/hanging signs;
- strip curtains, cushions and shader banners;
- blueprint shelf, desk and modern storage/construction variants.

These are not all trivial: collision shape, connected model, transparency, metadata pressure and block-ID budget must be designed before registration.

## Audio gap
Modern `sounds.json` defines 54 events while the current recovered legacy file defines 9.

High-value sound families include:
- multi-stage drill and buzzsaw loops/transients;
- metal press piston/smash;
- assembler, refinery, bottling and mixer loops;
- sawmill startup/idle/full/shutdown states;
- arc furnace, preheater and fermentation processing;
- ore conveyor/dump and process/lift events;
- mill creaking;
- electromagnet;
- siren, klaxon and buzzer.

Audio parity is therefore tracked as an independent subsystem, not embedded ad hoc in each renderer.
## Recommended dependency-driven sequence after Auto Workbench
1. Alloy Kiln — compact multiblock, low blast radius, validates the next registry/process family.
2. Mixer — establishes modern fluid-process semantics and richer animated/audio machine support.
3. Sawmill — exercises post-1.12 process animation, item visualization and multi-state sound design.
4. Contained devices — Turntable, Fluid Placer, Item Batcher, Fluid Sorter and selected control blocks.
5. Equipment track — Buzzsaw family, Powerpack, Shield, Earmuffs/Faraday and Maintenance Kit.
6. Conveyor variants — isolated transport subsystem modernization.
7. Redstone/control network family — probes, bundled redstone, state cells/switchboard/timer.
8. Feedthrough and wire modernization — only after dedicated wire regression infrastructure exists.
9. Chunk Loader and Radio Tower — only after explicit server policy/performance/security review.
10. Broad decorative/architectural parity and remaining feature closure.

This ordering is provisional until the full 398-block/314-item surface is semantically classified.

## Immediate auditor decisions
APPROVE continued A6 testing of `auto-workbench-test-1-r2` unchanged.

APPROVE the full 1.21.1 source audit in parallel on the isolated chief branch.

DO NOT APPROVE starting a new production feature branch until its registry slots, provenance, modern dependency graph and regression matrix are documented.

DO NOT APPROVE a hard dependency on GeckoLib or GTNHLib yet; both are evaluation candidates and require measured benefit.

DO NOT APPROVE wholesale replacement of the legacy wire/conveyor subsystems.
