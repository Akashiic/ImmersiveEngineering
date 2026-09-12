from pathlib import Path
import collections
import hashlib
import json
import re
import struct
import zipfile

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport")
REPO = ROOT / "work" / "alloy-kiln"
BASELINE = ROOT / "dist" / "auto-workbench-test-1-r2" / "ImmersiveEngineering-auto-workbench-test-1-r2.jar"

def sha(data):
    if isinstance(data, Path):
        data = data.read_bytes()
    return hashlib.sha256(data).hexdigest().upper()

candidates = [p for p in (REPO / "build" / "libs").glob("*.jar")
              if not p.name.endswith("-dev.jar") and not p.name.endswith("-sources.jar")]
if len(candidates) != 1:
    raise RuntimeError("Expected exactly one release jar, got %r" % candidates)
CANDIDATE = candidates[0]
print("BASELINE", BASELINE.name, sha(BASELINE))
print("CANDIDATE", CANDIDATE.name, sha(CANDIDATE))

with zipfile.ZipFile(BASELINE) as zb, zipfile.ZipFile(CANDIDATE) as zc:
    base = zb.namelist()
    cand = zc.namelist()
    assert zc.testzip() is None
    assert len(cand) == len(set(cand))
    added = sorted(set(cand)-set(base))
    removed = sorted(set(base)-set(cand))
    changed = sorted(n for n in set(base)&set(cand) if zb.read(n)!=zc.read(n))
    print("ZIP", len(cand), "entries; added", len(added), "removed", len(removed), "changed", len(changed))
    if removed:
        raise RuntimeError("Baseline entries removed: %r" % removed)

    majors = collections.Counter()
    for name in cand:
        if name.endswith(".class"):
            data = zc.read(name)
            if data[:4] != b"\xca\xfe\xba\xbe":
                raise RuntimeError("Bad class header: "+name)
            majors[struct.unpack(">H", data[6:8])[0]] += 1
    print("CLASS_MAJORS", dict(sorted(majors.items())))
    if set(majors) != {52}:
        raise RuntimeError("Unexpected class majors: %r" % dict(majors))

    manifest = zc.read("META-INF/MANIFEST.MF").decode("utf-8", "replace")
    mcmod = zc.read("mcmod.info").decode("utf-8", "replace")
    assert "FMLAT: ImmersiveEngineering_at.cfg" in manifest
    assert "1.7.10-0.7.9" in mcmod and "Forbidden Series Edition" in mcmod

    expected_classes = {
        "blusunrize/immersiveengineering/api/crafting/AlloyRecipe.class",
        "blusunrize/immersiveengineering/api/crafting/IngredientStack.class",
        "blusunrize/immersiveengineering/common/blocks/stone/TileEntityAlloySmelter.class",
        "blusunrize/immersiveengineering/common/blocks/multiblocks/MultiblockAlloySmelter.class",
        "blusunrize/immersiveengineering/common/gui/ContainerAlloySmelter.class",
        "blusunrize/immersiveengineering/client/gui/GuiAlloySmelter.class",
        "blusunrize/immersiveengineering/client/nei/NEIAlloySmelterHandler.class",
        "blusunrize/immersiveengineering/common/util/compat/minetweaker/AlloySmelter.class",
    }
    missing = sorted(expected_classes-set(cand))
    if missing:
        raise RuntimeError("Missing Alloy Kiln classes: %r" % missing)

    protected = [
        "assets/immersiveengineering/models/autoWorkbench.obj",
        "assets/immersiveengineering/models/autoWorkbenchMirrored.obj",
        "assets/immersiveengineering/models/autoWorkbenchAnimated.obj",
    ]
    for entry in protected:
        assert zb.read(entry) == zc.read(entry), entry

    server_core = [
        "blusunrize/immersiveengineering/api/crafting/AlloyRecipe.class",
        "blusunrize/immersiveengineering/api/crafting/IngredientStack.class",
        "blusunrize/immersiveengineering/common/blocks/stone/TileEntityAlloySmelter.class",
        "blusunrize/immersiveengineering/common/blocks/multiblocks/MultiblockAlloySmelter.class",
        "blusunrize/immersiveengineering/common/gui/ContainerAlloySmelter.class",
        "blusunrize/immersiveengineering/common/util/compat/minetweaker/AlloySmelter.class",
    ]
    forbidden = [b"net/minecraft/client/", b"blusunrize/immersiveengineering/client/"]
    bad_refs = []
    for entry in server_core:
        data = zc.read(entry)
        for needle in forbidden:
            if needle in data:
                bad_refs.append((entry, needle.decode()))
    print("SERVER_CORE_CLIENT_REFS", bad_refs)
    if bad_refs:
        raise RuntimeError("Dedicated-side client refs: %r" % bad_refs)

manifest_path = REPO / "docs" / "engineering" / "ALLOY-KILN-ASSET-MANIFEST.json"
asset_manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
with zipfile.ZipFile(CANDIDATE) as zc:
    for key, meta in asset_manifest["files"].items():
        if key.startswith("gui/"):
            entry = "assets/immersiveengineering/textures/"+key
        else:
            entry = "assets/immersiveengineering/textures/blocks/"+key
        actual = sha(zc.read(entry))
        if actual != meta["output_sha256"]:
            raise RuntimeError("Asset hash mismatch %s: %s != %s" % (entry, actual, meta["output_sha256"]))
print("ASSET_MANIFEST_PASS", len(asset_manifest["files"]))

lib = (REPO / "src/main/java/blusunrize/immersiveengineering/common/util/Lib.java").read_text(encoding="utf-8")
gui_pairs = [(name, int(value)) for name, value in re.findall(r"GUIID_(\w+)\s*=\s*(\d+)", lib)]
gui_values = [value for _, value in gui_pairs]
assert len(gui_values) == len(set(gui_values)), gui_pairs
assert dict(gui_pairs).get("GardenCloche") == 13
assert dict(gui_pairs).get("AutoWorkbench") == 14
assert dict(gui_pairs).get("AlloySmelter") == 15

content = (REPO / "src/main/java/blusunrize/immersiveengineering/common/IEContent.java").read_text(encoding="utf-8")
assert "blockStoneDecoration2 = new BlockStoneDecoration2();" in content
assert "blockStoneDevice2 = new BlockStoneDevices2();" in content
assert "registerTile(TileEntityAlloySmelter.class);" in content
assert "MultiblockHandler.registerMultiblock(MultiblockAlloySmelter.instance);" in content

recipe_src = (REPO / "src/main/java/blusunrize/immersiveengineering/common/IEContent.java").read_text(encoding="utf-8")
assert 'new IngredientStack("blockGlass", 2)' in recipe_src
assert 'addConditionalAlloy("ingotBronze", 4' in recipe_src
assert 'addConditionalAlloy("ingotInvar", 3' in recipe_src
assert "ingotManyullyn" not in recipe_src
print("REGISTRY_RECIPE_INVARIANTS_PASS")
print("ALLOY_KILN_CANDIDATE_AUDIT_PASS")
