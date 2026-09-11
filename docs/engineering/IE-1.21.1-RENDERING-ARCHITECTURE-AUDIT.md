# IE 1.21.1 -> 1.7.10 Rendering Architecture Audit

Date: 2026-09-11
Status: A0 SOURCE AUDIT — PASS FOR OBJ/PROCEDURAL STRATEGY
Upstream snapshot: `75a27f03e4243544243567e8d5c38d336f4f10f4`

## Finding
The modern 1.21.1 visual target does not require a universal skeletal-animation engine.

Official IE still represents many industrial machines as Wavefront OBJ geometry plus small dynamic submodels and procedural transforms. This maps unusually well to the renderer strategy already proven by the Auto Workbench backport.

The audited modern resource roots contain 72 OBJ files. Key multiblock OBJ sizes are modest: the largest sampled static machine model, `auto_workbench.obj`, is ~38 KiB with 593 vertices and 338 faces.

## Sampled geometry
| Model | Faces | Face topology | Dynamic companion |
|---|---:|---|---|
| Auto Workbench | 338 | 334 quads + 4 triangles | separate animated model |
| Arc Furnace | 368 | quads | renderer-driven state |
| Diesel Generator | 264 | quads | separate fan OBJ |
| Squeezer | 179 | quads | separate piston OBJ |
| Sawmill | 159 | quads | 110-quad animated OBJ |
| Refinery | 181 | quads | renderer-driven state |
| Assembler | 167 | quads | renderer-driven state |
| Mixer | 118 | quads | separate agitator OBJ |
| Fermenter | 124 | quads | renderer-driven state |
| Metal Press | 136 | quads | separate piston OBJ |
| Radio Tower | 104 | quads | state rendering |
| Crusher | 102 | quads | separate drum models |
## Proven compatibility lesson from Auto Workbench
The official modern Auto Workbench OBJ still contains the same mixed face topology that failed the Forge 1.7 parser path during R1: four triangles among hundreds of quads.

The R2 project already solved this deterministically by converting triangle `A/B/C` to degenerate quad `A/B/C/C` for the static 1.7 render stream, preserving visible geometry and normals.

Therefore modern asset use must always pass a topology audit/conversion stage before packaging. Never assume an upstream OBJ is directly valid for Forge 1.7.

## Rendering lanes
### Lane R1 — static/cacheable machine shell
Use converted OBJ geometry rendered through the most cache-friendly 1.7 path available. Invariant geometry must not be reparsed or rebuilt each frame.

### Lane R2 — small procedural moving submodels
Use separate OBJ groups/models plus explicit transforms for pistons, fans, agitators, drums, sawblades, lifts and presses.

This is the preferred path for Mixer, Sawmill, Metal Press, Squeezer, Crusher and Diesel Generator because the official renderer itself models their motion procedurally.

### Lane R3 — dynamic process items/fluids
Render process items only when visible and derive transforms from synchronized process state. Fluids use bounded geometry and explicit translucent-state restoration.

### Lane R4 — skeletal/keyframe animation
Evaluate GeckoLib only when a feature has true articulated hierarchy/keyframes whose procedural implementation would be materially more complex or less maintainable.
## Texture/material pipeline
Modern MTL files use a small number of material assignments per sampled machine. The conversion pipeline must map each material to explicit 1.7-visible texture groups and preserve source UV intent.

Required asset checks:
- deterministic source hash and converted hash;
- vertex/UV/normal table audit;
- face topology and winding;
- material-to-texture mapping;
- alpha/translucency classification;
- mipmap/atlas behavior in the real pack;
- item/inventory model representation;
- mirrored transform correctness.

Where the modern texture set is legally reusable, preserve its authored resolution and downscale only if measurement proves a material memory/startup problem. Texture resolution is not to be reduced merely because the runtime is 1.7.10.

## Angelica contract
Angelica is the primary render compatibility target.

Custom IE render code must:
- restore GL state it changes;
- avoid dependence on compatibility-profile undefined behavior;
- avoid persistent display-list assumptions unless tested through Angelica's emulation;
- treat quad conversion and vertex ordering explicitly;
- validate translucent ordering and alpha edges;
- never assume a renderer is visible merely because vanilla 1.7 renders it correctly.

EntityCulling is a separate visibility gate: animated/dynamic TileEntity renderers must be tested for false culling and pop-in without disabling culling globally.

ParallelMipMap is a texture-load regression environment; test modern texture sets for alpha edges, mip correctness, atlas construction and startup memory.
## Performance invariants
Renderer design must separate invariant shell geometry from per-frame state.

Per-frame work may include only what actually changes, such as:
- transform matrices for moving machine parts;
- a bounded number of visible process-item renders;
- fluid surface geometry when contents/height are visible;
- blueprint overlays within a deliberate distance gate.

Rejected patterns:
- parsing OBJ/MTL during rendering;
- rebuilding invariant tessellation every frame;
- scanning neighboring TileEntities from a renderer;
- allocating large temporary collections every frame;
- synchronizing animation position through per-tick packets when process state can reconstruct it.

The modern Auto Workbench renderer itself distance-gates blueprint projection. Similar expensive overlays should use visibility/distance gates in the 1.7 adaptation.

## GeckoLib decision
GeckoLib Unofficial remains **APPROVED FOR EVALUATION, NOT DEFAULT USE**.

Current evidence favors native OBJ/procedural rendering for the industrial machine track because:
- official 1.21.1 IE already uses that composition;
- the project has a proven OBJ adaptation pipeline;
- moving parts are usually simple rigid transforms;
- this avoids adding animation-engine overhead and dependency coupling to every machine.

GeckoLib becomes preferred only after an individual feature proves a genuine skeletal/keyframe requirement and passes Angelica/performance tests.

## Approval
A0 visual architecture: **PASS**.

APPROVE modern geometry/textures as first-class targets.
APPROVE deterministic OBJ conversion plus procedural dynamic submodels as the default industrial path.
REJECT flattening modern machines into simplistic legacy cubes solely for compatibility.
REJECT blanket GeckoLib conversion of IE multiblocks.

Next gate: build a machine-by-machine asset manifest and conversion test for Feature #3 after the full gap ranking is frozen.
