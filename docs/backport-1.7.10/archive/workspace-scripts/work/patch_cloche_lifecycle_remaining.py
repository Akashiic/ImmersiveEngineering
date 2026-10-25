from pathlib import Path

repo = Path(r'C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport')

def read(rel):
    return (repo/rel).read_text(encoding='utf-8')

def write(rel, text):
    text = text.replace('\r\n', '\n').replace('\r', '\n')
    (repo/rel).write_text('\r\n'.join(text.split('\n')), encoding='utf-8', newline='')

def once(text, old, new, label):
    c = text.count(old)
    if c != 1:
        raise RuntimeError(f'{label}: expected 1, found {c}')
    return text.replace(old, new, 1)

# BlockMetalDevices2
rel = 'src/main/java/blusunrize/immersiveengineering/common/blocks/metal/BlockMetalDevices2.java'
s = read(rel)
s = once(s, 'import blusunrize.immersiveengineering.api.AdvancedAABB;\n',
         'import blusunrize.immersiveengineering.ImmersiveEngineering;\nimport blusunrize.immersiveengineering.api.AdvancedAABB;\n', 'block import')
s = once(s, '\tpublic static final int META_blastFurnacePreheater = 11;\n',
         '\tpublic static final int META_blastFurnacePreheater = 11;\n\tpublic static final int META_gardenCloche = 12;\n', 'meta')
s = once(s,
         '"breakerSwitch","skycrateDispenser","energyMeter","electricLantern","floodlight","fluidPipe", "fluidPump", "barrel", "capacitorCreative", "redstoneBreaker","chargingStation","blastFurnacePreheater");',
         '"breakerSwitch","skycrateDispenser","energyMeter","electricLantern","floodlight","fluidPipe", "fluidPump", "barrel", "capacitorCreative", "redstoneBreaker","chargingStation","blastFurnacePreheater","gardenCloche");', 'subnames')
s = once(s, '\t\tlist.add(new ItemStack(item, 1,11));\n',
         '\t\tlist.add(new ItemStack(item, 1,11));\n\t\tlist.add(new ItemStack(item, 1,12));\n', 'creative')

# Activation: insert before the first charging-station branch inside onBlockActivated
method = s.index('\tpublic boolean onBlockActivated(')
branch = s.index('\t\telse if(te instanceof TileEntityChargingStation)', method)
activation = (
    '\t\telse if(te instanceof TileEntityGardenCloche && !player.isSneaking())\n'
    '\t\t{\n'
    '\t\t\tTileEntityGardenCloche cloche = ((TileEntityGardenCloche)te).getMaster();\n'
    '\t\t\tif(cloche!=null)\n'
    '\t\t\t{\n'
    '\t\t\t\tif(!world.isRemote)\n'
    '\t\t\t\t\tplayer.openGui(ImmersiveEngineering.instance, Lib.GUIID_GardenCloche, world, cloche.xCoord, cloche.yCoord, cloche.zCoord);\n'
    '\t\t\t\treturn true;\n'
    '\t\t\t}\n'
    '\t\t}\n')
s = s[:branch] + activation + s[branch:]

s = once(s,
         '\t\tcase META_blastFurnacePreheater:\n\t\t\treturn new TileEntityBlastFurnacePreheater();\n',
         '\t\tcase META_blastFurnacePreheater:\n\t\t\treturn new TileEntityBlastFurnacePreheater();\n\t\tcase META_gardenCloche:\n\t\t\treturn new TileEntityGardenCloche();\n', 'tile factory')

# Break lifecycle: insert directly after TileEntity lookup in breakBlock
break_sig = '\tpublic void breakBlock(World world, int x, int y, int z, Block par5, int par6)\n\t{\n\t\tTileEntity te = world.getTileEntity(x, y, z);\n'
break_code = break_sig + (
    '\t\tif(te instanceof TileEntityGardenCloche)\n'
    '\t\t{\n'
    '\t\t\tTileEntityGardenCloche part = (TileEntityGardenCloche)te;\n'
    '\t\t\tTileEntityGardenCloche master = part.getMaster();\n'
    '\t\t\tif(master==null)\n'
    '\t\t\t\tmaster = part;\n'
    '\t\t\tif(!master.breaking)\n'
    '\t\t\t{\n'
    '\t\t\t\tmaster.breaking = true;\n'
    '\t\t\t\tint baseY = y-part.dummy;\n'
    '\t\t\t\tif(!world.isRemote && world.getGameRules().getGameRuleBooleanValue("doTileDrops"))\n'
    '\t\t\t\t{\n'
    '\t\t\t\t\tif(!master.suppressMachineDrop)\n'
    '\t\t\t\t\t\tspawnClocheDrop(world, x, baseY, z, new ItemStack(this, 1, META_gardenCloche));\n'
    '\t\t\t\t\tfor(ItemStack stack : master.getInventoryDrops())\n'
    '\t\t\t\t\t\tspawnClocheDrop(world, x, baseY, z, stack);\n'
    '\t\t\t\t}\n'
    '\t\t\t\tmaster.clearInventory();\n'
    '\t\t\t\tfor(int i=0; i<3; i++)\n'
    '\t\t\t\t\tif(baseY+i!=y)\n'
    '\t\t\t\t\t{\n'
    '\t\t\t\t\t\tTileEntity other = world.getTileEntity(x, baseY+i, z);\n'
    '\t\t\t\t\t\tif(other instanceof TileEntityGardenCloche)\n'
    '\t\t\t\t\t\t{\n'
    '\t\t\t\t\t\t\t((TileEntityGardenCloche)other).breaking = true;\n'
    '\t\t\t\t\t\t\tworld.setBlockToAir(x, baseY+i, z);\n'
    '\t\t\t\t\t\t}\n'
    '\t\t\t\t\t}\n'
    '\t\t\t}\n'
    '\t\t}\n')
s = once(s, break_sig, break_code, 'break lifecycle')

# Drop helper + creative suppression
harvest_sig = '\t@Override\n\tpublic void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player)\n\t{\n\t\tTileEntity te = world.getTileEntity(x, y, z);\n'
harvest_code = (
    '\tprivate static void spawnClocheDrop(World world, int x, int y, int z, ItemStack stack)\n'
    '\t{\n'
    '\t\tif(stack==null || stack.stackSize<=0)\n'
    '\t\t\treturn;\n'
    '\t\tEntityItem entity = new EntityItem(world, x+.5, y+.5, z+.5, stack.copy());\n'
    '\t\tentity.motionX = world.rand.nextGaussian()*.05;\n'
    '\t\tentity.motionY = world.rand.nextGaussian()*.05+.2;\n'
    '\t\tentity.motionZ = world.rand.nextGaussian()*.05;\n'
    '\t\tworld.spawnEntityInWorld(entity);\n'
    '\t}\n\n' + harvest_sig +
    '\t\tif(meta==META_gardenCloche && player.capabilities.isCreativeMode && te instanceof TileEntityGardenCloche)\n'
    '\t\t{\n'
    '\t\t\tTileEntityGardenCloche master = ((TileEntityGardenCloche)te).getMaster();\n'
    '\t\t\tif(master!=null)\n'
    '\t\t\t\tmaster.suppressMachineDrop = true;\n'
    '\t\t}\n')
s = once(s, harvest_sig, harvest_code, 'harvest')
s = once(s, '\t\tif(metadata==META_barrel)\n\t\t\treturn ret;\n',
         '\t\tif(metadata==META_barrel || metadata==META_gardenCloche)\n\t\t\treturn ret;\n', 'getDrops')

# Bounds: insert before first charging station branch after setBlockBoundsBasedOnState signature
bounds_method = s.index('\tpublic void setBlockBoundsBasedOnState(')
bounds_branch = s.index('\t\telse if(te instanceof TileEntityChargingStation)', bounds_method)
s = s[:bounds_branch] + '\t\telse if(te instanceof TileEntityGardenCloche)\n\t\t\tthis.setBlockBounds(.0625f,0,.0625f, .9375f,1,.9375f);\n' + s[bounds_branch:]
write(rel, s)

# ItemBlock placement
rel = 'src/main/java/blusunrize/immersiveengineering/common/blocks/metal/ItemBlockMetalDevices2.java'
s = read(rel)
s = once(s,
         '\t\tif(meta==BlockMetalDevices2.META_blastFurnacePreheater)\n\t\t\tif(!world.isAirBlock(x, y+1, z) || !world.isAirBlock(x, y+2, z))\n\t\t\t\treturn false;\n',
         '\t\tif(meta==BlockMetalDevices2.META_blastFurnacePreheater || meta==BlockMetalDevices2.META_gardenCloche)\n\t\t\tif(!world.isAirBlock(x, y+1, z) || !world.isAirBlock(x, y+2, z))\n\t\t\t\treturn false;\n', 'placement clearance')
preheater = '\t\telse if(tileEntity instanceof TileEntityBlastFurnacePreheater)\n\t\t{\n'
cloche = (
    '\t\telse if(tileEntity instanceof TileEntityGardenCloche)\n'
    '\t\t{\n'
    '\t\t\tTileEntityGardenCloche master = (TileEntityGardenCloche)tileEntity;\n'
    '\t\t\tmaster.facing = f;\n'
    '\t\t\tmaster.dummy = 0;\n'
    '\t\t\tfor(int i=1; i<=2; i++)\n'
    '\t\t\t{\n'
    '\t\t\t\tworld.setBlock(x, y+i, z, IEContent.blockMetalDevice2, BlockMetalDevices2.META_gardenCloche, 3);\n'
    '\t\t\t\tTileEntity other = world.getTileEntity(x, y+i, z);\n'
    '\t\t\t\tif(other instanceof TileEntityGardenCloche)\n'
    '\t\t\t\t{\n'
    '\t\t\t\t\t((TileEntityGardenCloche)other).dummy = i;\n'
    '\t\t\t\t\t((TileEntityGardenCloche)other).facing = f;\n'
    '\t\t\t\t\tother.markDirty();\n'
    '\t\t\t\t}\n'
    '\t\t\t}\n'
    '\t\t\tmaster.markDirty();\n'
    '\t\t}\n')
s = once(s, preheater, cloche+preheater, 'dummy placement')
write(rel, s)

# IEContent
rel = 'src/main/java/blusunrize/immersiveengineering/common/IEContent.java'
s = read(rel)
s = once(s, 'import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;\n',
         'import blusunrize.immersiveengineering.api.tool.BelljarHandler;\nimport blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;\n', 'IEContent import')
s = once(s, '\t\tregisterTile(TileEntityBlastFurnacePreheater.class);\n',
         '\t\tregisterTile(TileEntityBlastFurnacePreheater.class);\n\t\tregisterTile(TileEntityGardenCloche.class);\n', 'tile register')
s = once(s, '\tpublic static void postInit()\n\t{\n\t\tIERecipes.postInitCrusherAndArcRecipes();\n',
         '\tpublic static void postInit()\n\t{\n\t\tBelljarHandler.init();\n\t\tIERecipes.postInitCrusherAndArcRecipes();\n', 'handler init')
write(rel, s)

print('remaining lifecycle patch applied')