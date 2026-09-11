# Forbidden Series Runtime Stack — Capability Matrix

Date: 2026-09-11
Status: INITIAL AUDIT / POLICY
Scope: IE 1.21.1 -> 1.7.10 backport

## Verified pack baseline versions
- UniMixins: `0.3.1`
- Angelica: `2.2.8`
- ArchaicFix: `0.8.0`
- CoreTweaks: `0.3.4.7-GTNH`
- EntityCulling: `1.6.4-mc1.7.10`
- GeckoLib Unofficial: `1.0.4`
- GTNHLib: `0.11.41`
- Hodgepodge: `2.7.189`
- ParallelMipMap: `1.0`

These are runtime facts from the frozen modpack inventory, not dependency declarations for Immersive Engineering.

## Policy summary
| Technology | Primary role | IE hard dependency? | Initial decision |
|---|---|---:|---|
| Angelica | renderer/performance compatibility target | No | REQUIRED TEST TARGET |
| GTNHLib | utilities, state/geometry/render helpers | Not yet | EVALUATE PER ADAPTER |
| Hodgepodge | pack fixes/performance | No | COMPATIBILITY TARGET |
| UniMixins | surgical platform hooks | No | RESTRICTED USE |
| GeckoLib Unofficial | skeletal/keyframe animation | No | OPTIONAL FEATURE TOOL |
| ArchaicFix | engine fixes/performance | No | COMPATIBILITY TARGET |
| CoreTweaks | startup/core optimization | No | COMPATIBILITY TARGET |
| EntityCulling | client culling | No | VISIBILITY REGRESSION TARGET |
| ParallelMipMap | texture pipeline optimization | No | TEXTURE REGRESSION TARGET |
## Angelica
Upstream Angelica targets OpenGL 3.3+ core profile, emulates fixed-function/display-list behavior, converts quads to triangles and includes a modernized model-building stack.

Use it to validate that IE renderers are explicit and deterministic. Do not code directly against Angelica internals unless a separately reviewed compatibility module becomes necessary.

Required tests:
- formed multiblocks normal/mirrored;
- translucent and emissive surfaces;
- dynamic item rendering;
- chunk reload and dimension change;
- shader-off baseline first, shader-on tests later;
- GL state restoration after every custom renderer.

## GTNHLib
GTNHLib exposes useful 1.7.10-era modernizations including block-state style properties, geometry transforms, render helpers, NBT/JSON utilities, synced keybinds and selected networking helpers.

Potential IE uses:
- common geometry/transform code shared by multiple backports;
- compact metadata/property adapters;
- renderer batching/capture experiments;
- diagnostics and utility code.

Rule: prefer an IE-local abstraction until reuse or measured performance justifies a GTNHLib API dependency. Preserve the ability to test IE logic independently.
## UniMixins
UniMixins is a compatibility-oriented Mixin loader for 1.7.10 and includes Mixingasm/MixinExtras-style capabilities.

Approved use:
- narrowly scoped hooks into Minecraft/Forge when no stable public hook exists;
- compatibility shims proven necessary by runtime evidence;
- instrumentation that cannot be implemented cleanly inside IE.

Rejected use:
- rewriting IE internals through Mixins when we own the source;
- broad core patches for convenience;
- transformer-order assumptions that couple the mod to one launch environment.

Every mixin requires target/version documentation and a failure-mode test.

## GeckoLib Unofficial 1.7.10
The maintained unofficial port advertises complex 3D keyframe animation, many easing modes, sound/particle/event keyframes, math animation, model/animation hot-swap and server synchronization.

Best-fit IE candidates:
- Robot Wolf/entity animation;
- unusually articulated equipment;
- future machines whose animation is materially simpler as a rig than procedural transforms.

Do not convert rigid multiblocks to GeckoLib by default. First compare CPU cost, allocations, draw path, Angelica behavior and visual parity against an IE-native renderer.
## Hodgepodge
Hodgepodge is treated as a broad patch/performance environment. It must not be used as a substitute for IE correctness.

Relevant regression areas include NBT-copy optimizations, TileEntity removal, packet-size behavior, GL-state fixes and TCP behavior. New IE code must remain valid if those optimizations change implementation.

## ArchaicFix
ArchaicFix provides broad 1.7.10 bugfix/performance work including lighting, memory-leak and data-structure improvements.

IE-specific rule: do not rely on ArchaicFix to mask lifecycle leaks, stale TileEntity references or pathological chunk access. Our own lifecycle must remain bounded and correct.

## CoreTweaks
CoreTweaks mainly improves startup/coremod behavior and transformer caching. It changes the launch environment more than IE gameplay.

IE-specific rule: clean builds and runtime tests must not depend on accidental transformer ordering.

## EntityCulling
EntityCulling can hide entities/block entities judged occluded. Dynamic IE machines, moving items and large multiblock renderers must be checked for false culling/pop-in.

If a whitelist is required, document it as a compatibility requirement rather than silently disabling culling globally.

## ParallelMipMap
Treat as part of the texture-load/mipmap environment. Modernized IE texture packs must be tested for mip generation, alpha edges, atlas correctness and startup memory spikes.
