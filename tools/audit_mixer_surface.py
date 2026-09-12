from pathlib import Path
import hashlib
import json
import subprocess

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport")
MODERN = ROOT / "upstream" / "ImmersiveEngineering-1.21.1-chief"
BRIDGE = ROOT / "upstream" / "ImmersiveEngineering-cloche-1.12.2"
REPO = ROOT / "work" / "chief-1.21.1-masterplan"
OUT = REPO / "docs" / "engineering" / "MIXER-SURFACE-AUDIT.json"

MODERN_COMMIT = subprocess.check_output(
    ["git", "-C", str(MODERN), "rev-parse", "HEAD"], text=True).strip()
BRIDGE_COMMIT = subprocess.check_output(
    ["git", "-C", str(BRIDGE), "rev-parse", "HEAD"], text=True).strip()

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest().upper()

def obj_stats(path):
    lines = path.read_text(encoding="utf-8", errors="ignore").splitlines()
    faces = [line.split() for line in lines if line.startswith("f ")]
    arities = [len(face)-1 for face in faces]
    return {
        "sha256": sha(path),
        "vertices": sum(1 for line in lines if line.startswith("v ")),
        "uvs": sum(1 for line in lines if line.startswith("vt ")),
        "faces": len(faces),
        "triangles": arities.count(3),
        "quads": arities.count(4),
    }

modern_models = MODERN / "src/main/resources/assets/immersiveengineering/models/block/metal_multiblock"
model_data = {
    name: obj_stats(modern_models/name)
    for name in ("mixer.obj", "mixer_agitator.obj")
}

recipe_dir = MODERN / "src/generated/resources/data/immersiveengineering/recipe/mixer"
recipes = {}
for path in sorted(recipe_dir.glob("*.json")):
    recipes[path.stem] = json.loads(path.read_text(encoding="utf-8"))

assets = {}
for rel in (
    "src/main/resources/assets/immersiveengineering/textures/gui/mixer.png",
    "src/main/resources/assets/immersiveengineering/textures/block/multiblocks/mixer.png",
    "src/main/resources/assets/immersiveengineering/sounds/mixer.ogg",
):
    path = MODERN / rel
    assets[rel] = {"size": path.stat().st_size, "sha256": sha(path)}

bridge_files = [
    "src/main/java/blusunrize/immersiveengineering/api/crafting/MixerRecipe.java",
    "src/main/java/blusunrize/immersiveengineering/common/blocks/metal/TileEntityMixer.java",
    "src/main/java/blusunrize/immersiveengineering/common/blocks/multiblocks/MultiblockMixer.java",
    "src/main/java/blusunrize/immersiveengineering/common/gui/ContainerMixer.java",
    "src/main/java/blusunrize/immersiveengineering/client/gui/GuiMixer.java",
    "src/main/java/blusunrize/immersiveengineering/client/render/TileRenderMixer.java",
]

history = subprocess.check_output([
    "git", "-C", str(BRIDGE), "log", "--oneline", "--all", "--",
    "src/main/java/blusunrize/immersiveengineering/common/blocks/metal/TileEntityMixer.java",
    "src/main/java/blusunrize/immersiveengineering/api/crafting/MixerRecipe.java",
], text=True, errors="replace").splitlines()

baseline_hits = []
for path in (REPO / "src/main/java").rglob("*.java"):
    try:
        text = path.read_text(encoding="utf-8", errors="ignore")
    except OSError:
        continue
    if "Mixer" in text or "mixer" in text:
        baseline_hits.append(str(path.relative_to(REPO)))

result = {
    "modern_commit": MODERN_COMMIT,
    "bridge_commit": BRIDGE_COMMIT,
    "modern_models": model_data,
    "modern_assets": assets,
    "modern_recipes": recipes,
    "bridge_files": {rel: (BRIDGE/rel).exists() for rel in bridge_files},
    "bridge_history": history[:40],
    "baseline_mixer_source_hits": baseline_hits,
}
OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_text(json.dumps(result, indent=2, sort_keys=True), encoding="utf-8")
print("modern models", model_data)
print("modern recipes", sorted(recipes))
print("baseline hits", len(baseline_hits))
print("wrote", OUT)
