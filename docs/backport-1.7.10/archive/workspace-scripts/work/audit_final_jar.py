from pathlib import Path
import zipfile, struct, json, collections, hashlib, re

repo=Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport")
jars=[p for p in (repo/'build/libs').glob('*.jar') if not p.name.endswith('-dev.jar') and not p.name.endswith('-sources.jar')]
assert len(jars)==1, jars
jar=jars[0]
print('JAR',jar)
print('SIZE',jar.stat().st_size)
print('SHA256',hashlib.sha256(jar.read_bytes()).hexdigest().upper())
with zipfile.ZipFile(jar) as z:
    names=z.namelist()
    print('ZIP_TEST',z.testzip())
    print('ENTRIES',len(names),'UNIQUE',len(set(names)),'DUPLICATES',len(names)-len(set(names)))
    manifest=z.read('META-INF/MANIFEST.MF').decode('utf-8','replace')
    print('FMLAT_OK', 'FMLAT: ImmersiveEngineering_at.cfg' in manifest)
    mc=z.read('mcmod.info').decode('utf-8','replace')
    print('MCMOD_VERSION_079', '1.7.10-0.7.9' in mc)
    print('MCMOD_FORBIDDEN', 'Forbidden Series Edition' in mc)
    required=[
      'blusunrize/immersiveengineering/api/tool/BelljarHandler.class',
      'blusunrize/immersiveengineering/common/blocks/metal/TileEntityGardenCloche.class',
      'blusunrize/immersiveengineering/common/gui/ContainerGardenCloche.class',
      'blusunrize/immersiveengineering/client/gui/GuiGardenCloche.class',
      'blusunrize/immersiveengineering/client/render/TileRenderGardenCloche.class',
      'assets/immersiveengineering/models/gardenCloche.obj',
      'assets/immersiveengineering/textures/blocks/metal2_gardenCloche.png',
      'assets/immersiveengineering/textures/gui/belljar.png',
      'assets/immersiveengineering/lang/en_US.lang']
    print('REQUIRED_MISSING',[n for n in required if n not in names])
    majors=collections.Counter()
    bad=[]
    for n in names:
        if n.endswith('.class'):
            b=z.read(n)
            if b[:4]!=b'\xca\xfe\xba\xbe':
                bad.append((n,'magic'))
                continue
            major=struct.unpack('>H',b[6:8])[0]
            majors[major]+=1
            if major>52: bad.append((n,major))
    print('CLASS_MAJORS',dict(sorted(majors.items())))
    print('CLASS_MAJOR_GT52',bad[:20], 'COUNT',len(bad))
    forbidden_prefixes=['codechicken/nei/','blusunrize/aquatweaks/','com/gtnewhorizons/','org/lwjgl/']
    for prefix in forbidden_prefixes:
        hits=[n for n in names if n.startswith(prefix)]
        print('SHADED',prefix,len(hits),hits[:5])
    print('TAGS_CLASS',[n for n in names if n.endswith('/Tags.class') or n=='Tags.class'])

    common_roots=[
      'blusunrize/immersiveengineering/api/tool/BelljarHandler',
      'blusunrize/immersiveengineering/common/blocks/metal/TileEntityGardenCloche',
      'blusunrize/immersiveengineering/common/gui/ContainerGardenCloche']
    needles=[b'net/minecraft/client/',b'blusunrize/immersiveengineering/client/',b'org/lwjgl/']
    for root in common_roots:
        cls=[n for n in names if n.startswith(root) and n.endswith('.class')]
        refs={needle.decode():[] for needle in needles}
        for n in cls:
            b=z.read(n)
            for needle in needles:
                if needle in b: refs[needle.decode()].append(n)
        print('COMMON_CLIENT_REFS',root,refs)
    obj=z.read('assets/immersiveengineering/models/gardenCloche.obj').decode('utf-8')
    lines=obj.splitlines(); verts=[]; uvs=[]; norms=[]; groups=[]; faces=[]; material=[]
    for line in lines:
        s=line.strip()
        if s.startswith('v '): verts.append(s)
        elif s.startswith('vt '): uvs.append(s)
        elif s.startswith('vn '): norms.append(s)
        elif s.startswith('o '): groups.append(s[2:].strip())
        elif s.startswith('f '): faces.append(s[2:].split())
        elif s.startswith('usemtl ') or s.startswith('mtllib '): material.append(s)
    invalid=[]
    for fi,face in enumerate(faces):
        for token in face:
            parts=token.split('/')
            try:
                vi=int(parts[0]); ti=int(parts[1]) if len(parts)>1 and parts[1] else None
                ni=int(parts[2]) if len(parts)>2 and parts[2] else None
            except ValueError:
                invalid.append((fi,token,'parse')); continue
            if not (1<=vi<=len(verts)): invalid.append((fi,token,'v'))
            if ti is not None and not (1<=ti<=len(uvs)): invalid.append((fi,token,'vt'))
            if ni is not None and not (1<=ni<=len(norms)): invalid.append((fi,token,'vn'))
    print('OBJ_COUNTS',len(verts),len(uvs),len(norms),len(faces),'GROUPS',groups)
    print('OBJ_MATERIAL_DIRECTIVES',material)
    print('OBJ_INVALID_INDICES',invalid[:10],'COUNT',len(invalid))

    for png in ['assets/immersiveengineering/textures/blocks/metal2_gardenCloche.png','assets/immersiveengineering/textures/gui/belljar.png']:
        b=z.read(png); assert b[:8]==b'\x89PNG\r\n\x1a\n'
        w,h=struct.unpack('>II',b[16:24])
        print('PNG',png,w,h,len(b))
