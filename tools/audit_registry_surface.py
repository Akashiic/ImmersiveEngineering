from pathlib import Path
import json, re

repo = Path(__file__).resolve().parents[1]
src = repo / "src" / "main" / "java"
metal = src / "blusunrize" / "immersiveengineering" / "common" / "blocks" / "metal"
lib = src / "blusunrize" / "immersiveengineering" / "common" / "util" / "Lib.java"
iecontent = src / "blusunrize" / "immersiveengineering" / "common" / "IEContent.java"

families = [
    "BlockMetalMultiblocks.java",
    "BlockMetalDevices.java",
    "BlockMetalDevices2.java",
    "BlockMetalDecoration.java",
    "BlockMetalDecoration2.java",
]
meta_re = re.compile(r"public\s+static\s+final\s+int\s+(META_[A-Za-z0-9_]+)\s*=\s*(\d+)")
result = {"metadata_families": {}, "gui_ids": {}, "tile_ids": []}

for name in families:
    text = (metal / name).read_text(encoding="utf-8", errors="replace")
    metas = {k: int(v) for k, v in meta_re.findall(text)}
    result["metadata_families"][name] = dict(sorted(metas.items(), key=lambda x: x[1]))
gui_text = lib.read_text(encoding="utf-8", errors="replace")
gui_re = re.compile(r"public\s+static\s+final\s+int\s+(GUIID_[A-Za-z0-9_]+)\s*=\s*(\d+)")
result["gui_ids"] = dict(sorted(((k, int(v)) for k, v in gui_re.findall(gui_text)), key=lambda x: x[1]))

content_text = iecontent.read_text(encoding="utf-8", errors="replace")
tile_re = re.compile(r"registerTile\((TileEntity[A-Za-z0-9_]+)\.class\)")
result["tile_ids"] = ["ImmersiveEngineering:" + n[len("TileEntity"):] for n in tile_re.findall(content_text)]

for family, metas in result["metadata_families"].items():
    used = sorted(metas.values())
    duplicates = sorted({v for v in used if used.count(v) > 1})
    result.setdefault("metadata_summary", {})[family] = {
        "used": used,
        "free": [i for i in range(16) if i not in used],
        "duplicates": duplicates,
    }

result["gui_summary"] = {
    "used": sorted(result["gui_ids"].values()),
    "duplicates": sorted({v for v in result["gui_ids"].values() if list(result["gui_ids"].values()).count(v) > 1}),
}
tile_dupes = sorted({x for x in result["tile_ids"] if result["tile_ids"].count(x) > 1})
result["tile_summary"] = {"count": len(result["tile_ids"]), "duplicates": tile_dupes}

out = repo / "docs" / "engineering" / "IE-1.7.10-REGISTRY-SURFACE.json"
out.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n", encoding="utf-8")

for family, summary in result["metadata_summary"].items():
    print(family, "used", summary["used"], "free", summary["free"], "dupes", summary["duplicates"])
print("GUI", result["gui_summary"])
print("TILES", result["tile_summary"])

assert not result["gui_summary"]["duplicates"]
assert not tile_dupes
assert all(not x["duplicates"] for x in result["metadata_summary"].values())
print("REGISTRY_AUDIT_PASS")
