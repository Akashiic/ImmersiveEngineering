from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
PATH = ROOT / "src/main/java/blusunrize/immersiveengineering/client/render/AutoWorkbenchBlueprintRenderer.java"
text = PATH.read_text(encoding="utf-8")

if "private static class BlueprintPoint" in text:
    print("BLUEPRINT_POINT_GATE=ALREADY_HARDENED")
    raise SystemExit(0)

replacements = [
    ("import java.awt.Point;\n", ""),
    ("HashMultimap<ShadeStyle, Point>", "HashMultimap<ShadeStyle, BlueprintPoint>"),
    ("for(Point pixel : areas.get(style))", "for(BlueprintPoint pixel : areas.get(style))"),
    ("private void drawShading(Point pixel)", "private void drawShading(BlueprintPoint pixel)"),
    ("private static class TexturePoint extends Point", "private static class TexturePoint extends BlueprintPoint"),
    ("private Point scaleTo(int targetScale)", "private BlueprintPoint scaleTo(int targetScale)"),
    ("return new Point((int)(x/(float)scale*targetScale), (int)(y/(float)scale*targetScale));",
     "return new BlueprintPoint((int)(x/(float)scale*targetScale), (int)(y/(float)scale*targetScale));"),
    ("private final Point first;", "private final BlueprintPoint first;"),
    ("private final Point second;", "private final BlueprintPoint second;"),
    ("private Edge(Point first, Point second)", "private Edge(BlueprintPoint first, BlueprintPoint second)"),
    ("Point scaledFirst = first instanceof TexturePoint", "BlueprintPoint scaledFirst = first instanceof TexturePoint"),
    (":new Point(first);", ":new BlueprintPoint(first);"),
    ("Point scaledSecond = second instanceof TexturePoint", "BlueprintPoint scaledSecond = second instanceof TexturePoint"),
    (":new Point(second);", ":new BlueprintPoint(second);"),
]
for old, new in replacements:
    count = text.count(old)
    if count < 1:
        raise RuntimeError("Expected source fragment missing: %r" % old)
    text = text.replace(old, new)

marker = "\tprivate static class TexturePoint extends BlueprintPoint\n"
if marker not in text:
    raise RuntimeError("TexturePoint insertion marker missing")

point_class = """\tprivate static class BlueprintPoint
\t{
\t\tprotected final int x;
\t\tprotected final int y;

\t\tprivate BlueprintPoint(int x, int y)
\t\t{
\t\t\tthis.x = x;
\t\t\tthis.y = y;
\t\t}

\t\tprivate BlueprintPoint(BlueprintPoint other)
\t\t{
\t\t\tthis(other.x, other.y);
\t\t}

"""
point_class += """\t\t@Override
\t\tpublic boolean equals(Object object)
\t\t{
\t\t\tif(this==object)
\t\t\t\treturn true;
\t\t\tif(object==null || getClass()!=object.getClass())
\t\t\t\treturn false;
\t\t\tBlueprintPoint other = (BlueprintPoint)object;
\t\t\treturn x==other.x && y==other.y;
\t\t}

\t\t@Override
\t\tpublic int hashCode()
\t\t{
\t\t\treturn 31*x+y;
\t\t}
\t}

"""

text = text.replace(marker, point_class+marker, 1)
PATH.write_text(text, encoding="utf-8")
print("BLUEPRINT_POINT_GATE=HARDENED")
print("java.awt.Point refs=%d" % text.count("java.awt.Point"))
print("BlueprintPoint refs=%d" % text.count("BlueprintPoint"))