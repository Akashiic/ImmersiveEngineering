# Auto Workbench A5.1 — Runtime Smoke Gate

Date: 2026-09-10
Status: PASS AFTER REWORK; original Test 1 tag REJECTED
Scope: client initialization / OBJ parser / renderer registration

## Rejected candidate
The annotated tag `auto-workbench-test-1` remains immutable and points to commit `e2ee445391e7144f5add0996ebe712110cf6cc8a`.
Its final JAR had SHA-256 `94547AA9A48BE6EE0A3E5C6B39F317973AAE14E0B9EC0FF4AA6CAADB1BB8DFAB`.

That artifact passed clean build and static artifact gates, but failed the first isolated client runtime smoke test.
It must not be installed or promoted.

## Runtime failure reproduced
A minimal RFG client was created from the same commit in a disposable Git worktree.
Optional pack dependencies were moved to compile-only only inside that worktree, so unrelated addon failures could not mask IE initialization.

Forge reached `ClientProxy.init()` and failed while constructing `TileRenderAutoWorkbench`:
`ModelFormatException: Invalid number of points for face (expected 4, found 3)`.
The offending face was in `immersiveengineering:models/autoWorkbench.obj`.
## Root cause — proven from Forge 1.7.10 source
`WavefrontObject.parseFace()` supports both triangles and quads, but a `GroupObject` locks its drawing mode when its first face is parsed.
A later face with different arity in the same group is rejected.

The converted static Auto Workbench model contained this exact mixed group:
- `base_workbench`: 289 quads + 4 triangles;
- `conveyors_conveyor`: 36 quads;
- `base_assembler`: 9 quads.

The mirrored model had the same 289+4 mixture in `base_workbench`.
The animated model contains 98 faces and all of them are quads.

A group-only split was rejected as an incomplete fix because `ClientUtils.renderStaticWavefrontModel()` does not render each group with its own `glDrawingMode`.
It emits all vertices into the Tessellator already opened by the IE static-render pipeline, which is a GL_QUADS stream.
## Corrective adaptation
For the two static models only, the deterministic converter now supports `--quadify-triangles`.
Each triangle `f A B C` is rewritten as `f A B C C`.

This is a degenerate quad, not a geometry approximation:
- A/B/C indices remain byte-for-byte identical;
- vertex/UV/normal tables are unchanged;
- the fourth corner duplicates C and adds zero surface area;
- `Face.calculateFaceNormal()` in Forge 1.7.10 uses only vertices 0, 1 and 2, so its calculated normal is unchanged;
- winding of the visible triangle is unchanged.

Independent topology audit requires exactly four such rewrites in each static model and zero triangle faces afterward.
The animated OBJ is not quadified.
## Provenance and corrected hashes
Official source tag: `0.10-54`.

Upstream normal OBJ:
`073452B87B4D32C62137E99E99F2F6A3261B78644439E271C9337CBF3640F12B`

Corrected 1.7 normal OBJ:
`263C39C0861D841A8A593672062DFB8C3A7ACF3F2B3409C18C76B9F290412434`

Upstream mirrored OBJ:
`384E1CC15C94666572B08DC86DD49D0FD1A01D71DA6208F04BE8A0B267B22915`

Corrected 1.7 mirrored OBJ:
`D2DD6FFC9616B253E2C17F995A5844F9C301EA732179B5C59E21DB61D96B80F0`

Both conversions report 338 source faces and exactly 4 quadified triangles.
## Runtime re-test after correction
The corrected normal and mirrored assets were copied into the same disposable minimal-client worktree.
The client then completed IE initialization successfully.

Observed evidence:
- Forge 10.13.4.1614 initialized;
- FML identified exactly 4 minimal-runtime mods;
- both block/item texture atlases were created;
- `Forge Mod Loader has successfully loaded 4 mods`;
- IE resource reload completed;
- revolver/item/block atlases were rebuilt;
- no Auto Workbench `ModelFormatException` or renderer initialization exception occurred.

`TileRenderAutoWorkbench` constructs the normal, mirrored and animated models in its constructor, so reaching successful IE initialization proves all three files parsed in this runtime smoke path.
## Non-regression noise separated from this feature
The minimal client reported three missing IE textures:
- `textures/revolvers/revolver_rose.png`;
- `textures/blocks/metal2_skycrateDispenser.png`;
- `textures/revolvers/revolver_tesla.png`.

All three entries are also absent from the approved `garden-cloche-test-2` JAR.
They are therefore inherited baseline/resource noise and not introduced by the Auto Workbench candidate.

## Dedicated-server limitation
A dedicated RFG smoke run reached the Minecraft EULA prompt in the isolated environment.
The EULA was not accepted automatically on the user's behalf, so this gate does not claim a full dedicated-server runtime start.
Dedicated-side safety remains supported by the bytecode comparison showing zero new `net.minecraft.client.*` references in common/API classes.

## Decision
- `auto-workbench-test-1`: REJECTED and retained as immutable evidence.
- OBJ parser/runtime failure: FIXED and client-smoke validated.
- next candidate: must receive a new commit/tag/hash and repeat clean build + artifact audit before user runtime Test 1.