from pathlib import Path
p=Path(r'C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport\src\main\java\blusunrize\immersiveengineering\common\blocks\metal\TileEntityGardenCloche.java')
s=p.read_text(encoding='utf-8')
needle='\tpublic ResourceLocation getSoilTexture()\n\t{'
assert needle in s
assert 'receiveContainerUpdate' not in s
insert='''\tpublic void receiveContainerUpdate(int id, int value)\n\t{\n\t\tswitch(id)\n\t\t{\n\t\tcase 0:\n\t\t\tenergyStorage.setEnergyStored(value);\n\t\t\tbreak;\n\t\tcase 1:\n\t\t\ttank.setFluid(value<=0?null:new FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER, value));\n\t\t\tbreak;\n\t\tcase 2:\n\t\t\tfertilizerAmount = value;\n\t\t\tbreak;\n\t\tcase 3:\n\t\t\tfertilizerMod = value/1000f;\n\t\t\tbreak;\n\t\tcase 4:\n\t\t\tgrowth = value/10000f;\n\t\t\trenderGrowth = growth;\n\t\t\tbreak;\n\t\tcase 5:\n\t\t\trenderActive = value!=0;\n\t\t\tbreak;\n\t\tdefault:\n\t\t\tbreak;\n\t\t}\n\t}\n\n'''
s=s.replace(needle,insert+needle,1)
p.write_text(s,encoding='utf-8',newline='\n')
print('inserted container sync')