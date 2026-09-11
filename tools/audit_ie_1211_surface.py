import argparse
import json
import re
from pathlib import Path


def load_json(path: Path):
    with path.open("r", encoding="utf-8") as handle:
        return json.load(handle)


def load_legacy_lang(path: Path):
    result = {}
    for raw in path.read_text(encoding="utf-8", errors="replace").splitlines():
        line = raw.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        result[key.strip()] = value.strip()
    return result


def normalize_name(value: str):
    return re.sub(r"[^a-z0-9]+", "", value.lower())


def collect_modern_names(lang):
    blocks = {k: v for k, v in lang.items() if k.startswith("block.immersiveengineering.")}
    items = {k: v for k, v in lang.items() if k.startswith("item.immersiveengineering.")}
    return blocks, items


def collect_legacy_names(lang):
    blocks = {}
    items = {}
    for key, value in lang.items():
        if key.startswith("tile.ImmersiveEngineering.") and key.endswith(".name"):
            blocks[key] = value
        elif key.startswith("item.ImmersiveEngineering.") and key.endswith(".name"):
            items[key] = value
    return blocks, items


def absent_by_display_name(modern, legacy):
    legacy_norm = {normalize_name(value) for value in legacy.values()}
    return {
        key: value for key, value in modern.items()
        if normalize_name(value) not in legacy_norm
    }


def list_multiblocks(modern_root: Path):
    folder = modern_root / "src/main/java/blusunrize/immersiveengineering/common/blocks/multiblocks"
    names = []
    for path in folder.glob("*Multiblock.java"):
        if path.name in {"IETemplateMultiblock.java", "UnionMultiblock.java", "StoneMultiblock.java"}:
            continue
        names.append(path.stem.removesuffix("Multiblock"))
    return sorted(names)


def count_assets(modern_root: Path):
    asset_root = modern_root / "src/main/resources/assets/immersiveengineering"
    generated_root = modern_root / "src/generated/resources/assets/immersiveengineering"
    roots = [p for p in (asset_root, generated_root) if p.exists()]
    counts = {"obj": 0, "json": 0, "png": 0, "ogg": 0}
    for root in roots:
        for path in root.rglob("*"):
            if path.is_file():
                suffix = path.suffix.lower().lstrip(".")
                if suffix in counts:
                    counts[suffix] += 1
    return counts


def count_sounds(modern_root: Path):
    sounds_file = modern_root / "src/main/resources/assets/immersiveengineering/sounds.json"
    if not sounds_file.exists():
        return 0, []
    data = load_json(sounds_file)
    return len(data), sorted(data)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--modern", required=True, type=Path)
    parser.add_argument("--legacy", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()

    modern_lang = load_json(
        args.modern / "src/generated/resources/assets/immersiveengineering/lang/en_us.json"
    )
    legacy_lang = load_legacy_lang(
        args.legacy / "src/main/resources/assets/immersiveengineering/lang/en_US.lang"
    )
    modern_blocks, modern_items = collect_modern_names(modern_lang)
    legacy_blocks, legacy_items = collect_legacy_names(legacy_lang)
    sound_count, sound_keys = count_sounds(args.modern)

    report = {
        "modern_block_translation_count": len(modern_blocks),
        "modern_item_translation_count": len(modern_items),
        "legacy_block_translation_count": len(legacy_blocks),
        "legacy_item_translation_count": len(legacy_items),
        "modern_blocks_absent_by_display_name": absent_by_display_name(modern_blocks, legacy_blocks),
        "modern_items_absent_by_display_name": absent_by_display_name(modern_items, legacy_items),
        "modern_multiblocks": list_multiblocks(args.modern),
        "modern_sound_event_count": sound_count,
        "modern_sound_events": sound_keys,
        "modern_asset_counts": count_assets(args.modern),
        "notes": [
            "Display-name comparison is an inventory heuristic, not a compatibility decision.",
            "Renames, metadata-backed legacy entries and semantic equivalents require manual audit.",
        ],
    }
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2, ensure_ascii=False), encoding="utf-8")
    print(json.dumps({k: v for k, v in report.items() if not isinstance(v, dict) and not isinstance(v, list)}, indent=2))
    print(f"modern multiblocks: {len(report['modern_multiblocks'])}")
    print(f"candidate missing blocks: {len(report['modern_blocks_absent_by_display_name'])}")
    print(f"candidate missing items: {len(report['modern_items_absent_by_display_name'])}")


if __name__ == "__main__":
    main()
