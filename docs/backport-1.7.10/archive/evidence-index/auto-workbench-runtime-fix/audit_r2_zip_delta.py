from pathlib import Path
import zipfile

base = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\dist\garden-cloche-test-2\ImmersiveEngineering-garden-cloche-test-2.jar")
old = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\dist\auto-workbench-test-1\ImmersiveEngineering-auto-workbench-test-1.jar")
r2 = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport\build\libs\ImmersiveEngineering-auto-workbench-test-1-r2.jar")
assert base.exists() and old.exists() and r2.exists(), (base.exists(), old.exists(), r2.exists())

def load(path):
    with zipfile.ZipFile(path) as z:
        names = z.namelist()
        return names, {n: z.read(n) for n in names}

bn, bd = load(base)
on, od = load(old)
rn, rd = load(r2)
assert len(rn) == len(set(rn))
old_added = sorted(set(rn)-set(on))
old_removed = sorted(set(on)-set(rn))
old_changed = sorted(n for n in set(on)&set(rn) if od[n] != rd[n])
print("REJECTED_TO_R2 added", len(old_added), "removed", len(old_removed), "changed", len(old_changed))
for n in old_changed: print("R2_CHANGED", n)
expected = {"assets/immersiveengineering/models/autoWorkbench.obj", "assets/immersiveengineering/models/autoWorkbenchMirrored.obj"}
assert not old_added and not old_removed and set(old_changed) == expected, (old_added, old_removed, old_changed)
base_added = sorted(set(rn)-set(bn))
base_removed = sorted(set(bn)-set(rn))
base_changed = sorted(n for n in set(bn)&set(rn) if bd[n] != rd[n])
print("CLOCHE_TO_R2 added", len(base_added), "removed", len(base_removed), "changed", len(base_changed))
assert len(base_added) == 25
assert not base_removed
assert len(base_changed) == 14
for n in base_added: print("BASE_ADDED", n)
for n in base_changed: print("BASE_CHANGED", n)
assert bd["mcmod.info"] == rd["mcmod.info"]
assert bd["META-INF/MANIFEST.MF"] == rd["META-INF/MANIFEST.MF"]
print("MCMOD_MANIFEST_IDENTITY=PASS")
print("AUTO_WORKBENCH_R2_ZIP_DELTA_GATE=PASS")