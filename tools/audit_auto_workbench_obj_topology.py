from pathlib import Path

root = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport")
evidence = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\evidence\auto-workbench-runtime-fix")
pairs = [
    (evidence / "autoWorkbench-upstream-0.10-54.obj", root / "src/main/resources/assets/immersiveengineering/models/autoWorkbench.obj"),
    (evidence / "autoWorkbenchMirrored-upstream-0.10-54.obj", root / "src/main/resources/assets/immersiveengineering/models/autoWorkbenchMirrored.obj"),
]
for source, converted in pairs:
    src_faces = [x.split() for x in source.read_text(encoding="utf-8").splitlines() if x.startswith("f ")]
    dst_faces = [x.split() for x in converted.read_text(encoding="utf-8").splitlines() if x.startswith("f ")]
    assert len(src_faces) == len(dst_faces) == 338
    changed = 0
    for src, dst in zip(src_faces, dst_faces):
        expected = src + [src[-1]] if len(src) == 4 else src
        assert dst == expected, (src, dst)
        changed += dst != src
    assert changed == 4, changed
    assert {len(face)-1 for face in dst_faces} == {4}
    print(converted.name, "faces=338 quadified=4 arities={4} PASS")
animated = root / "src/main/resources/assets/immersiveengineering/models/autoWorkbenchAnimated.obj"
assert {len(x.split())-1 for x in animated.read_text(encoding="utf-8").splitlines() if x.startswith("f ")} == {4}
print("autoWorkbenchAnimated.obj arities={4} unchanged PASS")
print("AUTO_WORKBENCH_OBJ_TOPOLOGY_GATE=PASS")