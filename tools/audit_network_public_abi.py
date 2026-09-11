from pathlib import Path
import subprocess

repo = Path(__file__).resolve().parents[1]
root = repo.parents[1]
baseline = root / "dist" / "auto-workbench-test-1-r2" / "ImmersiveEngineering-auto-workbench-test-1-r2.jar"
candidate = next(p for p in (repo / "build" / "libs").glob("*.jar") if not p.name.endswith("-dev.jar") and not p.name.endswith("-sources.jar"))
classes = [
    "blusunrize.immersiveengineering.common.blocks.TileEntityIEBase",
    "blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler",
    "blusunrize.immersiveengineering.common.blocks.metal.TileEntityAutoWorkbench",
    "blusunrize.immersiveengineering.common.blocks.metal.TileEntityConveyorSorter",
    "blusunrize.immersiveengineering.common.gui.ContainerAssembler",
    "blusunrize.immersiveengineering.common.gui.ContainerSorter",
    "blusunrize.immersiveengineering.common.util.network.MessageTileSync",
]

def public_lines(jar, cls):
    cp = subprocess.run(["javap", "-public", "-classpath", str(jar), cls], check=True, text=True, capture_output=True)
    return {line.strip() for line in cp.stdout.splitlines() if line.strip().endswith(";")}

failed = False
for cls in classes:
    before = public_lines(baseline, cls)
    after = public_lines(candidate, cls)
    missing = sorted(before-after)
    added = sorted(after-before)
    print(cls, "missing", len(missing), "added", len(added))
    for line in missing:
        print("  MISSING", line)
    for line in added:
        print("  ADDED", line)
    failed |= bool(missing)

if failed:
    raise SystemExit("ABI_AUDIT_FAIL")
print("ABI_AUDIT_PASS")
