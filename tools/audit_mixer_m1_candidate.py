from pathlib import Path
import hashlib
import struct
import zipfile

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport")
BASE = ROOT / "dist/alloy-kiln-test-1/ImmersiveEngineering-alloy-kiln-test-1.jar"
BUILD = ROOT / "work/mixer/build/libs"
CANDIDATES = sorted(
    p for p in BUILD.glob("*.jar")
    if not p.name.endswith("-dev.jar") and not p.name.endswith("-sources.jar")
)
if len(CANDIDATES) != 1:
    raise SystemExit("expected exactly one final Mixer M1 jar, got %d" % len(CANDIDATES))
CAND = CANDIDATES[0]

sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest().upper()
protected = {
    "assets/immersiveengineering/models/autoWorkbench.obj",
    "assets/immersiveengineering/models/autoWorkbenchMirrored.obj",
    "assets/immersiveengineering/models/autoWorkbenchAnimated.obj",
}
expected_added = {
    "blusunrize/immersiveengineering/api/crafting/MixerRecipe.class",
    "blusunrize/immersiveengineering/api/crafting/MixerRecipe$TrackedRecipeList.class",
    "blusunrize/immersiveengineering/common/util/inventory/MultiFluidTank.class",
}
expected_changed_classes = {
    "blusunrize/immersiveengineering/api/crafting/IngredientStack.class",
}

with zipfile.ZipFile(BASE) as zb, zipfile.ZipFile(CAND) as zc:
    base_names = set(zb.namelist())
    cand_names = set(zc.namelist())
    added = sorted(cand_names-base_names)
    removed = sorted(base_names-cand_names)
    changed = sorted(n for n in base_names&cand_names if zb.read(n)!=zc.read(n))

    if removed:
        raise AssertionError("removed entries: %r" % removed)
    for entry in protected:
        if zb.read(entry) != zc.read(entry):
            raise AssertionError("protected asset changed: "+entry)

    changed_classes = {n for n in changed if n.endswith(".class")}
    if changed_classes != expected_changed_classes:
        raise AssertionError("unexpected changed classes: %r" % sorted(changed_classes))
    allowed_added = []
    for entry in added:
        ok = (
            entry == "blusunrize/immersiveengineering/common/util/inventory/"
            or entry.startswith("blusunrize/immersiveengineering/api/crafting/MixerRecipe")
            or entry == "blusunrize/immersiveengineering/common/util/inventory/MultiFluidTank.class"
        )
        if not ok:
            raise AssertionError("unexpected added entry: "+entry)
        allowed_added.append(entry)
    for required in expected_added:
        if required not in cand_names:
            raise AssertionError("required M1 class missing: "+required)

    normalized_objs = []
    for entry in changed:
        if entry.endswith(".class"):
            continue
        if not entry.endswith(".obj"):
            raise AssertionError("unexpected non-class change: "+entry)
        before = zb.read(entry)
        after = zc.read(entry)
        if before.replace(b"\r\n", b"\n") != after.replace(b"\r\n", b"\n"):
            raise AssertionError("OBJ semantic bytes changed: "+entry)
        if b"\r\n" in after:
            raise AssertionError("candidate OBJ is not canonical LF: "+entry)
        normalized_objs.append(entry)
    class_majors = {}
    client_refs = []
    for entry in cand_names:
        if not entry.endswith(".class"):
            continue
        data = zc.read(entry)
        if data[:4] != b"\xca\xfe\xba\xbe":
            raise AssertionError("bad class magic: "+entry)
        major = struct.unpack(">H", data[6:8])[0]
        class_majors[major] = class_majors.get(major, 0)+1
        if (entry in added or entry in changed) and (
            entry.startswith("blusunrize/immersiveengineering/common/")
            or entry.startswith("blusunrize/immersiveengineering/api/")
        ) and b"net/minecraft/client/" in data:
            client_refs.append(entry)

    if set(class_majors) != {52}:
        raise AssertionError("non-Java-8 class majors: %r" % class_majors)
    if client_refs:
        raise AssertionError("new common/API client refs: %r" % client_refs)

print("MIXER_M1_CANDIDATE_AUDIT_PASS")
print("baseline", BASE.name, sha(BASE))
print("candidate", CAND.name, sha(CAND))
print("entries", len(cand_names), "added", len(added), "removed", len(removed), "changed", len(changed))
print("class_majors", class_majors)
print("canonicalized_legacy_objs", len(normalized_objs))
print("new_entries", allowed_added)
