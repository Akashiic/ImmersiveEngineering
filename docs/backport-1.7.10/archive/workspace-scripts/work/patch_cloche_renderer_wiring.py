from pathlib import Path
repo=Path(r'C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport')

def load(rel):
    p=repo/rel; return p,p.read_text(encoding='utf-8')
def save(p,s):
    s=s.replace('\r\n','\n').replace('\r','\n')
    p.write_text('\r\n'.join(s.split('\n')),encoding='utf-8',newline='')
def once(s,a,b,label):
    c=s.count(a)
    if c!=1: raise RuntimeError(f'{label}: expected 1, found {c}')
    return s.replace(a,b,1)

p,s=load('src/main/java/blusunrize/immersiveengineering/client/render/BlockRenderMetalDevices2.java')
s=once(s,'import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPump;\n',
       'import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPump;\nimport blusunrize.immersiveengineering.common.blocks.metal.TileEntityGardenCloche;\n','render block import')
s=once(s,'\tprivate static final TileEntityEnergyMeter meter = new TileEntityEnergyMeter();\n',
       '\tprivate static final TileEntityEnergyMeter meter = new TileEntityEnergyMeter();\n\tprivate static final TileEntityGardenCloche cloche = new TileEntityGardenCloche();\n','render block static tile')
needle='''\t\t\telse if(metadata==BlockMetalDevices2.META_blastFurnacePreheater)\n\t\t\t{\n\t\t\t\tGL11.glScalef(0.4375f, 0.4375f, 0.4375f);\n\t\t\t\tGL11.glTranslatef(-.5f,-1.25f,-.5f);\n\t\t\t\tTessellator.instance.startDrawingQuads();\n\t\t\t\tClientUtils.handleStaticTileRenderer(heat);\n\t\t\t\tTessellator.instance.draw();\n\t\t\t}\n'''
repl=needle+'''\t\t\telse if(metadata==BlockMetalDevices2.META_gardenCloche)\n\t\t\t{\n\t\t\t\tGL11.glScalef(.35f, .35f, .35f);\n\t\t\t\tGL11.glTranslatef(-.5f,-1.5f,-.5f);\n\t\t\t\trenderPass = 0;\n\t\t\t\tTessellator.instance.startDrawingQuads();\n\t\t\t\tClientUtils.handleStaticTileRenderer(cloche);\n\t\t\t\tTessellator.instance.draw();\n\t\t\t\tGL11.glEnable(GL11.GL_BLEND);\n\t\t\t\tGL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);\n\t\t\t\trenderPass = 1;\n\t\t\t\tTessellator.instance.startDrawingQuads();\n\t\t\t\tClientUtils.handleStaticTileRenderer(cloche);\n\t\t\t\tTessellator.instance.draw();\n\t\t\t\tGL11.glDisable(GL11.GL_BLEND);\n\t\t\t}\n'''
s=once(s,needle,repl,'inventory renderer')
s=once(s,
       '\t\tif(renderPass==0||metadata==BlockMetalDevices2.META_floodlight||metadata==BlockMetalDevices2.META_chargingStation)\n',
       '\t\tif(renderPass==0||metadata==BlockMetalDevices2.META_floodlight||metadata==BlockMetalDevices2.META_chargingStation||metadata==BlockMetalDevices2.META_gardenCloche)\n','render pass condition')
needle='''\t\t\telse if(metadata==BlockMetalDevices2.META_blastFurnacePreheater)\n\t\t\t{\n\t\t\t\tTileEntityBlastFurnacePreheater tile = (TileEntityBlastFurnacePreheater)world.getTileEntity(x, y, z);\n\t\t\t\tif(tile.dummy==0)\n\t\t\t\t{\n\t\t\t\t\tClientUtils.handleStaticTileRenderer(tile);\n\t\t\t\t\treturn true;\n\t\t\t\t}\n\t\t\t}\n'''
repl=needle+'''\t\t\telse if(metadata==BlockMetalDevices2.META_gardenCloche)\n\t\t\t{\n\t\t\t\tnet.minecraft.tileentity.TileEntity te = world.getTileEntity(x, y, z);\n\t\t\t\tif(te instanceof TileEntityGardenCloche && ((TileEntityGardenCloche)te).dummy==0)\n\t\t\t\t{\n\t\t\t\t\tClientUtils.handleStaticTileRenderer(te);\n\t\t\t\t\treturn true;\n\t\t\t\t}\n\t\t\t}\n'''
s=once(s,needle,repl,'world renderer')
save(p,s)

p,s=load('src/main/java/blusunrize/immersiveengineering/client/ClientProxy.java')
s=once(s,'import blusunrize.immersiveengineering.client.render.TileRenderFloodlight;\n',
       'import blusunrize.immersiveengineering.client.render.TileRenderFloodlight;\nimport blusunrize.immersiveengineering.client.render.TileRenderGardenCloche;\n','client renderer import')
s=once(s,
       '\t\tClientRegistry.bindTileEntitySpecialRenderer(TileEntityBlastFurnacePreheater.class, new TileRenderBlastFurnacePreheater());\n',
       '\t\tClientRegistry.bindTileEntitySpecialRenderer(TileEntityBlastFurnacePreheater.class, new TileRenderBlastFurnacePreheater());\n\t\tClientRegistry.bindTileEntitySpecialRenderer(TileEntityGardenCloche.class, new TileRenderGardenCloche());\n','tesr registration')
save(p,s)
print('renderer wiring applied')