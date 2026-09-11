from pathlib import Path
import collections, hashlib, struct, zipfile

root = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport")
repo = root / "work" / "network-hardening"
baseline = root / "dist" / "auto-workbench-test-1-r2" / "ImmersiveEngineering-auto-workbench-test-1-r2.jar"
candidates = [p for p in (repo / "build" / "libs").glob("*.jar")
              if not p.name.endswith("-dev.jar") and not p.name.endswith("-sources.jar")]
assert len(candidates) == 1, candidates
candidate = candidates[0]

print("BASELINE", baseline.name, baseline.stat().st_size,
      hashlib.sha256(baseline.read_bytes()).hexdigest().upper())
print("CANDIDATE", candidate.name, candidate.stat().st_size,
      hashlib.sha256(candidate.read_bytes()).hexdigest().upper())

with zipfile.ZipFile(baseline) as zb, zipfile.ZipFile(candidate) as zc:
    base_names = zb.namelist()
    cand_names = zc.namelist()
    print("ZIP_TEST", zc.testzip())
    print("ENTRIES", len(cand_names), "UNIQUE", len(set(cand_names)),
          "DUPLICATES", len(cand_names) - len(set(cand_names)))
    added = sorted(set(cand_names) - set(base_names))
    removed = sorted(set(base_names) - set(cand_names))
    common = sorted(set(base_names) & set(cand_names))
    changed = sorted(n for n in common if zb.read(n) != zc.read(n))
    print("ADDED", len(added), added)
    print("REMOVED", len(removed), removed)
    print("CHANGED", len(changed))
    for name in changed:
        print("  CHANGED", name)

    majors = collections.Counter()
    bad_majors = []
    for name in cand_names:
        if not name.endswith(".class"):
            continue
        data = zc.read(name)
        assert data[:4] == b"\xca\xfe\xba\xbe", name
        major = struct.unpack(">H", data[6:8])[0]
        majors[major] += 1
        if major > 52:
            bad_majors.append((name, major))
    print("CLASS_MAJORS", dict(sorted(majors.items())))
    print("CLASS_MAJOR_GT52", bad_majors)

    resource_changes = [n for n in added + removed + changed
                        if not n.endswith(".class") and not n.startswith("META-INF/")]
    print("NONCLASS_RUNTIME_DELTA", resource_changes)
    expected_new = {
        "blusunrize/immersiveengineering/common/util/network/ServerNetworkTaskQueue.class",
    }
    print("EXPECTED_NEW_PRESENT", expected_new.issubset(set(cand_names)))

    common_prefix = "blusunrize/immersiveengineering/common/"
    forbidden = [b"net/minecraft/client/", b"blusunrize/immersiveengineering/client/"]
    new_client_refs = []
    for name in sorted(set(added + changed)):
        if not name.startswith(common_prefix) or not name.endswith(".class"):
            continue
        data = zc.read(name)
        for needle in forbidden:
            if needle in data:
                new_client_refs.append((name, needle.decode()))
    print("TOUCHED_COMMON_CLIENT_REFS", new_client_refs)

    manifest = zc.read("META-INF/MANIFEST.MF").decode("utf-8", "replace")
    mcmod = zc.read("mcmod.info").decode("utf-8", "replace")
    print("FMLAT_OK", "FMLAT: ImmersiveEngineering_at.cfg" in manifest)
    print("IDENTITY_OK", "1.7.10-0.7.9" in mcmod and "Forbidden Series Edition" in mcmod)

with zipfile.ZipFile(baseline) as zb, zipfile.ZipFile(candidate) as zc:
    source_models = repo / "src" / "main" / "resources" / "assets" / "immersiveengineering" / "models"
    for model in ["autoWorkbench.obj", "autoWorkbenchMirrored.obj", "autoWorkbenchAnimated.obj"]:
        entry = "assets/immersiveengineering/models/" + model
        src = (source_models / model).read_bytes()
        base = zb.read(entry)
        cand = zc.read(entry)
        print("MODEL_HASH", model,
              "SRC", hashlib.sha256(src).hexdigest().upper(),
              "BASE", hashlib.sha256(base).hexdigest().upper(),
              "CAND", hashlib.sha256(cand).hexdigest().upper(),
              "SRC_EQ_CAND", src == cand,
              "SRC_EQ_BASE", src == base)

    touched = sorted(n for n in set(zb.namelist()) & set(zc.namelist())
                     if n.endswith(".class") and zb.read(n) != zc.read(n))
    introduced_client_refs = []
    for name in touched:
        before, after = zb.read(name), zc.read(name)
        for needle in forbidden:
            if needle not in before and needle in after:
                introduced_client_refs.append((name, needle.decode()))
    print("INTRODUCED_COMMON_CLIENT_REFS", introduced_client_refs)
