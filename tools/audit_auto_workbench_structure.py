#!/usr/bin/env python3
"""Prove Auto Workbench 1.7 structure coordinates against the 0.12 contract."""

FACINGS = {
    2: ("north", (0, 0, -1)),
    3: ("south", (0, 0, 1)),
    4: ("west", (-1, 0, 0)),
    5: ("east", (1, 0, 0)),
}
ROTATE_Y = {2: 5, 3: 4, 4: 2, 5: 3}
ROTATE_Y_CCW = {2: 4, 3: 5, 4: 3, 5: 2}
START = (100, 64, 100)


def add(a, b, scale=1):
    return tuple(a[i]+b[i]*scale for i in range(3))


def upstream_offset(side, l, w, h, mirror):
    ww = -w if mirror else w
    x = 1-l if side == 4 else l-1 if side == 5 else ww if side == 2 else -ww
    z = 1-l if side == 2 else l-1 if side == 3 else ww if side == 5 else -ww
    return (x, h, z)

for side, (name, forward) in FACINGS.items():
    right = FACINGS[ROTATE_Y[side]][1]
    for mirror in (False, True):
        master = add(START, forward)
        parts = {}
        for l in range(3):
            for w in range(-1, 2):
                for h in range(-1, 1):
                    ww = -w if mirror else w
                    world = add(add(START, forward, l), right, ww)
                    world = (world[0], world[1]+h, world[2])
                    pos = (h+1)*9+l*3+(w+1)
                    assert pos not in parts
                    parts[pos] = world
                    local_offset = tuple(world[i]-master[i] for i in range(3))
                    assert local_offset == upstream_offset(side, l, w, h, mirror)
        assert len(parts) == 18 and len(set(parts.values())) == 18
        assert parts[13] == master
        energy = parts[9]
        redstone = parts[1]
        out_side = ROTATE_Y_CCW[side] if mirror else ROTATE_Y[side]
        output = add(master, FACINGS[out_side][1], 2)
        assert energy != master and redstone != master
        assert output not in set(parts.values())
        assert sum(abs(output[i]-master[i]) for i in range(3)) == 2
        print("%s mirror=%s master=%s energy=%s redstone=%s output=%s PASS" %
              (name, mirror, master, energy, redstone, output))

print("AUTO_WORKBENCH_STRUCTURE_GATE=PASS")
