from pathlib import Path

repo = Path(r'C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport')

def read(rel):
    return (repo/rel).read_text(encoding='utf-8')

def write(rel, text):
    path = repo/rel
    text = text.replace('\r\n', '\n').replace('\r', '\n')
    path.write_text('\r\n'.join(text.split('\n')), encoding='utf-8', newline='')

def replace_once(text, old, new, label):
    count = text.count(old)
    if count != 1:
        raise RuntimeError(f'{label}: expected exactly 1 occurrence, found {count}')
    return text.replace(old, new, 1)

# Config
rel = 'src/main/java/blusunrize/immersiveengineering/common/Config.java'
s = read(rel)
s = replace_once(s,
    'import blusunrize.immersiveengineering.api.IEApi;\n',
    'import blusunrize.immersiveengineering.api.IEApi;\nimport blusunrize.immersiveengineering.api.tool.BelljarHandler;\n',
    'Config import')
s = replace_once(s,
    '\t\tsetInt("preheater_consumption", config.get("machines", "BlastFurnacePreheater: Charge", 32, "The RF per tick the Blast Furnace Preheater will consume to speed up the Blast Furnace").getInt());\n',
    '\t\tsetInt("preheater_consumption", config.get("machines", "BlastFurnacePreheater: Charge", 32, "The RF per tick the Blast Furnace Preheater will consume to speed up the Blast Furnace").getInt());\n'
    '\t\tsetInt("gardenCloche_consumption", config.get("machines", "Garden Cloche: Consumed", 8, "The RF per tick consumed by the Garden Cloche while growing plants").getInt());\n'
    '\t\tsetInt("gardenCloche_fertilizerDuration", config.get("machines", "Garden Cloche: Fertilizer Duration", 6000, "The amount of ticks one dose of fertilizer lasts in the Garden Cloche").getInt());\n'
    '\t\tsetInt("gardenCloche_fluidPerDose", config.get("machines", "Garden Cloche: Fluid per Dose", 250, "The amount of fertilizer fluid consumed per Garden Cloche dose in mB").getInt());\n'
    '\t\tsetDouble("gardenCloche_growthModifier", config.get("machines", "Garden Cloche: Growth Modifier", 1d, "Global modifier applied to Garden Cloche growth speed").getDouble());\n'
    '\t\tsetDouble("gardenCloche_solidFertilizerModifier", config.get("machines", "Garden Cloche: Solid Fertilizer Modifier", 1d, "Base modifier applied to solid Garden Cloche fertilizers").getDouble());\n'
    '\t\tsetDouble("gardenCloche_fluidFertilizerModifier", config.get("machines", "Garden Cloche: Fluid Fertilizer Modifier", 1d, "Base modifier applied to fluid Garden Cloche fertilizers").getDouble());\n'
    '\t\tBelljarHandler.solidFertilizerModifier = (float)getDouble("gardenCloche_solidFertilizerModifier");\n'
    '\t\tBelljarHandler.fluidFertilizerModifier = (float)getDouble("gardenCloche_fluidFertilizerModifier");\n',
    'Config machine settings')
write(rel, s)

# GUI id
rel = 'src/main/java/blusunrize/immersiveengineering/common/util/Lib.java'
s = read(rel)
s = replace_once(s,
    '\tpublic static final int GUIID_Toolbox = 12;\n',
    '\tpublic static final int GUIID_Toolbox = 12;\n\tpublic static final int GUIID_GardenCloche = 13;\n',
    'Lib GUI id')
write(rel, s)

# Block lifecycle
rel = 'src/main/java/blusunrize/immersiveengineering/common/blocks/metal/BlockMetalDevices2.java'
s = read(rel)
s = replace_once(s,
    'import blusunrize.immersiveengineering.api.AdvancedAABB;\n',
    'import blusunrize.immersiveengineering.ImmersiveEngineering;\nimport blusunrize.immersiveengineering.api.AdvancedAABB;\n',
    'Block import')
s = replace_once(s,
    '\tpublic static final int META_blastFurnacePreheater = 11;\n',
    '\tpublic static final int META_blastFurnacePreheater = 11;\n\tpublic static final int META_gardenCloche = 12;\n',
    'Block metadata')
s = replace_once(s,
    '"breakerSwitch","skycrateDispenser","energyMeter","electricLantern","floodlight","fluidPipe", "fluidPump", "barrel", "capacitorCreative", "redstoneBreaker","chargingStation","blastFurnacePreheater");',
    '"breakerSwitch","skycrateDispenser","energyMeter","electricLantern","floodlight","fluidPipe", "fluidPump", "barrel", "capacitorCreative", "redstoneBreaker","chargingStation","blastFurnacePreheater","gardenCloche");',
    'Block subnames')
s = replace_once(s,
    '\t\tlist.add(new ItemStack(item, 1,11));\n',
    '\t\tlist.add(new ItemStack(item, 1,11));\n\t\tlist.add(new ItemStack(item, 1,12));\n',
    'Creative subblock')
s = replace_once(s,
    '\t\telse if(te instanceof TileEntityChargingStation)\n\t\t{\n',
    '\t\telse if(te instanceof TileEntityGardenCloche && !player.isSneaking())\n'
    '\t\t{\n'
    '\t\t\tTileEntityGardenCloche cloche = ((TileEntityGardenCloche)te).getMaster();\n'
    '\t\t\tif(cloche!=null)\n'
    '\t\t\t{\n'
    '\t\t\t\tif(!world.isRemote)\n'
    '\t\t\t\t\tplayer.openGui(ImmersiveEngineering.instance, Lib.GUIID_GardenCloche, world, cloche.xCoord, cloche.yCoord, cloche.zCoord);\n'
    '\t\t\t\treturn true;\n'
    '\t\t\t}\n'
    '\t\t}\n'
    '\t\telse if(te instanceof TileEntityChargingStation)\n\t\t{\n',
    'Block activation')
s = replace_once(s,
    '\t\tcase META_blastFurnacePreheater:\n\t\t\treturn new TileEntityBlastFurnacePreheater();\n',
    '\t\tcase META_blastFurnacePreheater:\n\t\t\treturn new TileEntityBlastFurnacePreheater();\n'
    '\t\tcase META_gardenCloche:\n\t\t\treturn new TileEntityGardenCloche();\n',
    'Tile factory')
s = replace_once(s,
    '\t\tif(te instanceof TileEntityEnergyMeter)\n',
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
    '\t\t}\n'
    '\t\tif(te instanceof TileEntityEnergyMeter)\n',
    'Cloche break lifecycle')
s = replace_once(s,
    '\t@Override\n\tpublic void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player)\n\t{\n\t\tTileEntity te = world.getTileEntity(x, y, z);\n',
    '\tprivate static void spawnClocheDrop(World world, int x, int y, int z, ItemStack stack)\n'
    '\t{\n'
    '\t\tif(stack==null || stack.stackSize<=0)\n'
    '\t\t\treturn;\n'
    '\t\tEntityItem entity = new EntityItem(world, x+.5, y+.5, z+.5, stack.copy());\n'
    '\t\tentity.motionX = world.rand.nextGaussian()*.05;\n'
    '\t\tentity.motionY = world.rand.nextGaussian()*.05+.2;\n'
    '\t\tentity.motionZ = world.rand.nextGaussian()*.05;\n'
    '\t\tworld.spawnEntityInWorld(entity);\n'
    '\t}\n\n'
    '\t@Override\n\tpublic void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player)\n\t{\n\t\tTileEntity te = world.getTileEntity(x, y, z);\n'
    '\t\tif(meta==META_gardenCloche && player.capabilities.isCreativeMode && te instanceof TileEntityGardenCloche)\n'
    '\t\t{\n'
    '\t\t\tTileEntityGardenCloche master = ((TileEntityGardenCloche)te).getMaster();\n'
    '\t\t\tif(master!=null)\n'
    '\t\t\t\tmaster.suppressMachineDrop = true;\n'
    '\t\t}\n',
    'Creative suppress / drop helper')
s = replace_once(s,
    '\t\tif(metadata==META_barrel)\n\t\t\treturn ret;\n',
    '\t\tif(metadata==META_barrel || metadata==META_gardenCloche)\n\t\t\treturn ret;\n',
    'Cloche getDrops')
s = replace_once(s,
    '\t\telse if(te instanceof TileEntityChargingStation)\n\t\t{\n\t\t\tint f = ((TileEntityChargingStation)te).facing;\n',
    '\t\telse if(te instanceof TileEntityGardenCloche)\n'
    '\t\t\tthis.setBlockBounds(.0625f,0,.0625f, .9375f,1,.9375f);\n'
    '\t\telse if(te instanceof TileEntityChargingStation)\n\t\t{\n\t\t\tint f = ((TileEntityChargingStation)te).facing;\n',
    'Cloche bounds')
write(rel, s)

# ItemBlock placement
rel = 'src/main/java/blusunrize/immersiveengineering/common/blocks/metal/ItemBlockMetalDevices2.java'
s = read(rel)
s = replace_once(s,
    '\t\tif(meta==BlockMetalDevices2.META_blastFurnacePreheater)\n\t\t\tif(!world.isAirBlock(x, y+1, z) || !world.isAirBlock(x, y+2, z))\n\t\t\t\treturn false;\n',
    '\t\tif(meta==BlockMetalDevices2.META_blastFurnacePreheater || meta==BlockMetalDevices2.META_gardenCloche)\n\t\t\tif(!world.isAirBlock(x, y+1, z) || !world.isAirBlock(x, y+2, z))\n\t\t\t\treturn false;\n',
    'Cloche placement clearance')
s = replace_once(s,
    '\t\telse if(tileEntity instanceof TileEntityBlastFurnacePreheater)\n\t\t{\n',
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
    '\t\t}\n'
    '\t\telse if(tileEntity instanceof TileEntityBlastFurnacePreheater)\n\t\t{\n',
    'Cloche placement dummies')
write(rel, s)

# IEContent registration + handler init
rel = 'src/main/java/blusunrize/immersiveengineering/common/IEContent.java'
s = read(rel)
s = replace_once(s,
    'import blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;\n',
    'import blusunrize.immersiveengineering.api.tool.BelljarHandler;\nimport blusunrize.immersiveengineering.api.tool.ExternalHeaterHandler;\n',
    'IEContent handler import')
s = replace_once(s,
    '\t\tregisterTile(TileEntityBlastFurnacePreheater.class);\n',
    '\t\tregisterTile(TileEntityBlastFurnacePreheater.class);\n\t\tregisterTile(TileEntityGardenCloche.class);\n',
    'IEContent tile register')
s = replace_once(s,
    '\tpublic static void postInit()\n\t{\n\t\tIERecipes.postInitCrusherAndArcRecipes();\n',
    '\tpublic static void postInit()\n\t{\n\t\tBelljarHandler.init();\n\t\tIERecipes.postInitCrusherAndArcRecipes();\n',
    'IEContent handler init')
write(rel, s)

print('Lifecycle patch applied successfully')