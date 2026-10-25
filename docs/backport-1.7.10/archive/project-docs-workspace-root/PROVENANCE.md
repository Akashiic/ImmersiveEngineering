# Source Provenance — Immersive Engineering 1.7.10 / Forbidden Series

Date: 2026-09-09
Status: VERIFIED ANCESTRY; exact public Forbidden 0.7.9 repository not located

## 1. Deployed artifact

Path in production pack:
`mods/ImmersiveEngineering-1.7.10-0.7.9.jar`

Evidence:
- SHA-256: `f68b4bef165414ae751d00ef2e1eb2a9692b196239e45193a6bc82d37bb92fb8`
- `mcmod.info` version: `1.7.10-0.7.9`
- description: `Forbidden Series Edition`
- bytecode major: 52 (Java 8)
- archive timestamps cluster around 2023-09-12, proving a post-2016 rebuild rather than the untouched official 0.7.7 binary.

## 2. Official 1.7.10 source baseline

Repository: `BluSunrize/ImmersiveEngineering`
Branch: `1.7.10`
Commit: `c45a3a2577a4751afdc78e3109f2358b10b0f397`
Commit message: `Build and Release v0.7.7`
Date: 2016-05-09

Important: the remote tag named `0.7.7` is on a divergent history and is not an ancestor of branch HEAD. For this recovery effort the branch HEAD above, not the divergent tag object, is the canonical official source-line baseline.

## 3. chaos234 continuation

Repository: `chaos234/ImmersiveEngineering`
Branch: `1.7.10`
Observed HEAD: `3cf2a38831d5443e9b5a90c158c0d293c0db31da`

Evidence linking it to the deployed binary:
- deployed `ImmersiveEngineering.class`, after CFR decompilation, changes the contributor revolvers URL from BluSunrize to:
  `https://raw.githubusercontent.com/chaos234/ImmersiveEngineering/master/contributorRevolvers.json`
- chaos234 history contains the 0.7.8-era continuation and fixes subsequently inherited by Bogdan-G.

Conclusion: chaos234 is a proven ancestor, but not the final source because its HEAD lacks the distinctive deployed energy-splitting implementation.

## 4. Bogdan-G continuation

Repository: `Bogdan-G/ImmersiveEngineering`
Branch: `1.7.10`
Commit: `798ce21a1dda0bba6be0a3931c2d0e26b495673c`
Date: 2017-04-27/28 depending on timezone representation.

This branch is a direct descendant of the official 1.7.10 line and incorporates the chaos234-era continuation.

Three independent fingerprints connect it to the deployed Forbidden binary:

### Fingerprint A — energy distribution
Bogdan-G contains the `TileEntityConnectorLV` algorithm using:
- `powerSortingMax`
- proportional priority calculation
- `outMap`
- redistribution of remaining RF using an iterator

The deployed 0.7.9 decompilation contains the same distinctive logic.

### Fingerprint B — XSTR
Bogdan-G HEAD adds:
`org/bogdang/modifications/random/XSTR.java`

The deployed 0.7.9 JAR contains:
`org/bogdang/modifications/random/XSTR.class`

### Fingerprint C — render/float optimization family
Bogdan-G HEAD message explicitly records conversion of GL/double/random paths to float and addition of XSTR. The deployed decompiled delta contains the same family across Matrix4 and renderer classes.

## 5. Forbidden overlay

The deployed artifact is not a byte-for-byte build of the public Bogdan-G HEAD.

Observed overlay behavior includes:
- version metadata changed to `0.7.9`;
- description changed to `Forbidden Series Edition`;
- Java 8 recompilation;
- embedded `com.jadarstudios.developercapes.*` classes;
- live `ClientProxy` call to `DevCapes.getInstance().registerConfig(...)` with a remote JSON URL.

The DeveloperCapes subsystem is not part of core IE functionality and introduces an external network dependency. It is preserved as historical evidence, but is not automatically approved for the hardened continuation.

## 6. Binary/source normalization result

Comparing the deployed 0.7.9 JAR against a local official 0.7.7 JAR:
- ~678 class files differ at the binary level because of compiler/reobfuscation differences;
- after CFR decompilation + normalization, only 42 shared Java files differ and 14 Java classes are added;
- most added classes are DeveloperCapes; one is Bogdan-G XSTR.

This proves that raw class SHA comparison dramatically overstates the actual semantic delta.

## 7. Accepted recovery chain

```text
BluSunrize 1.7.10 @ c45a3a2 (official 0.7.7 branch HEAD)
        |
        v
chaos234 continuation (0.7.8-era fixes)
        |
        v
Bogdan-G 1.7.10 @ 798ce21 (energy + world/wire + crash + XSTR/render fixes)
        |
        v
Forbidden Series 0.7.9 overlay/rebuild (recovered from deployed binary)
```

Engineering status:
- official source: VERIFIED
- chaos234 ancestry: VERIFIED
- Bogdan-G ancestry: VERIFIED with multiple independent fingerprints
- literal public source repository for `Forbidden Series Edition 0.7.9`: NOT LOCATED
- recoverability of core runtime: HIGH; final claim of parity requires a clean modern build followed by semantic/runtime comparison.
