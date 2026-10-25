from pathlib import Path
import shutil
root=Path(r'C:\Users\User\Desktop\Codex\immersiveengineering-backport')
up=root/'upstream/ImmersiveEngineering-cloche-1.12.2/src/main/resources/assets/immersiveengineering'
repo=root/'work/ImmersiveEngineering-Forbidden-Backport/src/main/resources/assets/immersiveengineering'
src=up/'models/block/metal_device/belljar.obj.ie'
dst=repo/'models/gardenCloche.obj'
lines=src.read_text(encoding='utf-8').splitlines()
out=[]
in_farmland=False
for line in lines:
    if line.startswith('mtllib '):
        continue
    if line == 'usemtl farmland':
        out.append('o farmland')
        in_farmland=True
        continue
    if line == 'usemtl belljar':
        if in_farmland:
            out.append('o base')
            in_farmland=False
        continue
    # Forge 1.7.10 WavefrontObject already applies v -> 1-v while parsing vt lines.
    # Keep upstream UVs untouched here; baking another flip would cancel the loader flip.
    out.append(line)
dst.parent.mkdir(parents=True,exist_ok=True)
dst.write_text('\n'.join(out)+'\n',encoding='utf-8')
tex_src=up/'textures/blocks/metal_device1_glassbell.png'
tex_dst=repo/'textures/blocks/metal2_gardenCloche.png'
shutil.copy2(tex_src,tex_dst)
print('OBJ',dst)
print('groups:', [l for l in out if l.startswith('o ')])
print('texture',tex_dst,tex_dst.stat().st_size)