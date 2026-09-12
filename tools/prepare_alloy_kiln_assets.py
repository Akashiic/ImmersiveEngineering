from pathlib import Path
from PIL import Image
import hashlib, json, shutil

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport")
UPSTREAM = ROOT / "upstream" / "ImmersiveEngineering-1.21.1-chief"
REPO = ROOT / "work" / "alloy-kiln"
SRC = UPSTREAM / "src/main/resources/assets/immersiveengineering/textures"
DST = REPO / "src/main/resources/assets/immersiveengineering/textures/blocks"
MANIFEST = REPO / "docs/engineering/ALLOY-KILN-ASSET-MANIFEST.json"

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest().upper()

DST.mkdir(parents=True, exist_ok=True)
manifest = {"source_commit": "75a27f03e4243544243567e8d5c38d336f4f10f4", "files": {}}

brick_src = SRC / "block/stone_decoration/alloybrick.png"
brick_dst = DST / "stoneDecoration2_alloyBrick.png"
shutil.copyfile(brick_src, brick_dst)
manifest["files"][brick_dst.name] = {
    "source": str(brick_src.relative_to(UPSTREAM)),
    "source_sha256": sha(brick_src),
    "output_sha256": sha(brick_dst),
    "transform": "identity",
}

insulating_src = SRC / "block/stone_decoration/insulating_glass.png"
insulating_dst = DST / "stoneDevice_insulatorGlass.png"
shutil.copyfile(insulating_src, insulating_dst)
manifest["files"][insulating_dst.name] = {
    "source": str(insulating_src.relative_to(UPSTREAM)),
    "source_sha256": sha(insulating_src),
    "output_sha256": sha(insulating_dst),
    "transform": "identity-registry-preserving",
}
faces = {
    "bottom": "block/multiblocks/alloy_smelter_bottom.png",
    "front_off": "block/multiblocks/alloy_smelter_off.png",
    "front_on": "block/multiblocks/alloy_smelter_on.png",
    "side": "block/multiblocks/alloy_smelter_side.png",
    "top": "block/multiblocks/alloy_smelter_top.png",
}
for label, rel in faces.items():
    source = SRC / rel
    image = Image.open(source).convert("RGBA")
    if image.size != (32, 32):
        raise RuntimeError("Unexpected Alloy Kiln face size: %s=%s" % (source, image.size))
    for row in range(2):
        for col in range(2):
            out = DST / ("stoneDevice2_alloySmelter_%s_%d%d.png" % (label, col, row))
            image.crop((col*16, row*16, col*16+16, row*16+16)).save(out)
            manifest["files"][out.name] = {
                "source": str(source.relative_to(UPSTREAM)),
                "source_sha256": sha(source),
                "output_sha256": sha(out),
                "transform": "crop16",
                "quadrant": [col, row],
            }

gui_src = SRC / "gui/alloy_smelter.png"
gui_dst = REPO / "src/main/resources/assets/immersiveengineering/textures/gui/alloy_smelter.png"
shutil.copyfile(gui_src, gui_dst)
manifest["files"]["gui/alloy_smelter.png"] = {
    "source": str(gui_src.relative_to(UPSTREAM)),
    "source_sha256": sha(gui_src),
    "output_sha256": sha(gui_dst),
    "transform": "identity",
}
MANIFEST.parent.mkdir(parents=True, exist_ok=True)
MANIFEST.write_text(json.dumps(manifest, indent=2, sort_keys=True), encoding="utf-8")
print("prepared", len(manifest["files"]), "Alloy Kiln assets")
print("manifest", MANIFEST)
