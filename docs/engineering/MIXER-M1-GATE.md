# Mixer M1 — Fluid / Recipe Foundation Gate

Date: 2026-09-12
Runtime commit under test: `f7a4abd4`
Status: **PASS**

## Scope
M1 establishes reusable infrastructure before the Mixer multiblock exists:
- `MultiFluidTank`: ordered, capacity-bounded multi-fluid storage;
- `MixerRecipe`: fluid + item recipe contract;
- exact item-consumption planning across multiple slots;
- recipe-list revision tracking for cache invalidation;
- direct ItemStack matching without unnecessary OreDictionary initialization.

This gate does **not** approve Mixer gameplay, new fluids, rendering, sound, GUI or automation.
Those begin at M2+.

## Build gate
From a clean tree at `f7a4abd4`:
- `clean build`: PASS;
- 26 tasks: 25 executed, 1 up-to-date;
- `compileJava`: PASS;
- `reobfJar`: PASS;
- `compileTestJava`: PASS;
- `test`: PASS;
- `checkstyleMain` / `checkstyleTest`: PASS;
- `check` / `build`: PASS.

## Pure algorithm self-test
The tracked test harness runs through a JavaExec task without FML world bootstrap.
Result: `MIXER_M1_SELF_TEST_PASS`.

It proves:
- an overlapping-ingredient case rejected by the old greedy planner is solved exactly;
- one ingredient quantity can be split across multiple slots;
- insufficient total quantity is rejected;
- fluid-only recipes produce a valid zero-consumption plan;
- recipe revision advances on add and `iterator.remove()`.

## Artifact gate
Baseline: `ImmersiveEngineering-alloy-kiln-test-1.jar`
- SHA-256: `F03FC1EC9EA4C17DBB38E62D204EAD4853966D570D06E3258B0525DAA5DA8590`

M1 build artifact:
- SHA-256: `31576B492E563FFF62F1B0D27FD9E6086753E0FBBDB5ACC65B2281BAE7F8A623`
- archive entries: 1,451;
- added: 5;
- removed: 0;
- changed: 44;
- class major: 770/770 at Java 8 / major 52;
- new common/API client references: 0;
- Auto Workbench protected OBJ assets: byte-identical.

Of the 44 changed entries, 43 are legacy OBJ resources whose only difference is
CRLF in the older Alloy Kiln worktree versus canonical LF in the fresh Mixer worktree.
The auditor requires normalized bytes to be identical and rejects any geometry change.
The sole semantic class delta from the baseline is `IngredientStack.class`.

## FML runtime probe
A detached disposable worktree at `f7a4abd4` converted optional integrations to
compile-only and injected a temporary pre-init probe. The candidate source branch was not modified.

Observed under Forge/FML 1.7.10:
- exactly 4 minimal mods identified;
- `MIXER_M1_RUNTIME_PROBE_PASS amount=875 types=3`;
- Forge Mod Loader successfully loaded 4 mods;
- `MultiFluidTank` fill simulation did not mutate state;
- capacity clamping was correct;
- same-fluid stacks with different NBT remained distinct;
- drain simulation did not mutate state;
- NBT save/load preserved amount, type count and variant tag.

Evidence:
`evidence/mixer-m1-runtime-probe/fml-client-latest.log`
SHA-256: `D3DB65BC471CB1F6A9DB702210A9EB8BF87A8A822CCD1407F76568386A856BDC`

## Inherited runtime noise
The minimal client again reports only the three previously proven missing resources:
- `textures/revolvers/revolver_rose.png`;
- `textures/blocks/metal2_skycrateDispenser.png`;
- `textures/revolvers/revolver_tesla.png`.

They predate Mixer M1 and are not a gate failure for this feature.

## Decision
**M1 PASS.**

Approved to begin M2: modern fluid identities, render assets and Concrete lifecycle.
No Mixer multiblock registration is permitted until the M2 fluid foundation passes its own gate.
