from pathlib import Path
import re
import subprocess
import tempfile
import zipfile

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport")
REPO = ROOT / "work" / "alloy-kiln"
BASE = ROOT / "dist" / "auto-workbench-test-1-r2" / "ImmersiveEngineering-auto-workbench-test-1-r2.jar"
CANDS = [p for p in (REPO / "build" / "libs").glob("*.jar")
         if not p.name.endswith("-dev.jar") and not p.name.endswith("-sources.jar")]
assert len(CANDS) == 1, CANDS
CAND = CANDS[0]

with zipfile.ZipFile(BASE) as zb, zipfile.ZipFile(CAND) as zc:
    common = sorted(set(zb.namelist()) & set(zc.namelist()))
    changed = [n for n in common if n.endswith(".class") and zb.read(n) != zc.read(n)]
    changed_top = sorted({n.split("$")[0] for n in changed if "$" not in n})
print("CHANGED_TOP_CLASSES", len(changed_top))

def public_surface(jar, class_name):
    proc = subprocess.run(
        ["javap", "-classpath", str(jar), "-public", class_name],
        stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, check=False)
    if proc.returncode != 0:
        raise RuntimeError("javap failed for %s: %s" % (class_name, proc.stderr))
    members = set()
    for raw in proc.stdout.splitlines():
        line = raw.strip()
        if not line or line.startswith("Compiled from") or line in ("{", "}"):
            continue
        if line.startswith("public class ") or line.startswith("public interface ") \
                or line.startswith("public abstract class ") or line.startswith("public enum "):
            continue
        if line.startswith("public ") or line.startswith("protected "):
            members.add(re.sub(r"\s+", " ", line))
    return members

missing_total = []
for entry in changed_top:
    class_name = entry[:-6].replace("/", ".")
    before = public_surface(BASE, class_name)
    after = public_surface(CAND, class_name)
    missing = sorted(before-after)
    added = sorted(after-before)
    print(class_name, "missing", len(missing), "added", len(added))
    for item in missing:
        print("  MISSING", item)
    missing_total.extend((class_name, item) for item in missing)

if missing_total:
    raise RuntimeError("Public ABI removals detected: %r" % missing_total)
print("ALLOY_KILN_PUBLIC_ABI_PASS")
