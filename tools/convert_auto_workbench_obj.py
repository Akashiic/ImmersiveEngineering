#!/usr/bin/env python3
"""Deterministic IE Auto Workbench OBJ adapter for Forge 1.7.10.

Forge 1.7 ignores MTL directives and flips V while parsing texture
coordinates. Vertices, UVs and normals are preserved exactly. For the legacy
static GL_QUADS pipeline, triangle faces can be converted to degenerate quads
by repeating their final corner. Object/material boundaries become groups.
"""

from __future__ import print_function

import argparse
import hashlib
import re
from collections import Counter

GEOMETRY_PREFIXES = ("v ", "vt ", "vn ", "f ")


def sanitize(name):
    return re.sub(r"[^A-Za-z0-9_.]", "_", name or "default")


def sha256(path):
    digest = hashlib.sha256()
    with open(path, "rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest().upper()


def read_lines(path):
    with open(path, "r", encoding="utf-8") as handle:
        return [line.rstrip("\r\n") for line in handle]


def geometry_records(lines):
    return [line for line in lines if line.startswith(GEOMETRY_PREFIXES)]


def record_counts(lines):
    counts = Counter()
    for line in lines:
        token = line.split(None, 1)[0] if line.strip() else ""
        if token in ("v", "vt", "vn", "f"):
            counts[token] += 1
    return counts


def convert(lines, mode, quadify_triangles=False):
    output = ["# Adapted from official Immersive Engineering Auto Workbench asset.",
              "# Vertex/UV/normal records are preserved; Forge 1.7 performs the V flip."]
    current_object = "default"
    current_material = "default"
    emitted_group = None
    for line in lines:
        if line.startswith("o ") or line.startswith("g "):
            current_object = line.split(None, 1)[1].strip()
            continue
        if line.startswith("usemtl "):
            current_material = line.split(None, 1)[1].strip()
            continue
        if line.startswith("mtllib "):
            continue
        if line.startswith("f "):
            group = sanitize(current_object if mode == "object" else
                             current_object + "_" + current_material)
            if group != emitted_group:
                output.append("g " + group)
                emitted_group = group
            tokens = line.split()
            arity = len(tokens)-1
            if arity not in (3, 4):
                raise ValueError("Unsupported face arity %d: %r" % (arity, line))
            if quadify_triangles and arity == 3:
                line = line + " " + tokens[-1]
            output.append(line)
            continue
        if line.startswith(("v ", "vt ", "vn ")):
            output.append(line)
            continue
        if line.startswith("#") or not line.strip():
            continue
        raise ValueError("Unsupported OBJ record: %r" % line)
    return output


def validate(source, converted, expected_groups, quadify_triangles=False):
    source_non_faces = [line for line in geometry_records(source) if not line.startswith("f ")]
    converted_non_faces = [line for line in geometry_records(converted) if not line.startswith("f ")]
    if source_non_faces != converted_non_faces:
        raise AssertionError("Vertex/UV/normal records changed during conversion")
    source_faces = [line.split() for line in source if line.startswith("f ")]
    converted_faces = [line.split() for line in converted if line.startswith("f ")]
    if len(source_faces) != len(converted_faces):
        raise AssertionError("Face count changed during conversion")
    for source_face, converted_face in zip(source_faces, converted_faces):
        expected_face = list(source_face)
        if quadify_triangles and len(source_face)==4:
            expected_face.append(source_face[-1])
        if converted_face != expected_face:
            raise AssertionError("Unexpected face rewrite: %r -> %r" % (source_face, converted_face))
    if record_counts(source) != record_counts(converted):
        raise AssertionError("Geometry record counts differ")
    groups = [line.split(None, 1)[1] for line in converted if line.startswith("g ")]
    if groups != expected_groups:
        raise AssertionError("Unexpected groups: %r != %r" % (groups, expected_groups))
    forbidden = [line for line in converted if line.startswith(("mtllib ", "usemtl ", "o "))]
    if forbidden:
        raise AssertionError("Unsupported directives remained: %r" % forbidden[:3])


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("source")
    parser.add_argument("destination")
    parser.add_argument("--mode", choices=("material", "object"), required=True)
    parser.add_argument("--expected-groups", required=True,
                        help="Comma-separated exact output group order")
    parser.add_argument("--quadify-triangles", action="store_true",
                        help="Duplicate the final corner of triangle faces for the legacy GL_QUADS static renderer")
    args = parser.parse_args()

    source_lines = read_lines(args.source)
    converted_lines = convert(source_lines, args.mode, args.quadify_triangles)
    expected = [part for part in args.expected_groups.split(",") if part]
    validate(source_lines, converted_lines, expected, args.quadify_triangles)
    with open(args.destination, "w", encoding="utf-8", newline="\n") as handle:
        handle.write("\n".join(converted_lines) + "\n")

    print("SOURCE_SHA256=" + sha256(args.source))
    print("OUTPUT_SHA256=" + sha256(args.destination))
    source_triangles = sum(1 for line in source_lines if line.startswith("f ") and len(line.split())==4)
    print("COUNTS=" + repr(dict(record_counts(source_lines))))
    print("GROUPS=" + ",".join(expected))
    print("QUADIFIED_TRIANGLES=" + str(source_triangles if args.quadify_triangles else 0))
    print("OBJ_CONVERSION_GATE=PASS")


if __name__ == "__main__":
    main()
