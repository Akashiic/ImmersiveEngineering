from pathlib import Path
import hashlib, json, re, subprocess

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport")
LEGACY = ROOT / "work" / "chief-1.21.1-masterplan"
MODERN = ROOT / "upstream" / "ImmersiveEngineering-1.21.1-chief"
BRIDGE = ROOT / "upstream" / "ImmersiveEngineering-cloche-1.12.2"
OUT = LEGACY / "docs" / "engineering" / "ALLOY-KILN-SURFACE-AUDIT.json"

def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest().upper()

def git(repo, *args):
    return subprocess.check_output(
        ["git", "-C", str(repo), *args], text=True, errors="replace"
    ).strip()

modern_assets = MODERN / "src/main/resources/assets/immersiveengineering"
recipe_dir = MODERN / "src/generated/resources/data/immersiveengineering/recipe/alloysmelter"
asset_rel = [
    "models/block/stone_multiblocks/cube_two.obj",
    "textures/block/multiblocks/alloy_smelter_bottom.png",
    "textures/block/multiblocks/alloy_smelter_off.png",
    "textures/block/multiblocks/alloy_smelter_on.png",
    "textures/block/multiblocks/alloy_smelter_side.png",
    "textures/block/multiblocks/alloy_smelter_top.png",
    "textures/gui/alloy_smelter.png",
]
assets = {}
for rel in asset_rel:
    path = modern_assets / rel
    assets[rel] = {"bytes": path.stat().st_size, "sha256": sha256(path)}

recipes = {}
for path in sorted(recipe_dir.glob("*.json")):
    data = json.loads(path.read_text(encoding="utf-8"))
    recipes[path.stem] = {
        "time": data.get("time", 200),
        "input0": data.get("input0"),
        "input1": data.get("input1"),
        "result": data.get("result"),
    }

stone_deco = (LEGACY / "src/main/java/blusunrize/immersiveengineering/common/blocks/stone/BlockStoneDecoration.java").read_text()
stone_dev = (LEGACY / "src/main/java/blusunrize/immersiveengineering/common/blocks/stone/BlockStoneDevices.java").read_text()
lib = (LEGACY / "src/main/java/blusunrize/immersiveengineering/common/util/Lib.java").read_text()

def subnames(source, registry_name):
    m = re.search(r'super\("'+re.escape(registry_name)+r'".*?;(?:\r?\n)', source, re.S)
    if not m:
        return []
    return re.findall(r'"([A-Za-z0-9_]+)"', m.group(0))[1:]

legacy_alloy_recipe = list((LEGACY / "src/main/java").rglob("AlloyRecipe.java"))
gui_ids = [int(v) for v in re.findall(r"GUIID_\w+\s*=\s*(\d+)", lib)]
history = git(
    BRIDGE, "log", "--format=%H%x09%s", "--follow", "--",
    "src/main/java/blusunrize/immersiveengineering/common/blocks/stone/TileEntityAlloySmelter.java"
).splitlines()

report = {
    "modern": {
        "commit": git(MODERN, "rev-parse", "HEAD"),
        "version": "1.21.1",
        "assets": assets,
        "recipes": recipes,
        "recipe_count": len(recipes),
        "structure": {"dimensions": [2, 2, 2], "alloy_bricks": 8},
    },
    "bridge_1_12_2": {
        "commit": git(BRIDGE, "rev-parse", "HEAD"),
        "history": history[:25],
        "original_feature_commit": "f5fdf38058a203c840d5a8d412cc984ff5229184",
    },
    "legacy_1_7_10": {
        "alloy_recipe_files": [str(p.relative_to(LEGACY)) for p in legacy_alloy_recipe],
        "stone_decoration_subnames": subnames(stone_deco, "stoneDecoration"),
        "stone_device_subnames": subnames(stone_dev, "stoneDevice"),
        "gui_ids": sorted(gui_ids),
    },
}
OUT.write_text(json.dumps(report, indent=2, sort_keys=True), encoding="utf-8")
print("modern recipes:", len(recipes), sorted(recipes))
print("legacy AlloyRecipe files:", len(legacy_alloy_recipe))
print("stoneDecoration:", report["legacy_1_7_10"]["stone_decoration_subnames"])
print("stoneDevice:", report["legacy_1_7_10"]["stone_device_subnames"])
print("GUI IDs:", sorted(gui_ids))
print("wrote", OUT)
