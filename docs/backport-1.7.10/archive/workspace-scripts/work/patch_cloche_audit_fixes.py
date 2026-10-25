from pathlib import Path

repo = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport")

def patch(rel, old, new):
    p = repo / rel
    b = p.read_bytes()
    nl = b"\r\n" if b"\r\n" in b else b"\n"
    old_b = old.replace("\n", nl.decode()).encode()
    new_b = new.replace("\n", nl.decode()).encode()
    count = b.count(old_b)
    assert count == 1, (rel, count)
    p.write_bytes(b.replace(old_b, new_b, 1))

patch("src/main/java/blusunrize/immersiveengineering/common/blocks/metal/ItemBlockMetalDevices2.java",
"""\t\tif(meta==BlockMetalDevices2.META_blastFurnacePreheater || meta==BlockMetalDevices2.META_gardenCloche)
\t\t\tif(!world.isAirBlock(x, y+1, z) || !world.isAirBlock(x, y+2, z))
\t\t\t\treturn false;
""",
"""\t\tif(meta==BlockMetalDevices2.META_blastFurnacePreheater || meta==BlockMetalDevices2.META_gardenCloche)
\t\t\tif(y<0 || y+2>=world.getHeight() || !world.isAirBlock(x, y+1, z) || !world.isAirBlock(x, y+2, z))
\t\t\t\treturn false;
""")

patch("src/main/java/blusunrize/immersiveengineering/common/IEContent.java",
"""\tpublic static void postInit()
\t{
\t\tBelljarHandler.init();
\t\tIERecipes.postInitCrusherAndArcRecipes();
""",
"""\tpublic static void postInit()
\t{
\t\tBelljarHandler.init();
\t\tregisterGardenClocheHemp();
\t\tIERecipes.postInitCrusherAndArcRecipes();
""")
hemp_method = """\tprivate static void registerGardenClocheHemp()
\t{
\t\tBelljarHandler.registerHandler(new BelljarHandler.PlantHandlerBase()
\t\t{
\t\t\tprivate final ItemStack hempSeed = new ItemStack(itemSeeds);

\t\t\t@Override
\t\t\tpublic boolean isValid(ItemStack seed)
\t\t\t{
\t\t\t\treturn seed!=null && OreDictionary.itemMatches(hempSeed, seed, false);
\t\t\t}

\t\t\t@Override
\t\t\tpublic boolean isCorrectSoil(ItemStack seed, ItemStack soil)
\t\t\t{
\t\t\t\treturn soil!=null && OreDictionary.itemMatches(new ItemStack(Blocks.dirt), soil, false);
\t\t\t}

\t\t\t@Override
\t\t\tpublic float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile, float fertilizer)
\t\t\t{
\t\t\t\treturn .003125f*fertilizer;
\t\t\t}

\t\t\t@Override
\t\t\tpublic ItemStack[] getOutput(ItemStack seed, ItemStack soil, TileEntity tile)
\t\t\t{
\t\t\t\treturn new ItemStack[]{new ItemStack(itemMaterial, 4, 4), new ItemStack(itemSeeds, 2)};
\t\t\t}
"""
hemp_method += """
\t\t\t@Override
\t\t\tpublic BelljarHandler.PlantRenderInfo[] getRenderInfo(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
\t\t\t{
\t\t\t\tint age = Math.min(4, Math.max(0, Math.round(growth*4f)));
\t\t\t\tif(age==4)
\t\t\t\t\treturn new BelljarHandler.PlantRenderInfo[]{
\t\t\t\t\t\tnew BelljarHandler.PlantRenderInfo(blockCrop, 4, .6875f, 0f, 0f, 0f),
\t\t\t\t\t\tnew BelljarHandler.PlantRenderInfo(blockCrop, 5, .6875f, 0f, .6875f, 0f)};
\t\t\t\treturn new BelljarHandler.PlantRenderInfo[]{new BelljarHandler.PlantRenderInfo(blockCrop, age, .6875f, 0f, 0f, 0f)};
\t\t\t}
\t\t});
\t}

"""

patch("src/main/java/blusunrize/immersiveengineering/common/IEContent.java",
"""\tpublic static void registerToOreDict(String type, ItemIEBase item, int... metas)
""",
hemp_method + """\tpublic static void registerToOreDict(String type, ItemIEBase item, int... metas)
""")

print("audit fixes applied")
