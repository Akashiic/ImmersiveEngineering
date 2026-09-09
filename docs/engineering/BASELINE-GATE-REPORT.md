# Baseline Gate Report — Forbidden IE 0.7.9 Recovery

Date: 2026-09-09
Gate: G0/G1 — provenance, reproducible build, parity audit
Result: PASS WITH DOCUMENTED COMPILER-EQUIVALENT DELTAS

## Production reference

Artifact: `ImmersiveEngineering-1.7.10-0.7.9.jar`
SHA-256: `F68B4BEF165414AE751D00EF2E1EB2A9692B196239E45193A6BC82D37BB92FB8`
Class count: 694
Java class major: 52
Metadata: `Forbidden Series Edition`, `1.7.10-0.7.9`

## Recovered build

Toolchain: RetroFuturaGradle / Gradle 8.2.1
Minecraft: 1.7.10
Forge: 10.13.4.1614
MCP: stable_12
Target bytecode: Java 8 / class major 52

Full gate command: `gradlew clean build --no-daemon`
Result: `BUILD SUCCESSFUL`
Tasks include compile, jar, reobfJar, checkstyle, check and build.

## Structural parity

Recovered class count: 694
Production-only classes: 0
Recovered-only classes: 0
Exact `.class` byte matches: 551
Different `.class` bytes: 143

The differing class bytes are expected because the recovered build does not reproduce the unknown 2023 javac/reobfuscation environment byte-for-byte.
Therefore semantic comparison was performed using CFR 0.152 on both reobfuscated artifacts.

## CFR semantic parity

Java files identical after decompilation: 463
Java files modified: 32
Production-only Java files: 0
Recovered-only Java files: 0

The 32 remaining Java deltas are classified as:
- 19 declaration/order-only differences in nested classes;
- 7 NEI signatures changed from raw `GuiRecipe` to `GuiRecipe<?>`;
- 2 javac/autoboxing reconstruction differences in drill animation counters;
- 3 reflection-call casts where `(Object) tile` and `tile` have identical invocation semantics;
- 1 equivalent cast/parenthesization reconstruction in Arc Furnace slot selection.

For all seven NEI handlers, `javap -s` confirms the JVM method descriptors are identical between production and recovered builds. The wildcard exists only in the generic Signature attribute/source representation.

## Forbidden overlay recovery

The 13 DeveloperCapes classes are present and no longer differ in the CFR file set.
The production `ClientProxy` cape hook is reproduced, including the `thorfusion` guard and remote cape JSON registration.
DeveloperCapes source is MIT-licensed and retained with source headers plus `THIRD_PARTY_NOTICES.md`.

## Gate decision

The recovered tree is accepted as the engineering baseline for new work.
It is not claimed to be byte-for-byte reproducible with the unknown 2023 build environment.
It is accepted as source/behavior parity at the level supported by decompiled semantic comparison, binary API descriptors, class-set equality and successful modern reobfuscated build.
