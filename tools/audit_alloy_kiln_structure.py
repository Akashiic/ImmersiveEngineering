from pathlib import Path
import json

RIGHT = {2: 4, 3: 5, 4: 3, 5: 2}
OPPOSITE = {2: 3, 3: 2, 4: 5, 5: 4}
VEC = {2: (0, -1), 3: (0, 1), 4: (-1, 0), 5: (1, 0)}

def part_local(pos):
    return ((pos//4)&1, (pos//2)&1, pos&1)  # height, depth, width

def coords(facing, pos):
    h, depth, width = part_local(pos)
    bx, bz = VEC[OPPOSITE[facing]]
    rx, rz = VEC[RIGHT[facing]]
    return (bx*depth+rx*width, h, bz*depth+rz*width)

def quadrant(col, row):
    return row*2+col

def face_quadrant(facing, pos, side):
    h, depth, width = part_local(pos)
    row = 1-h
    if side == 0:
        return "bottom", quadrant(width, depth)
    if side == 1:
        return "top", quadrant(1-width, depth)
    if side == facing:
        return "front", quadrant(width, row)
    if side == OPPOSITE[facing]:
        return "back", quadrant(1-width, row)
    if side == RIGHT[facing]:
        return "right", quadrant(depth, row)
    return "left", quadrant(1-depth, row)
report = {}
for facing in (2, 3, 4, 5):
    points = [coords(facing, pos) for pos in range(8)]
    assert len(set(points)) == 8, (facing, points)
    assert coords(facing, 0) == (0, 0, 0)

    by_face = {}
    external = {
        "bottom": [p for p in range(8) if part_local(p)[0] == 0],
        "top": [p for p in range(8) if part_local(p)[0] == 1],
        "front": [p for p in range(8) if part_local(p)[1] == 0],
        "back": [p for p in range(8) if part_local(p)[1] == 1],
        "left": [p for p in range(8) if part_local(p)[2] == 0],
        "right": [p for p in range(8) if part_local(p)[2] == 1],
    }
    sides = {"bottom": 0, "top": 1, "front": facing,
             "back": OPPOSITE[facing], "right": RIGHT[facing],
             "left": OPPOSITE[RIGHT[facing]]}
    for name, parts in external.items():
        quads = [face_quadrant(facing, p, sides[name])[1] for p in parts]
        assert sorted(quads) == [0, 1, 2, 3], (facing, name, quads)
        by_face[name] = quads

    report[str(facing)] = {"coords": points, "quadrants": by_face}
out = Path(__file__).resolve().parents[1] / "docs/engineering/ALLOY-KILN-STRUCTURE-AUDIT.json"
out.write_text(json.dumps(report, indent=2, sort_keys=True), encoding="utf-8")
print("ALLOY_KILN_STRUCTURE_AUDIT_PASS")
for facing, data in report.items():
    print("facing", facing, "coords", data["coords"])
print("wrote", out)
