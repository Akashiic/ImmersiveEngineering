#!/usr/bin/env python3
"""Prove the 1.7 Auto Workbench facing/mirror transform equivalence."""

from __future__ import print_function

import math
from pathlib import Path

FACINGS = {
    2: ("north", 0, (0, 0, -1)),
    3: ("south", 180, (0, 0, 1)),
    4: ("west", 90, (-1, 0, 0)),
    5: ("east", -90, (1, 0, 0)),
}


def mm(a, b):
    return [[sum(a[r][k]*b[k][c] for k in range(3)) for c in range(3)] for r in range(3)]


def mv(a, v):
    return tuple(sum(a[r][k]*v[k] for k in range(3)) for r in range(3))


def close(a, b, eps=1e-9):
    if isinstance(a, tuple):
        return all(abs(x-y)<eps for x, y in zip(a, b))
    return all(abs(a[r][c]-b[r][c])<eps for r in range(3) for c in range(3))


def ry(degrees):
    angle = math.radians(degrees)
    c, s = math.cos(angle), math.sin(angle)
    return [[c, 0, s], [0, 1, 0], [-s, 0, c]]


mx = [[-1, 0, 0], [0, 1, 0], [0, 0, 1]]
mz = [[1, 0, 0], [0, 1, 0], [0, 0, -1]]
for facing, data in FACINGS.items():
    name, degrees, direction = data
    rotation = ry(degrees)
    world_mirror = mx if direction[0] == 0 else mz
    assert close(mm(world_mirror, rotation), mm(rotation, mx)), name+" mirror mismatch"

    drill = mv(rotation, (0, 0, -.9375))
    expected_drill = (-.9375 if facing == 4 else .9375 if facing == 5 else 0,
                      0,
                      -.9375 if facing == 2 else .9375 if facing == 3 else 0)
    assert close(drill, expected_drill), name+" drill offset mismatch"

    press_axis = mv(rotation, (1, 0, 0))
    expected_axis = (-direction[2], 0, direction[0])
    assert close(press_axis, expected_axis), name+" press axis mismatch"
    print("%s: angle=%s mirror=PASS drill=%s press_axis=%s" %
          (name, degrees, tuple(round(x, 6) for x in drill),
           tuple(round(x, 6) for x in press_axis)))

root = Path(__file__).resolve().parents[1]
model_path = root / "src/main/resources/assets/immersiveengineering/models/autoWorkbench.obj"
vertices = []
for line in model_path.read_text(encoding="utf-8").splitlines():
    if line.startswith("v "):
        vertices.append(tuple(float(value) for value in line.split()[1:4]))
assert vertices, "static OBJ has no vertices"
mins = tuple(min(v[i] for v in vertices)-.5 for i in range(3))
maxs = tuple(max(v[i] for v in vertices)-.5 for i in range(3))
assert abs(mins[0]+1.500001)<1e-6 and abs(maxs[0]-1.5)<1e-6
assert abs(mins[2]+1.5)<1e-6 and abs(maxs[2]-1.5)<1e-6
assert abs(mins[1]+1.5)<1e-6
print("manual formed bbox after translation: min=%s max=%s" % (mins, maxs))

manual_rotations = {13: 180, 14: 180, 15: 180, 16: 90}
expected = {13: (0, 0, 1), 14: (0, 0, 1), 15: (0, 0, 1), 16: (-1, 0, 0)}
for iterator, degrees in manual_rotations.items():
    result = mv(ry(degrees), (0, 0, -1))
    assert close(result, expected[iterator]), "manual conveyor %d mismatch" % iterator
print("manual conveyor orientation: PASS")
print("AUTO_WORKBENCH_TRANSFORM_GATE=PASS")
