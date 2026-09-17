#!/usr/bin/env python3
from pathlib import Path
import hashlib
import subprocess
import sys
import zipfile

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport")
DIST = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\dist\auto-workbench-test-1-r2")
PROD = Path(r"C:\Users\User\AppData\Roaming\.technic\modpacks\forbidden-series\mods")
TAG = "auto-workbench-test-1-r2"
TAG_TARGET = "97cea596eed2254050f602428853007caade286d"
EXPECTED_SHA = "C72F7C4AAD54491F83EA6073799DBEAFF44C260B8B4CBDE33249F1351820FA97"
JAR_NAME = "ImmersiveEngineering-auto-workbench-test-1-r2.jar"

def sha256(path):
    h = hashlib.sha256()
    with open(path, "rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest().upper()

def run(*args):
    return subprocess.check_output(args, cwd=str(ROOT), text=True).strip()
checks = []
def check(name, condition, detail=""):
    checks.append((name, bool(condition), detail))
    print(name + "=" + ("PASS" if condition else "FAIL") + ((" :: " + detail) if detail else ""))

jar = DIST / JAR_NAME
prod_jar = PROD / JAR_NAME
check("dist_jar_exists", jar.is_file(), str(jar))
check("production_jar_exists", prod_jar.is_file(), str(prod_jar))
if not jar.is_file() or not prod_jar.is_file():
    raise SystemExit(1)

check("tag_target", run("git", "rev-list", "-n", "1", TAG) == TAG_TARGET)
ancestor = subprocess.run(["git", "merge-base", "--is-ancestor", TAG, "HEAD"], cwd=str(ROOT)).returncode == 0
check("tag_is_ancestor_of_head", ancestor)
check("dist_sha256", sha256(jar) == EXPECTED_SHA, sha256(jar))
check("production_sha256", sha256(prod_jar) == EXPECTED_SHA, sha256(prod_jar))
check("dist_production_byte_identity", jar.read_bytes() == prod_jar.read_bytes())

ie_jars = sorted(PROD.glob("ImmersiveEngineering*.jar"))
check("single_ie_jar_in_production", len(ie_jars) == 1, ",".join(p.name for p in ie_jars))
required = [
    "blusunrize/immersiveengineering/common/blocks/metal/TileEntityAutoWorkbench.class",
    "blusunrize/immersiveengineering/common/blocks/multiblocks/MultiblockAutoWorkbench.class",
    "blusunrize/immersiveengineering/common/gui/ContainerAutoWorkbench.class",
    "blusunrize/immersiveengineering/client/gui/GuiAutoWorkbench.class",
    "blusunrize/immersiveengineering/client/render/TileRenderAutoWorkbench.class",
    "assets/immersiveengineering/models/autoWorkbench.obj",
    "assets/immersiveengineering/models/autoWorkbenchMirrored.obj",
    "assets/immersiveengineering/models/autoWorkbenchAnimated.obj",
]
expected_assets = {
    "assets/immersiveengineering/models/autoWorkbench.obj": "263C39C0861D841A8A593672062DFB8C3A7ACF3F2B3409C18C76B9F290412434",
    "assets/immersiveengineering/models/autoWorkbenchMirrored.obj": "D2DD6FFC9616B253E2C17F995A5844F9C301EA732179B5C59E21DB61D96B80F0",
    "assets/immersiveengineering/models/autoWorkbenchAnimated.obj": "68B20FBDEF15804F488621C9CBFE6CE2BFF5922D2726E8464E6A48F5A6705C09",
}
with zipfile.ZipFile(jar) as z:
    names = z.namelist()
    check("zip_duplicate_entries", len(names) == len(set(names)), str(len(names)))
    check("required_entries", all(name in names for name in required))
    classes = [name for name in names if name.endswith(".class")]
    majors = {int.from_bytes(z.read(name)[6:8], "big") for name in classes}
    check("all_classes_java8", majors == {52}, repr(sorted(majors)))
    for name, expected in expected_assets.items():
        actual = hashlib.sha256(z.read(name)).hexdigest().upper()
        check("asset_" + Path(name).name + "_sha256", actual == expected, actual)
    info = z.read("mcmod.info").decode("utf-8", errors="replace")
    check("mcmod_version_0_7_9", '"version": "1.7.10-0.7.9"' in info)
    check("forbidden_series_identity", "Forbidden Series Edition" in info)

for script in [
    "tools/audit_auto_workbench_obj_topology.py",
    "tools/audit_auto_workbench_transform.py",
    "tools/audit_auto_workbench_structure.py",
    "tools/harden_auto_workbench_blueprint_points.py",
]:
    proc = subprocess.run([sys.executable, str(ROOT / script)], cwd=str(ROOT), capture_output=True, text=True)
    check("script_" + Path(script).stem, proc.returncode == 0, proc.stdout.strip().splitlines()[-1] if proc.stdout.strip() else proc.stderr.strip())

failed = [name for name, ok, _ in checks if not ok]
print("TOTAL_PASS=%d/%d" % (len(checks)-len(failed), len(checks)))
if failed:
    print("FAILED=" + ",".join(failed))
    raise SystemExit(1)
print("AUTO_WORKBENCH_R2_REGRESSION_GATE=PASS")