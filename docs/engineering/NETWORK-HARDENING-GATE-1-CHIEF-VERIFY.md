# Network Hardening Gate 1 — Chief Verification

Date: 2026-09-11
Branch: `hardening/network-gate-chief`
Source checkpoint: `a1b811a6`
Baseline: `auto-workbench-test-1-r2` / `97cea596`
Status: STATIC / CLEAN BUILD / ARTIFACT / ABI PASS; RUNTIME PENDING

## Isolation
This verification was performed in a clean worktree created from commit `a1b811a6` after the original `network-hardening` worktree gained unrelated concurrent edits.
Those concurrent edits were not included here.

## Clean build
`gradlew clean build --no-daemon`
- BUILD SUCCESSFUL;
- 23 actionable tasks, 23 executed;
- compileJava PASS;
- reobfJar PASS;
- checkstyleMain PASS;
- check PASS;
- build PASS.

## Final artifact
`ImmersiveEngineering-auto-workbench-test-1-r2-hardening-network-gate-chief.5+a1b811a694.jar`
SHA-256: `1D16936EB40ECE71DCF7CD61AC292FC13DF52F190F0177BFAC4E96A4225937E0`

## Artifact audit
Compared against the exact R2 baseline:
- ZIP test: PASS;
- entries: 1,410;
- duplicate entries: 0;
- added entries: 5;
- removed entries: 0;
- Java class major set: `{52}` only;
- FMLAT preserved;
- mod identity/version preserved;
- no newly introduced client references in changed common-side classes;
- Auto Workbench normal/mirrored/animated OBJ bytes are identical to R2.

Added classes are limited to the server-owned network queue and handler closure generated for the hardened C2S path.

## Public ABI audit
`javap -public` baseline signatures were required to remain a subset of the candidate for:
- TileEntityIEBase;
- TileEntityAssembler;
- TileEntityAutoWorkbench;
- TileEntityConveyorSorter;
- ContainerAssembler;
- ContainerSorter;
- MessageTileSync.

Missing baseline public members: **0**.

New public members are additive only:
- authenticated `receiveMessageFromClient(EntityPlayer, NBTTagCompound)` overloads;
- container `getTile()` accessors used by server authorization.

## Approval boundary
**APPROVED for static integration review.**

Not approved for production/runtime promotion until these tests pass in a real dedicated environment:
- legitimate Sorter GUI changes;
- Assembler GUI reset + NEI transfer;
- Auto Workbench recipe selection;
- rapid-click quota behavior;
- close-container and reconnect races;
- malformed/oversized payload rejection;
- no world mutation from Netty threads;
- no false rejection for legitimate latency.

The frozen Auto Workbench R2 remains the gameplay candidate until this hardening receives its own runtime evidence.