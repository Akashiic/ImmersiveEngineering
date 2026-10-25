from pathlib import Path
p=Path(r'C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport\src\main\java\blusunrize\immersiveengineering\api\tool\BelljarHandler.java')
s=p.read_text()

def once(old,new,label):
    global s
    c=s.count(old)
    if c!=1: raise RuntimeError(f'{label}: {c}')
    s=s.replace(old,new,1)

once('private static final List<ItemFertilizerHandler> itemFertilizers = new ArrayList<ItemFertilizerHandler>();\n',
     'private static final List<ItemFertilizerHandler> itemFertilizers = new ArrayList<ItemFertilizerHandler>();\n\tprivate static final List<SoilHandler> soilHandlers = new ArrayList<SoilHandler>();\n', 'soil field')

anchor='\tpublic static void registerBasicItemFertilizer(final ItemStack fertilizer, final float multiplier)\n'
insert='''\t/**\n\t * Registers a server-safe soil extension. This is intentionally independent\n\t * from client rendering and allows compat modules to provide dirt substitutes\n\t * and evidence-backed growth modifiers.\n\t */\n\tpublic static void registerSoilHandler(SoilHandler handler)\n\t{\n\t\tif(handler!=null && !soilHandlers.contains(handler))\n\t\t\tsoilHandlers.add(handler);\n\t}\n\n\tpublic static boolean isDirtSoil(ItemStack soil)\n\t{\n\t\tif(soil==null)\n\t\t\treturn false;\n\t\tif(OreDictionary.itemMatches(new ItemStack(Blocks.dirt), soil, false))\n\t\t\treturn true;\n\t\tfor(SoilHandler handler : soilHandlers)\n\t\t\tif(handler.isDirtReplacement(soil))\n\t\t\t\treturn true;\n\t\treturn false;\n\t}\n\n\tpublic static float getSoilGrowthMultiplier(ItemStack seed, ItemStack soil, TileEntity tile)\n\t{\n\t\tfloat multiplier = 1f;\n\t\tfor(SoilHandler handler : soilHandlers)\n\t\t\tif(handler.isValid(soil))\n\t\t\t\tmultiplier *= Math.max(0f, handler.getGrowthMultiplier(seed, soil, tile));\n\t\treturn multiplier;\n\t}\n\n'''
once(anchor,insert+anchor,'soil methods')

anchor2='\tpublic interface ItemFertilizerHandler\n\t{\n\t\tboolean isValid(ItemStack fertilizer);\n\n\t\tfloat getGrowthMultiplier(ItemStack fertilizer, ItemStack seed, ItemStack soil, TileEntity tile);\n\t}\n'
replace2=anchor2+'''\n\tpublic interface SoilHandler\n\t{\n\t\tboolean isValid(ItemStack soil);\n\n\t\tboolean isDirtReplacement(ItemStack soil);\n\n\t\tfloat getGrowthMultiplier(ItemStack seed, ItemStack soil, TileEntity tile);\n\t}\n'''
once(anchor2,replace2,'soil interface')

anchor3='\tprivate static SoilMatcher exactSoil(final ItemStack required)\n'
insert3='''\tprivate static SoilMatcher dirtSoil()\n\t{\n\t\treturn new SoilMatcher()\n\t\t{\n\t\t\t@Override\n\t\t\tpublic boolean matches(ItemStack soil)\n\t\t\t{\n\t\t\t\treturn isDirtSoil(soil);\n\t\t\t}\n\t\t};\n\t}\n\n'''
once(anchor3,insert3+anchor3,'dirt matcher')
s=s.replace('exactSoil(new ItemStack(Blocks.dirt))', 'dirtSoil()')
p.write_text(s)
print('Belljar soil API patched')