from pathlib import Path
p = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\evidence\auto-workbench-runtime-fix\audit_r2_zip_delta.py")
t = p.read_text(encoding="utf-8")
needle = "(old_added, old_removed, old_changed)base_added"
assert t.count(needle) == 1, t.count(needle)
p.write_text(t.replace(needle, "(old_added, old_removed, old_changed)\nbase_added"), encoding="utf-8")
print("AUDIT_R2_BOUNDARY_FIX=PASS")