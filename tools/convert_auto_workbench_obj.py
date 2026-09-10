#!/usr/bin/env python3
"""Deterministic IE Auto Workbench OBJ adapter for Forge 1.7.10.

Forge 1.7 ignores MTL directives and flips V while parsing texture
coordinates. Preserve source geometry/UV records exactly and convert
object/material boundaries into parser-visible groups.
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


def convert(lines, mode):
    output = ["# Adapted from official Immersive Engineering Auto Workbench asset.",
              "# Geometry and UV records are preserved; Forge 1.7 performs the V flip."]
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
            output.append(line)
            continue
        if line.startswith(("v ", "vt ", "vn ")):
            output.append(line)
            continue
        if line.startswith("#") or not line.strip():
            continue
        raise ValueError("Unsupported OBJ record: %r" % line)
    return output


def validate(source, converted, expected_groups):
    if geometry_records(source) != geometry_records(converted):
        raise AssertionError("Geometry/UV/face records changed during conversion")
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
    args = parser.parse_args()

    source_lines = read_lines(args.source)
    converted_lines = convert(source_lines, args.mode)
    expected = [part for part in args.expected_groups.split(",") if part]
    validate(source_lines, converted_lines, expected)
    with open(args.destination, "w", encoding="utf-8", newline="\n") as handle:
        handle.write("\n".join(converted_lines) + "\n")

    print("SOURCE_SHA256=" + sha256(args.source))
    print("OUTPUT_SHA256=" + sha256(args.destination))
    print("COUNTS=" + repr(dict(record_counts(source_lines))))
    print("GROUPS=" + ",".join(expected))
    print("OBJ_CONVERSION_GATE=PASS")


if __name__ == "__main__":
    main()
