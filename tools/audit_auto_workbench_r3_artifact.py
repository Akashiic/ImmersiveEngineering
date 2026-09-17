#!/usr/bin/env python3
from pathlib import Path
import hashlib
import subprocess
import sys
import zipfile

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport")
REPO = ROOT / "work" / "ImmersiveEngineering-Forbidden-Backport"
R2 = ROOT / "dist" / "auto-workbench-test-1-r2" / "ImmersiveEngineering-auto-workbench-test-1-r2.jar"
R3 = REPO / "build" / "libs" / "ImmersiveEngineering-auto-workbench-test-1-r3.jar"
R3_TAG = "auto-workbench-test-1-r3"
R3_COMMIT = "5d467a612605c1201a13bde52b54e3550477ac05"
R3_SHA = "B91A12EBC1D424915CB8E3DE5F3267D5699FA564BA605FAFDD9635E61CFB0A12"

checks = []
def check(name, condition, detail=""):
    checks.append((name, bool(condition), detail))
    print(name + "=" + ("PASS" if condition else "FAIL") + ((" :: " + detail) if detail else ""))

def sha(data):
    return hashlib.sha256(data).hexdigest().upper()

def file_sha(path):
    return sha(path.read_bytes())
check("r2_exists", R2.is_file(), str(R2))
check("r3_exists", R3.is_file(), str(R3))
if not R2.is_file() or not R3.is_file():
    raise SystemExit(1)
check("r3_sha256", file_sha(R3) == R3_SHA, file_sha(R3))
tag_target = subprocess.check_output(["git", "rev-list", "-n", "1", R3_TAG], cwd=str(REPO), text=True).strip()
check("r3_tag_target", tag_target == R3_COMMIT, tag_target)

z2 = zipfile.ZipFile(R2)
z3 = zipfile.ZipFile(R3)
n2 = z2.namelist()
n3 = z3.namelist()
s2 = set(n2)
s3 = set(n3)
check("r3_entries_1405", len(n3) == 1405, str(len(n3)))
check("r3_no_duplicate_entries", len(n3) == len(s3))
check("entry_set_unchanged_from_r2", s2 == s3)

changed = {n for n in s2 & s3 if sha(z2.read(n)) != sha(z3.read(n))}
expected_changed = {
    "blusunrize/immersiveengineering/common/blocks/metal/TileEntityAutoWorkbench.class",
    "blusunrize/immersiveengineering/common/blocks/metal/TileEntityAutoWorkbench$AutoWorkbenchProcess.class",
    "blusunrize/immersiveengineering/common/gui/ContainerAutoWorkbench.class",
    "blusunrize/immersiveengineering/common/gui/ContainerAutoWorkbench$1.class",
}
check("changed_entries_exact", changed == expected_changed, repr(sorted(changed)))
classes = [n for n in n3 if n.endswith(".class")]
majors = {int.from_bytes(z3.read(n)[6:8], "big") for n in classes}
check("class_count_747", len(classes) == 747, str(len(classes)))
check("all_classes_java8", majors == {52}, repr(sorted(majors)))

def common_client_refs(z):
    return {n for n in z.namelist()
            if n.endswith(".class")
            and (n.startswith("blusunrize/immersiveengineering/common/") or n.startswith("blusunrize/immersiveengineering/api/"))
            and b"net/minecraft/client/" in z.read(n)}
refs2 = common_client_refs(z2)
refs3 = common_client_refs(z3)
check("common_client_ref_count_unchanged", len(refs2) == len(refs3) == 55, "%d->%d" % (len(refs2), len(refs3)))
check("no_new_common_client_refs", refs3-refs2 == set(), repr(sorted(refs3-refs2)))
check("auto_workbench_common_has_no_client_refs", not [n for n in refs3 if "AutoWorkbench" in n])

assets = [
    "assets/immersiveengineering/models/autoWorkbench.obj",
    "assets/immersiveengineering/models/autoWorkbenchMirrored.obj",
    "assets/immersiveengineering/models/autoWorkbenchAnimated.obj",
    "assets/immersiveengineering/textures/blocks/metalDecoration0_redstoneEngineering.png",
    "assets/immersiveengineering/textures/blocks/metal_multiblock_autoworkbench.png",
    "assets/immersiveengineering/textures/blocks/metal_multiblock_autoworkbench_blueprint.png",
    "assets/immersiveengineering/textures/gui/auto_workbench.png",
    "assets/immersiveengineering/textures/gui/auto_workbench_buttons.png",
]
check("all_auto_workbench_assets_unchanged", all(z2.read(n) == z3.read(n) for n in assets))

def javap(jar, cls, *opts):
    return subprocess.check_output(["javap", "-classpath", str(jar), *opts, cls], text=True, stderr=subprocess.STDOUT)

for cls in [
    "blusunrize.immersiveengineering.common.blocks.metal.TileEntityAutoWorkbench$AutoWorkbenchProcess",
    "blusunrize.immersiveengineering.common.gui.ContainerAutoWorkbench$1",
]:
    check("logical_bytecode_unchanged_" + cls.rsplit(".", 1)[-1], javap(R2, cls, "-c", "-p") == javap(R3, cls, "-c", "-p"))

def surface(jar, cls):
    out = javap(jar, cls, "-protected", "-s")
    return {line.strip() for line in out.splitlines() if line.strip() and not line.startswith("Compiled from") and line.strip() != "}"}
for cls in [
    "blusunrize.immersiveengineering.common.blocks.metal.TileEntityAutoWorkbench",
    "blusunrize.immersiveengineering.common.gui.ContainerAutoWorkbench",
]:
    removed = surface(R2, cls) - surface(R3, cls)
    check("abi_removed_" + cls.rsplit(".", 1)[-1], not removed, repr(sorted(removed)))

mcmod = z3.read("mcmod.info").decode("utf-8", errors="replace")
manifest = z3.read("META-INF/MANIFEST.MF").decode("utf-8", errors="replace")
check("mcmod_version_0_7_9", '"version": "1.7.10-0.7.9"' in mcmod)
check("forbidden_series_identity", "Forbidden Series Edition" in mcmod)
check("manifest_fmlat", "FMLAT: ImmersiveEngineering_at.cfg" in manifest)

failed = [name for name, ok, _ in checks if not ok]
print("TOTAL_PASS=%d/%d" % (len(checks)-len(failed), len(checks)))
if failed:
    print("FAILED=" + ",".join(failed))
    raise SystemExit(1)
print("AUTO_WORKBENCH_R3_ARTIFACT_GATE=PASS")