# Alloy Kiln K4 — Build, Artifact, ABI and Runtime-Smoke Gate

Date: 2026-09-12
Feature branch: `feature/alloy-kiln`
Runtime commit: `a1376474`
Baseline: `auto-workbench-test-1-r2` / `97cea596`

## Decision
**PASS — static/build/artifact/public-ABI/client-initialization smoke.**

**NOT YET APPROVED — real gameplay Test 1 and full dedicated-server runtime.**

This gate authorizes creation of an Alloy Kiln Test 1 candidate only. It does not authorize production/stable promotion.

## Clean build evidence
Final build was executed from the clean runtime commit after correcting physical worktree line endings for protected OBJ assets.

Result:
- `BUILD SUCCESSFUL in 2m 28s`;
- 23 actionable tasks;
- 22 executed, 1 up-to-date;
- `compileJava` PASS;
- `processResources` PASS;
- `reobfJar` PASS;
- `checkstyleMain` PASS;
- `check` PASS;
- `build` PASS.

## Candidate identity
JAR:
`ImmersiveEngineering-auto-workbench-test-1-r2-feature-alloy-kiln.8+a137647457.jar`

Size: `3,871,389 bytes`

SHA-256:
`F03FC1EC9EA4C17DBB38E62D204EAD4853966D570D06E3258B0525DAA5DA8590`

## Artifact audit
Compared with exact R2 baseline:
- 1,446 ZIP entries;
- zero duplicate ZIP entries;
- 41 entries added;
- zero entries removed;
- 13 existing entries changed;
- 766 classes;
- all 766 classes are Java class major 52;
- mod identity remains `1.7.10-0.7.9` / `Forbidden Series Edition`;
- manifest FMLAT remains present;
- expected Alloy Kiln API, multiblock, TileEntity, GUI, NEI and MineTweaker classes are present;
- no client references were introduced into the audited new server-core classes.

## Protected baseline assets
The first artifact audit correctly rejected a candidate because the disposable/worktree checkout had converted the three Auto Workbench OBJ files to CRLF even though the committed blobs were LF.

The committed blobs were independently verified to equal the exact R2 hashes. The physical checkout was normalized to LF and the clean build was repeated.

Final candidate preserves byte-identically:
- `autoWorkbench.obj` — `263C39C0861D841A8A593672062DFB8C3A7ACF3F2B3409C18C76B9F290412434`;
- `autoWorkbenchMirrored.obj` — `D2DD6FFC9616B253E2C17F995A5844F9C301EA732179B5C59E21DB61D96B80F0`;
- `autoWorkbenchAnimated.obj` — `68B20FBDEF15804F488621C9CBFE6CE2BFF5922D2726E8464E6A48F5A6705C09`.

## Asset provenance gate
`ALLOY-KILN-ASSET-MANIFEST.json` contains 23 generated/copied outputs from the frozen IE 1.21.1 source commit.

Every asset inside the final JAR matches its manifest SHA-256.

This includes registry-preserving replacement of the legacy Insulating Glass texture with the modern 16x16 RGBA asset; block identity/meta are unchanged.

## Registry and recipe invariants
- Garden Cloche GUI ID remains 13;
- Auto Workbench GUI ID remains 14;
- Alloy Smelter GUI ID is new ID 15;
- existing metadata families were not remapped;
- new content uses `stoneDecoration2` and `stoneDevice2`;
- `TileEntityAlloySmelter` is registered with a new TileEntity identifier;
- Mixer/other future multiblocks do not consume the legacy `metalMultiblock` meta 15 in this feature;
- modern Insulating Glass route is present;
- Bronze and Invar sized-input routes are present;
- modern Manyullyn recipe remains intentionally absent because Netherite Scrap has no proven 1.7.10 semantic equivalent.

## Public ABI audit
Seven pre-existing top-level classes changed bytecode versus R2.

`javap -public` comparison found:
- zero removed public members;
- zero signature regressions;
- only expected additive fields/members in `IEContent` and `Lib`.

Result: `ALLOY_KILN_PUBLIC_ABI_PASS`.

## Isolated client runtime smoke
A disposable detached worktree was created from `a1376474` and all optional pack integrations were changed to compile-only for the smoke environment only.

Observed runtime evidence:
- Forge 10.13.4.1614 initialized;
- FML identified exactly 4 mods;
- Immersive Engineering pre-initialized and initialized;
- `Forge Mod Loader has successfully loaded 4 mods`;
- block, item and revolver atlases were created;
- IE resources were reloaded successfully;
- no Alloy Kiln exception, ModelFormatException or client classloading failure occurred.

Client smoke log SHA-256:
`0C93603B8A91E892742827C5CF820B1A86DF518031279046E294EEFD14104501`

Environmental/baseline noise observed:
- missing initial `config/splash.properties` before FML created the config directory;
- Forge legacy version-check JSON error;
- the same three inherited missing IE textures previously proven in R2: revolver rose, skycrate dispenser and revolver tesla.

None are introduced by Alloy Kiln.

## Dedicated-server smoke limitation
The disposable RFG server harness reached its online-mode question and was set to offline only inside the smoke worktree.

It then asked for Minecraft EULA acceptance. The EULA was explicitly **not accepted** and the harness exited with `Minecraft EULA not accepted`.

Therefore this gate does **not** claim a full dedicated-server runtime PASS.
Dedicated-side safety is currently supported by:
- successful clean compilation/reobfuscation;
- selected server-core bytecode containing zero client references;
- no new common-side client-boundary regression in artifact audit.

## Promotion status
K4 static/artifact/client-smoke is complete.

Next gate is **K5 / Alloy Kiln Test 1 in the real Forbidden Series runtime**, including formation, processing, persistence, visuals, NEI, MineTweaker, Angelica and performance.

Until K5 passes:
**do not promote this JAR to production/stable.**

## Immutable candidate tag
Annotated tag: `alloy-kiln-test-1`

The tag resolves to runtime commit:
`a137647457513e219661af3fa98395309a42a58b`

Verification/documentation commits after this point must not move the tag or redefine the candidate artifact.
