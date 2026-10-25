from pathlib import Path
p=Path(r'C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport\src\main\java\blusunrize\immersiveengineering\common\IERecipes.java')
s=p.read_text(encoding='utf-8')
if 'META_gardenCloche), " L ","G G","WCW"' not in s:
    anchor='\t\taddOredictRecipe(new ItemStack(IEContent.blockMetalDevice2,1, BlockMetalDevices2.META_blastFurnacePreheater), "S ","S ","HP", \'S\',new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_sheetMetal), \'H\',new ItemStack(IEContent.blockMetalDevice,1,BlockMetalDevices.META_furnaceHeater), \'P\',new ItemStack(IEContent.blockMetalDevice2,1,BlockMetalDevices2.META_fluidPipe));'
    assert anchor in s
    recipe='\n\t\taddOredictRecipe(new ItemStack(IEContent.blockMetalDevice2,1, BlockMetalDevices2.META_gardenCloche), " L ","G G","WCW", \'L\',new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_lantern), \'G\',"blockGlass", \'W\',"plankTreatedWood", \'C\',componentIron);'
    s=s.replace(anchor,anchor+recipe,1)
p.write_text(s,encoding='utf-8',newline='\n')
print('recipe present:', 'META_gardenCloche), " L ","G G","WCW"' in s)