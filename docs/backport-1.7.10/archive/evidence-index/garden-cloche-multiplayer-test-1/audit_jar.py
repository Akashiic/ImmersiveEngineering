from pathlib import Path
import hashlib
import struct
import zipfile

BASE = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\dist\garden-cloche-test-2\ImmersiveEngineering-garden-cloche-test-2.jar")
NEW = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\garden-cloche-multiplayer\build\libs\ImmersiveEngineering-garden-cloche-multiplayer-test-1.jar")

def sha(path):
    h=hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda:f.read(1024*1024), b""):
            h.update(chunk)
    return h.hexdigest().upper()

def class_major(data):
    assert data[:4] == b"\xca\xfe\xba\xbe"
    return struct.unpack(">H", data[6:8])[0]

with zipfile.ZipFile(BASE) as zb, zipfile.ZipFile(NEW) as zn:
    base_names=zb.namelist(); new_names=zn.namelist()
    assert len(base_names)==len(set(base_names))
    assert len(new_names)==len(set(new_names))
    bset=set(base_names); nset=set(new_names)
    added=sorted(nset-bset); removed=sorted(bset-nset)
    common=sorted(bset&nset)
    changed=[n for n in common if zb.read(n)!=zn.read(n)]
    print("BASE_SHA256", sha(BASE))
    print("NEW_SHA256", sha(NEW))
    print("ENTRIES", len(base_names), len(new_names))
    print("ADDED", len(added), added)
    print("REMOVED", len(removed), removed)
    print("CHANGED", len(changed))
    for n in changed:
        print("*", n)

    classes=[n for n in new_names if n.endswith(".class")]
    majors={}
    for n in classes:
        m=class_major(zn.read(n)); majors[m]=majors.get(m,0)+1
    print("CLASS_MAJORS", majors)
    assert set(majors)=={52}, majors

    expected_changed={
        "blusunrize/immersiveengineering/client/gui/GuiGardenCloche.class",
        "blusunrize/immersiveengineering/common/blocks/metal/TileEntityGardenCloche.class",
        "blusunrize/immersiveengineering/common/gui/ContainerGardenCloche.class",
        "blusunrize/immersiveengineering/common/gui/ContainerGardenCloche$1.class",
        "blusunrize/immersiveengineering/common/gui/ContainerGardenCloche$2.class",
        "blusunrize/immersiveengineering/common/gui/ContainerGardenCloche$3.class",
    }
    assert not added, added
    assert not removed, removed
    unexpected=[x for x in changed if x not in expected_changed]
    print("UNEXPECTED_CHANGED", unexpected)
    assert not unexpected, unexpected
    mcmod=zn.read("mcmod.info").decode("utf-8", errors="replace")
    manifest=zn.read("META-INF/MANIFEST.MF").decode("utf-8", errors="replace")
    assert '"version": "1.7.10-0.7.9"' in mcmod
    assert "Forbidden Series Edition" in mcmod
    assert "FMLAT: ImmersiveEngineering_at.cfg" in manifest
    print("IDENTITY_GATE=PASS")

    required=[
        "blusunrize/immersiveengineering/common/blocks/metal/TileEntityGardenCloche.class",
        "blusunrize/immersiveengineering/common/gui/ContainerGardenCloche.class",
        "blusunrize/immersiveengineering/client/gui/GuiGardenCloche.class",
        "assets/immersiveengineering/models/gardenCloche.obj",
        "assets/immersiveengineering/textures/blocks/metal2_gardenCloche.png",
    ]
    for n in required:
        assert n in nset, n
    print("REQUIRED_ENTRIES_GATE=PASS")

    def common_client_refs(z):
        out=set()
        for name in z.namelist():
            if name.endswith(".class") and (name.startswith("blusunrize/immersiveengineering/common/") or name.startswith("blusunrize/immersiveengineering/api/")):
                data=z.read(name)
                if b"net/minecraft/client" in data or b"net.minecraft.client" in data:
                    out.add(name)
        return out
    old_refs=common_client_refs(zb); new_refs=common_client_refs(zn)
    print("COMMON_CLIENT_REFS", len(old_refs), len(new_refs))
    print("NEW_COMMON_CLIENT_REFS", sorted(new_refs-old_refs))
    assert not (new_refs-old_refs)
    print("GARDEN_CLOCHE_MULTIPLAYER_JAR_GATE=PASS")
