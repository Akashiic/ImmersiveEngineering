# Immersive Engineering 1.7.10 — 1.21.1 backport dossier (Forbidden Series)

*Portuguese is the main language of this dossier: see [README.pt-BR.md](README.pt-BR.md). This page is a short English summary.*

> **Most recent state of work: `auto-workbench-test-1-r3` (R3)** — tag at commit `5d467a61`, 2026-09-17 (UTC-3). It is the build the author is running.
> Build, artifact audit and minimal client smoke **PASS**; the real gameplay gate (**A6**, matrix T0–T18) has **not** been executed.
> **Heads-up:** R3 lives on its own line. Alloy Kiln, Mixer, network hardening and Garden Cloche multiplayer hardening are on *other* branches, which still carry the **rejected** Auto Workbench R2. No existing jar contains everything.

## What this is

A backport of Immersive Engineering 1.21.1 features onto Minecraft/Forge 1.7.10 (Forge 10.13.4.1614, RetroFuturaGradle, MCP stable_12, Java 8 bytecode), starting from the recovered **Forbidden Series 0.7.9** lineage (BluSunrize → chaos234 → Bogdan-G → Forbidden overlay). Work was done 2026-09-09 → 2026-09-17.

## Version progression

| Candidate | Date | Status |
|---|---|---|
| Forbidden 0.7.9 baseline on RFG (`0c803472`) | 09 Sep | accepted |
| Garden Cloche Test 2 (`ad5e6201`) | 09 Sep | **user-approved in game** |
| Auto Workbench Test 1 (`e2ee4453`) | 10 Sep | rejected (OBJ mixed triangles/quads) |
| Auto Workbench R2 (`97cea596`) | 10 Sep | rejected 17 Sep (missing `markDirty`, GUI RF sync) |
| Alloy Kiln Test 1 (`a1376474`) | 12 Sep | K4 PASS, gameplay pending |
| Garden Cloche multiplayer Test 1 (`ac69028c`) | 16 Sep | static PASS, multiplayer pending |
| **Auto Workbench R3 (`5d467a61`)** | **17 Sep** | **latest · A5.6 PASS · A6 pending** |

## Map

[CONTEXTO.md](CONTEXTO.md) (context) · [CRONOLOGIA.md](CRONOLOGIA.md) (chronology, all 45 commits, 8 tags) · [FLUXOGRAMAS.md](FLUXOGRAMAS.md) (12 Mermaid diagrams, sources in [`diagrams/`](diagrams/)) · [ESTADO-ATUAL-E-RETOMADA.md](ESTADO-ATUAL-E-RETOMADA.md) (resume guide) · [BUILD-EM-USO.md](BUILD-EM-USO.md) (what the R3 jar is) · [ARTEFATOS.md](ARTEFATOS.md) (hashes) · [HIPOTESES-E-INCIDENTES.md](HIPOTESES-E-INCIDENTES.md) · [PROVENIENCIA.md](PROVENIENCIA.md) · [`data/`](data/) · [`archive/`](archive/INDEX.md) (original docs, byte-for-byte).

## To resume

Install exactly one jar, `ImmersiveEngineering-auto-workbench-test-1-r3.jar` (SHA-256 `B91A12EBC1D424915CB8E3DE5F3267D5699FA564BA605FAFDD9635E61CFB0A12`), back up the world, run `python tools/collect_auto_workbench_a6_evidence.py --start`, play T0–T18 from `docs/AUTO_WORKBENCH_TEST_1.md`, then `--collect`. Details (including the hard-coded author paths in the collector) are in the resume guide.

## Licence note

No `LICENSE` ships with the 1.7.10 tree; Blu's License of Common Sense allows forks but forbids redistributing compiled binaries without permission. **This repository does not publish jars** — only source, history, reports and SHA-256 hashes.

## Honesty note

Nothing here was re-built or re-tested in game when the dossier was assembled (October 2026). The R3 jar was checked against the documented candidate, and patch applicability across the diverged lines was dry-run (no compilation). Everything else is as reported by the original gate documents.
