# Crop Integration Audit — Exact Forbidden Series Binaries

Date: 2026-09-09
Status: evidence captured from installed JAR bytecode/signatures

## Exact targets

- `AgriCraft-1.5.2-evernife-v1.jar`
- `magicalcrops-4.0.0_PUBLIC_BETA_5.jar`
- `ImmersiveEngineering-1.7.10-0.7.9.jar`

The architecture below is based on these exact installed artifacts, not assumptions from newer versions.

## AgriCraft API facts verified

The installed AgriCraft includes API generations v1/v2/v3 and exposes seed genetics through `ISeedStats`.

Verified seed stat accessors:
- `getGrowth()`
- `getGain()`
- `getStrength()`

Verified seed NBT keys used internally:
- `growth`
- `gain`
- `strength`
- `analyzed`

The implementation clamps stats to `ConfigurationHandler.cropStatCap` and defaults zero values to 1.

API access exists through `com.InfinityRaider.AgriCraft.api.API.getAPI(int)`.

The v2 API exposes operations including:
- seed validity / crop recognition;
- `getSeedStats(ItemStack)`;
- `getCropPlant(ItemStack)`;
- growth requirement access.

## AgriCraft plant abstraction facts verified

`ICropPlant`/internal `CropPlant` provides crop semantics independent of the original mod implementation.

Important methods available in this exact binary include:
- `getSeed()`
- `getBlock()`
- `getAllFruits()`
- `getRandomFruit(Random)`
- `getFruitsOnHarvest(int gain, Random)`
- `getTier()` / `getGrowthRate()`
- `getGrowthRequirement()`
- maturity/render helpers

`CropPlantGeneric.getFruitsOnHarvest(gain, random)` computes a number of random fruits equivalent to `ceil(gain / 3.0)`.

This makes AgriCraft's own crop abstraction a high-value integration source for a Cloche when a seed is recognized by AgriCraft.

## Existing AgriCraft integrations verified

The installed AgriCraft already ships compatibility handlers for many mods present in Forbidden Series, including:
- Immersive Engineering Hemp (`CropPlantHemp`)
- Magical Crops (`CropPlantMagicalCropsBeta`, `CropPlantMagicalCropsV4`)
- HarvestCraft
- Forestry
- GardenStuff
- Natura
- Botania
- numerous others

This is architecturally important: we should not duplicate all of those compatibility tables inside IE when AgriCraft can already provide the normalized crop definition.

## Exact Magical Crops detection inside AgriCraft

`MagicalCropsHelper` inspects the Magical Crops mod version. If the version does not contain `ALPHA`, it selects its V4 pathway.

For the installed PUBLIC_BETA_5 structure, the V4 pathway references:
- `com.mark719.magicalcrops.handlers.MSeeds`
- `com.mark719.magicalcrops.handlers.ModCompat`
- `com.mark719.magicalcrops.blocks.BlockMagicalCrops`
- protected crop product method `func_149865_P`

The installed Magical Crops JAR contains the V4 package structure above, so this compatibility pathway matches the installed binary family.

## Magical Crops BETA 5 facts verified directly

Base class:
`com.mark719.magicalcrops.blocks.BlockMagicalCrops`

Properties:
- extends `BlockBush`;
- implements `IGrowable`;
- maturity metadata is 7;
- normal random-tick growth increments metadata until 7;
- valid growth requires crop-like soil behavior;
- magical fertilizer can advance the crop directly to metadata 7 in its specific path.

Each concrete crop overrides seed/product methods. Example `IronCrop`:
- seed -> `MSeeds.IronSeeds`
- product -> `Essence.IronEssence`

`MSeeds` creates normal Minecraft `ItemSeeds` subclasses that point at their crop block and farmland.

The base `getDrops` path at maturity can add extra seeds based on `ConfigMain.SECOND_SEED_CHANCE` and fortune configuration.

## Duplication rule for Cloche

The seed occupying the Cloche seed slot is a machine input/template and must not also be emitted as the normal crop replant seed by default. This follows a known bug class in later official IE Cloche history where Mystical Agriculture/seed-producing crops could replicate seeds indefinitely.

For a direct Magical Crops adapter:
- output the crop product/essence;
- do not emit the retained template seed as a routine harvest output;
- optional secondary-seed behavior, if desired by pack design, must be explicit, deterministic/configurable, and independently balance-audited rather than inherited accidentally from world-block `getDrops`.

## Proposed adapter precedence

```text
Seed inserted
   |
   v
Is AgriCraft loaded and does API recognize this seed?
   | yes
   v
AgriCraftAdapter
  - read Growth/Gain/Strength
  - obtain ICropPlant
  - obtain fruits through plant abstraction
  - preserve NBT
   |
   +--> includes AgriCraft-managed Magical Crops / IE Hemp / HC / etc.

If not recognized:
   |
   v
Direct known adapter registry
  - IE Hemp
  - Magical Crops BETA 5
  - Vanilla/Forge IPlantable patterns
  - explicit pack integrations
```

Why this order:
1. preserves AgriCraft genetics when present;
2. reuses the pack's already-tested crop normalization;
3. avoids two competing harvest calculations for the same genetically enhanced seed;
4. still supports Magical Crops when AgriCraft compatibility is disabled or the seed is not wrapped/recognized.

## Genetic behavior target

- Growth: affects cycle duration only after formula is mapped to AgriCraft's exact growth semantics and balance tested.
- Gain: use the plant's own `getFruitsOnHarvest(gain, Random)` where possible.
- Strength: must not be invented as a production multiplier without evidence. In field farming it primarily participates in crop resilience/spread mechanics; Cloche use is DEFERRED until a justified machine semantic is designed.
- No mutation or genetic improvement occurs in the Cloche.
- Input seed NBT remains unchanged across production cycles.

## Remaining prerequisite before implementation

- map AgriCraft `getGrowth()` to actual tick-growth probability/formula from `TileEntityCrop` so the Cloche speed modifier is behaviorally grounded;
- inspect API concrete class returned for v2/v3 and lifecycle timing to ensure calls occur only after AgriCraft plant registration has completed;
- verify configuration can disable Magical Crops integration and detect that state cleanly;
- define server-safe rendering bridge that does not load AgriCraft client renderer classes from common/server code.
