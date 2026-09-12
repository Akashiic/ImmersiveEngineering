from pathlib import Path
import hashlib
import json
import shutil

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport")
UPSTREAM = ROOT / "upstream" / "ImmersiveEngineering-1.21.1-chief"
REPO = ROOT / "work" / "mixer"
SOURCE = UPSTREAM / "src/main/resources/assets/immersiveengineering/textures/block/fluid"
DEST = REPO / "src/main/resources/assets/immersiveengineering/textures/blocks/fluid"
MANIFEST = REPO / "docs/engineering/MIXER-M2-ASSET-MANIFEST.json"
COMMIT = "75a27f03e4243544243567e8d5c38d336f4f10f4"


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest().upper()


DEST.mkdir(parents=True, exist_ok=True)
manifest = {"source_commit": COMMIT, "files": {}}
for fluid in ("concrete", "herbicide", "redstone_acid"):
    for suffix in ("still.png", "still.png.mcmeta", "flow.png", "flow.png.mcmeta"):
        name = "%s_%s" % (fluid, suffix)
        source = SOURCE / name
        if not source.is_file():
            raise RuntimeError("Missing modern Mixer fluid asset: %s" % source)
        target = DEST / name
        shutil.copyfile(source, target)
        manifest["files"][str(target.relative_to(REPO)).replace("\\", "/")] = {
            "source": str(source.relative_to(UPSTREAM)).replace("\\", "/"),
            "source_sha256": sha256(source),
            "output_sha256": sha256(target),
            "transform": "identity_path_translation",
        }

MANIFEST.parent.mkdir(parents=True, exist_ok=True)
MANIFEST.write_text(json.dumps(manifest, indent=2, sort_keys=True), encoding="utf-8")
print("prepared", len(manifest["files"]), "Mixer M2 fluid assets")
print("manifest", MANIFEST)
