# Immersive Engineering 1.7.10 Backport — Project Charter

Status: ACTIVE / BASELINE RECOVERY
Date: 2026-09-09

## Mission
Build a technically modern, world-compatible continuation of Immersive Engineering for Minecraft 1.7.10, using the Forbidden Series runtime as the compatibility target and selectively backporting modern Immersive Engineering functionality without destabilizing existing 1.7.10 gameplay, registries, saves, energy networks, rendering, or mod integrations.

## Source-of-truth hierarchy
1. Runtime behavior and binary of the Forbidden Series IE 0.7.9 currently deployed.
2. Public source ancestry proven by commit/file/semantic matches.
3. Official BluSunrize 1.7.10 source history.
4. Later 1.7.10 community continuations after patch-by-patch audit.
5. Modern Immersive Engineering source for feature semantics only; no blind copying.
6. Documentation/changelog/inference only when source/runtime evidence cannot answer the question.

## Verified production baseline
- Minecraft: 1.7.10
- Installed IE artifact: ImmersiveEngineering-1.7.10-0.7.9.jar
- IE artifact SHA-256: f68b4bef165414ae751d00ef2e1eb2a9692b196239e45193a6bc82d37bb92fb8
- IE metadata description: Forbidden Series Edition
- IE compiled bytecode: Java 8 / class major 52
- Exact modpack inventory + SHA-256 snapshot: evidence/modpack-mods-baseline.json

Important installed integration targets observed in the real pack include:
- AgriCraft-1.5.2-evernife-v1.jar
- magicalcrops-4.0.0_PUBLIC_BETA_5.jar
- GardenStuff-1.7.10-1.7.0.jar (separate mod; not IE Garden Cloche)
- gtnhlib-0.11.41.jar
- hodgepodge-2.7.189.jar
- angelica-2.2.8.jar
- +unimixins-all-1.7.10-0.3.1.jar
- NotEnoughItems-2.8.125-GTNH.jar
- Forestry-4.11.36.jar
- Pam's HarvestCraft 1.7.10Lb.jar

## Non-negotiable engineering constraints
- Never overwrite or mutate JARs in the production modpack during development.
- Preserve mod id `ImmersiveEngineering` and existing registry semantics unless a migration is explicitly designed and tested.
- Existing Forbidden Series worlds must remain loadable without unintended block/item remaps.
- No feature is accepted solely because it compiles.
- No patch is accepted without provenance, rationale, and a regression test or inspection criterion.
- Soft integrations must not cause classloading failures when optional mods are absent.
- Client-side rendering changes must be tested with Angelica enabled and disabled where practical.
- Dedicated-server classloading is a first-class gate; no accidental client-class references on server paths.
- Performance regressions require evidence and profiling, not intuition.

## Primary milestones
M0 — Source archaeology and reproducible evidence.
M1 — Recovered 0.7.9-compatible source baseline.
M2 — Modern RFG build toolchain targeting Java 8 bytecode and Forge 1614.
M3 — Legacy hardening: audited bugfixes from later 1.7.10 forks.
M4 — Backport framework/API and compatibility adapter layer.
M5 — Garden Cloche core backport.
M6 — Magical Crops Beta 5 adapter.
M7 — AgriCraft 1.5.2-evernife-v1 adapter.
M8 — wider crop ecosystem integrations and tuning.
M9 — selected modern IE machines/features, one gated feature family at a time.
M10 — release candidate, world-upgrade audit, performance audit, documentation.

## Definition of Done for a feature
A feature is APPROVED only when all applicable checks pass:
- source provenance recorded;
- source compiles in clean environment;
- reobfuscated production JAR builds;
- client starts;
- dedicated server starts;
- existing baseline world loads;
- new world creates;
- registry/ID delta reviewed;
- feature functional tests pass;
- optional-mod absence test passes where relevant;
- NEI integration does not crash;
- Angelica rendering smoke test passes where relevant;
- no known item/fluid/energy duplication introduced;
- no uncontrolled worldgen introduced;
- configuration defaults documented;
- changelog + decision record updated.

## Explicitly deferred
- Public redistribution/release packaging until licensing/permission requirements are resolved.
- Blind wholesale merge of modern IE branches.
- Rewriting the wire network solely for aesthetics without a measured correctness/performance benefit.
- Removing legacy compatibility behavior before a replacement has passed parity tests.
