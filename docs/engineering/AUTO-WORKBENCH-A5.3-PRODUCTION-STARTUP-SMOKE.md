# Auto Workbench A5.3 — Production Pack Startup Smoke

Date: 2026-09-16
Status: PASS — startup/classloading only; A6 gameplay still open

## Environment
Production Forbidden Series client mod directory currently contains:
`ImmersiveEngineering-auto-workbench-test-1-r2.jar`

Verified SHA-256:
`C72F7C4AAD54491F83EA6073799DBEAFF44C260B8B4CBDE33249F1351820FA97`

This is byte-identical to the audited R2 distribution candidate.
No file replacement or production mutation was performed by this gate.

## Evidence source
`C:\Users\User\AppData\Roaming\.technic\modpacks\forbidden-series\logs\fml-client-latest.log`

The log was scanned in one streaming pass to avoid loading the multi-million-line file into memory.
## Streaming scan results
Total lines scanned: 8,716,747.

Auto Workbench-related class references:
- `TileRenderAutoWorkbench`: 567
- `TileEntityAutoWorkbench`: 189
- `ContainerAutoWorkbench`: 189
- `GuiAutoWorkbench`: 0

The counts reflect LaunchWrapper/FML classloading and transformer passes; they are not interpreted as gameplay use counts.

Critical parser signature:
- `ModelFormatException: Error parsing`: **0**

Therefore the R2 static OBJ correction survives the real production transformer stack, including the installed Angelica compatibility transformer.
## False-positive separation
Two lines matched the generic substring `AutoWorkbench` together with the word `Exception`:
- `buildcraft.factory.BlockAutoWorkbenchCompat`
- `buildcraft.factory.TileAutoWorkbenchCompat`

Both belong to BuildCraft Factory compatibility loading and are unrelated to the Immersive Engineering Auto Workbench.
No IE Auto Workbench exception line was identified.

## Interpretation
This gate proves startup/classloading/render-model parsing compatibility in the user's real modpack stack.
It does **not** prove:
- multiblock formation;
- GUI recipe selection;
- Blueprint crafting;
- RF consumption/queue timing;
- sided automation/output;
- mirrored structure behavior;
- teardown or save/reload persistence.

Those remain A6 user runtime Test 1 criteria.