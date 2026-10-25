from pathlib import Path

p = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport\docs\engineering\ROADMAP.md")
t = p.read_text(encoding="utf-8")

replacements = [
    ("    P --> Q[AgriCraft 1.5.2 adapter]\n    Q --> R[Pack integration + performance]",
     "    P --> Q[AgriCraft 1.5.2 adapter]\n    Q --> V[Garden Cloche Test 2 runtime approval]\n    V --> W[Modern Feature Gap Audit]\n    W --> X[Feature #2 Auto Workbench]\n    X --> R[Pack integration + performance]"),
    ("Status: IN PROGRESS, high confidence ancestry established.",
     "Status: PASS — baseline ancestry and semantic recovery accepted."),
    ("## Gate G2 — Modern build parity\nStatus: IN PROGRESS.",
     "## Gate G2 — Modern build parity\nStatus: PASS — clean RFG/reobf build and baseline gate accepted."),
    ("## Gate G3 — Runtime parity\nStatus: BLOCKED by G2.",
     "## Gate G3 — Runtime parity\nStatus: ACTIVE / PARTIAL — feature-development runtime proven; full release-parity matrix remains open."),
    ("## Gate G5 — Backport API framework\nStatus: PLANNED.",
     "## Gate G5 — Backport API framework\nStatus: PARTIAL PASS — minimum framework proven by Garden Cloche; generic expansion remains ongoing."),
    ("## Gate G6 — Garden Cloche core\nStatus: PLANNED.",
     "## Gate G6 — Garden Cloche core\nStatus: PASS — `garden-cloche-test-2`, runtime approved 2026-09-09."),
    ("## Gate G7 — Magical Crops BETA 5\nStatus: PLANNED.",
     "## Gate G7 — Magical Crops BETA 5\nStatus: PASS — exact installed BETA5 adapter validated in approved Test 2."),
    ("## Gate G8 — AgriCraft 1.5.2-evernife-v1\nStatus: PLANNED.",
     "## Gate G8 — AgriCraft 1.5.2-evernife-v1\nStatus: PASS — exact evernife-v1 genetic adapter validated in approved Test 2."),
]

for old, new in replacements:
    if old not in t:
        raise RuntimeError(f"Expected roadmap fragment not found: {old[:80]!r}")
    t = t.replace(old, new, 1)

needle = "Constraint:\n- machine must not silently improve genetics.\n\n## Gate G9 — Wider ecosystem"
insert = """Constraint:
- machine must not silently improve genetics.

## Gate G8.5 — Modern Feature Gap Audit
Status: PASS — 2026-09-09.

Decision:
- genuine later-feature gaps classified semantically, not by filename alone;
- 1.12 `0.12-89` used as reproducible mature snapshot with later 1.12 fixes audited per feature;
- Automated Engineer's Workbench ranked first at 92/100;
- Alloy Kiln and Mixer ranked next at 83/100;
- high-blast-radius wire/conveyor families deferred.

Evidence: `MODERN-FEATURE-GAP-AUDIT.md`.

## Gate G8.6 — Feature #2: Automated Engineer's Workbench
Status: APPROVED FOR IMPLEMENTATION PLANNING.

Execution gates A0–A8 are defined in `AUTO-WORKBENCH-FEATURE-CHARTER.md`.
No implementation branch opens until audit/charter checkpoint is committed.

## Gate G9 — Wider ecosystem"""

if needle not in t:
    raise RuntimeError("G8/G9 insertion point not found")
t = t.replace(needle, insert, 1)

old = "## Gate G9 — Wider ecosystem\nCandidates, only after exact-version audit:"
new = "## Gate G9 — Wider ecosystem\nStatus: DEFERRED / NON-BLOCKING while Feature #2 proceeds.\n\nCandidates, only after exact-version audit:"
if old not in t:
    raise RuntimeError("G9 status insertion point not found")
t = t.replace(old, new, 1)

p.write_text(t, encoding="utf-8")
print("ROADMAP updated")