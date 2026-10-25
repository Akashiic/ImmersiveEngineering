from pathlib import Path
p=Path(r'C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport\src\main\java\blusunrize\immersiveengineering\common\util\compat\IECompatModule.java')
s=p.read_text()
imp='import blusunrize.immersiveengineering.common.util.compat.magicalcrops.MagicalCropsHelper;\n'
anchor='import blusunrize.immersiveengineering.common.util.compat.mfr.MFRHelper;\n'
assert anchor in s
if imp not in s:
    s=s.replace(anchor, anchor+imp)
entry='\t\tmoduleClasses.put("magicalcrops", MagicalCropsHelper.class);\n'
anchor2='\t\tmoduleClasses.put("MineFactoryReloaded", MFRHelper.class);\n'
assert anchor2 in s
if entry not in s:
    s=s.replace(anchor2, anchor2+entry)
p.write_text(s)
print('patched', p)
