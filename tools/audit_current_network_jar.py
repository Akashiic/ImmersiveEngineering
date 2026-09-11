from pathlib import Path
import hashlib, struct, zipfile

repo = Path(__file__).resolve().parents[1]
root = repo.parents[1]
baseline = root / "dist" / "auto-workbench-test-1-r2" / "ImmersiveEngineering-auto-workbench-test-1-r2.jar"
libs = repo / "build" / "libs"
candidates = sorted(p for p in libs.glob("*.jar") if not p.name.endswith("-dev.jar") and not p.name.endswith("-sources.jar"))
assert len(candidates) == 1, candidates
candidate = candidates[0]

print("BASELINE", baseline.name, hashlib.sha256(baseline.read_bytes()).hexdigest().upper())
print("CANDIDATE", candidate.name, hashlib.sha256(candidate.read_bytes()).hexdigest().upper())
with zipfile.ZipFile(baseline) as zb, zipfile.ZipFile(candidate) as zc:
    bn, cn = set(zb.namelist()), set(zc.namelist())
    added, removed = sorted(cn-bn), sorted(bn-cn)
    changed = sorted(n for n in bn & cn if zb.read(n) != zc.read(n))
    print("ZIP_TEST", zc.testzip(), "ENTRIES", len(zc.namelist()), "DUP", len(zc.namelist())-len(cn))
    print("DELTA", "added", len(added), "removed", len(removed), "changed", len(changed))
    majors = {struct.unpack(">H", zc.read(n)[6:8])[0] for n in cn if n.endswith(".class")}
    print("CLASS_MAJORS", sorted(majors))
    assert majors == {52}
    forbidden = (b"net/minecraft/client/", b"blusunrize/immersiveengineering/client/")
    introduced = []
    for n in changed:
        if not n.startswith("blusunrize/immersiveengineering/common/") or not n.endswith(".class"):
            continue
        before, after = zb.read(n), zc.read(n)
        for needle in forbidden:
            if needle not in before and needle in after:
                introduced.append((n, needle.decode()))
    print("INTRODUCED_COMMON_CLIENT_REFS", introduced)
    assert not introduced
    manifest = zc.read("META-INF/MANIFEST.MF").decode("utf-8", "replace")
    mcmod = zc.read("mcmod.info").decode("utf-8", "replace")
    print("FMLAT_OK", "FMLAT: ImmersiveEngineering_at.cfg" in manifest)
    print("IDENTITY_OK", "1.7.10-0.7.9" in mcmod and "Forbidden Series Edition" in mcmod)
    for model in ("autoWorkbench.obj", "autoWorkbenchMirrored.obj", "autoWorkbenchAnimated.obj"):
        entry = "assets/immersiveengineering/models/"+model
        print("MODEL_EQUAL", model, zb.read(entry)==zc.read(entry))
        assert zb.read(entry)==zc.read(entry)
    assert not removed
print("AUDIT_PASS")
