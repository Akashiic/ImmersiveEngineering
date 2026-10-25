from pathlib import Path
repo=Path(r'C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport')

def rw(rel):
    p=repo/rel
    return p,p.read_text(encoding='utf-8')
def save(p,s):
    s=s.replace('\r\n','\n').replace('\r','\n')
    p.write_text('\r\n'.join(s.split('\n')),encoding='utf-8',newline='')
def once(s,a,b,label):
    c=s.count(a)
    if c!=1: raise RuntimeError(f'{label}: {c}')
    return s.replace(a,b,1)

# CommonProxy
p,s=rw('src/main/java/blusunrize/immersiveengineering/common/CommonProxy.java')
s=once(s,'import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;\n',
       'import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;\nimport blusunrize.immersiveengineering.common.blocks.metal.TileEntityGardenCloche;\n','common tile import')
s=once(s,'import blusunrize.immersiveengineering.common.gui.ContainerFermenter;\n',
       'import blusunrize.immersiveengineering.common.gui.ContainerFermenter;\nimport blusunrize.immersiveengineering.common.gui.ContainerGardenCloche;\n','common container import')
s=once(s,'\t\tif(ID==Lib.GUIID_Toolbox && player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof ItemToolbox)\n\t\t\treturn new ContainerToolbox(player.inventory, world);\n',
       '\t\tif(ID==Lib.GUIID_Toolbox && player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof ItemToolbox)\n\t\t\treturn new ContainerToolbox(player.inventory, world);\n\t\tif(ID==Lib.GUIID_GardenCloche && te instanceof TileEntityGardenCloche)\n\t\t\treturn new ContainerGardenCloche(player.inventory, (TileEntityGardenCloche)te);\n','server gui map')
save(p,s)

# ClientProxy imports and client GUI map
p,s=rw('src/main/java/blusunrize/immersiveengineering/client/ClientProxy.java')
s=once(s,'import blusunrize.immersiveengineering.client.gui.GuiFermenter;\n',
       'import blusunrize.immersiveengineering.client.gui.GuiFermenter;\nimport blusunrize.immersiveengineering.client.gui.GuiGardenCloche;\n','client gui import')
s=once(s,'import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;\n',
       'import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFermenter;\nimport blusunrize.immersiveengineering.common.blocks.metal.TileEntityGardenCloche;\n','client tile import')
s=once(s,'\t\tif(ID==Lib.GUIID_Toolbox && player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof ItemToolbox)\n\t\t\treturn new GuiToolbox(player.inventory, world);\n',
       '\t\tif(ID==Lib.GUIID_Toolbox && player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof ItemToolbox)\n\t\t\treturn new GuiToolbox(player.inventory, world);\n\t\tif(ID==Lib.GUIID_GardenCloche && te instanceof TileEntityGardenCloche)\n\t\t\treturn new GuiGardenCloche(player.inventory, (TileEntityGardenCloche)te);\n','client gui map')
save(p,s)

# English localization
p,s=rw('src/main/resources/assets/immersiveengineering/lang/en_US.lang')
lines=s.splitlines()
add=[
 'tile.ImmersiveEngineering.metalDevice2.gardenCloche.name=Garden Cloche',
 'gui.ImmersiveEngineering.gardenCloche=Garden Cloche',
 'desc.ImmersiveEngineering.info.gardenClocheFertilizer=Fertilizer: %s%%',
 'desc.ImmersiveEngineering.info.gardenClocheFertilizerMod=Growth multiplier: %sx',
]
for line in add:
    if line.split('=')[0] not in s:
        lines.append(line)
s='\n'.join(lines)+'\n'
save(p,s)

print('GUI wiring applied')