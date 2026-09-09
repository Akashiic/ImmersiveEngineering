# Architecture

## 1. Layered design

```text
+--------------------------------------------------------------+
| Feature Backports                                             |
| Garden Cloche -> later selected modern IE systems             |
+--------------------------------------------------------------+
| Crop / Integration Adapter Layer                              |
| Vanilla | IE Hemp | Magical Crops B5 | AgriCraft | optional  |
+--------------------------------------------------------------+
| Legacy Compatibility Facade                                  |
| registry IDs | NBT | energy | fluids | recipes | NEI | save |
+--------------------------------------------------------------+
| Hardened IE 1.7.10 Runtime                                   |
| recovered 0.7.9 semantics + audited 1.7.10 fixes             |
+--------------------------------------------------------------+
| Modern Build / Test Infrastructure                            |
| RetroFuturaGradle | Forge 1614 | MCP stable 12 | JVM 8 out  |
+--------------------------------------------------------------+
```

## 2. Provenance architecture

Canonical ancestry currently established:

```text
BluSunrize/ImmersiveEngineering
  1.7.10 @ c45a3a2577a4751afdc78e3109f2358b10b0f397
  "Build and Release v0.7.7"
          |
          v
chaos234 lineage
  c38fd6b... -> 3cf2a38...
  0.7.8-era fixes / electrode behavior / compat fixes
          |
          v
Bogdan-G/ImmersiveEngineering 1.7.10
  798ce21a1dda0bba6be0a3931c2d0e26b495673c
  + energy split fix
  + force-loaded-world/wire fix
  + potion crash fix
  + XSTR and float/render changes
          |
          v
Forbidden Series deployed 0.7.9
  SHA256 f68b4bef...
  + version/metadata overlay
  + DeveloperCapes hook and embedded classes
  + Java 8 rebuild
```

The last arrow is a reconstructed relationship, not a claim that a public `0.7.9` source repository exists. It must remain labelled as recovered until a byte/semantic parity build confirms it.

## 3. Compatibility facade

Backported features must not reach arbitrary 1.7.10 mod internals directly from their tile entities. Integrations live behind adapters.

Proposed interfaces (names provisional until implementation):

```java
interface IClocheCropAdapter {
    boolean supportsSeed(ItemStack seed);
    ClochePlantProfile resolve(ItemStack seed, ItemStack soil);
    GrowthResult simulateGrowth(ClocheContext context);
    GrowthResult harvest(ClocheContext context);
    void renderPlant(ClocheRenderContext context);
}
```

`ClochePlantProfile` should describe inputs/outputs/growth/render requirements without embedding mod-specific classes into the core Cloche implementation.

Adapters:
- `VanillaCropAdapter`
- `IEHempAdapter`
- `MagicalCropsBeta5Adapter`
- `AgriCraft152Adapter`
- later optional adapters only after their exact installed APIs are inspected.

## 4. Optional-mod classloading rule

Core classes must not directly reference optional mod classes in fields, signatures, static initializers, annotations, or superclass/interface declarations unless Forge optional handling is proven safe for that exact path.

Preferred patterns:
- isolated compat package loaded only after `Loader.isModLoaded()`;
- reflection only when the dependency exposes no stable compile-time API or when binary variants are known;
- compileOnly/deobf dependency when an exact API is stable and absence is classloader-safe;
- explicit capability probing at initialization, with one-time diagnostic logging.

## 5. Registry and save compatibility

1. Freeze all existing block/item IDs and config keys from the deployed 0.7.9 environment.
2. Inventory unused metadata before adding sub-blocks.
3. Prefer new configurable IDs for genuinely new block classes when metadata reuse creates migration ambiguity.
4. Never repurpose an old metadata value that may already exist in world saves.
5. New TileEntities require stable registration names and NBT schema versioning from first implementation.
6. World-data changes require backward readers before writers are changed.

## 6. Energy architecture

The recovered Bogdan/Forbidden energy distribution behavior is compatibility-critical. Modern feature backports should consume the existing IE energy abstractions rather than replacing the entire wire network during the feature phase.

A wire-network rewrite is a separate project gate and must show one of:
- correctness fix that cannot be isolated;
- measurable CPU/memory improvement;
- required semantics for a selected modern feature.

## 7. Rendering architecture

Rendering is split into:
- static block/item assets;
- TESR/dynamic machinery;
- plant rendering inside Cloche;
- wire/catenary rendering.

Angelica compatibility requirements:
- no assumed fixed-function state leakage;
- push/pop and blend/lighting state balanced;
- avoid unnecessary GL double precision calls in legacy paths;
- no OptiFine-only APIs;
- rendering must fail soft if optional enhancement hooks are absent.

Cloche rendering should be separated from crop simulation so server code never loads crop renderer classes.

## 8. Garden Cloche runtime model

Proposed deterministic pipeline:

```text
Insert seed/soil
    |
    v
Resolve adapter + plant profile
    |
    +--> unsupported -> reject with diagnostic reason
    |
    v
Validate power + fertilizer/fluid + output capacity
    |
    v
Advance normalized growth progress
    |
    v
At maturity: adapter computes harvest transaction
    |
    v
Validate complete output transaction
    |
    +--> cannot fit -> pause (never void / partial dupe)
    |
    v
Commit outputs + consume configured resources atomically
    |
    v
Reset/continue growth cycle
```

No adapter may mutate the world to grow a real crop block as its primary production mechanism; simulation must avoid chunk/world side effects.

## 9. AgriCraft integration principle

Exact seed NBT/API semantics will be derived from the installed `AgriCraft-1.5.2-evernife-v1.jar` before implementation. Until that inspection is complete, Growth/Gain/Strength handling is a design target, not an asserted API contract.

Design requirement: a Cloche must never silently upgrade genetic stats. It may use valid seed stats as production modifiers only after the installed implementation is verified.

## 10. Magical Crops integration principle

Target exactly `magicalcrops-4.0.0_PUBLIC_BETA_5.jar`. Drops, seed retention/replant behavior, metadata and crop maturity must be derived from that binary/source version, not from Mystical Agriculture or later Magical Crops versions.

## 11. Failure containment

Each feature family has a config master switch. A failing integration must be disableable without removing the whole mod. Diagnostics should identify:
- adapter id;
- source mod/version detected;
- seed/soil involved;
- rejection reason;
- recipe/profile registration conflict.

No per-tick log spam is permitted.
