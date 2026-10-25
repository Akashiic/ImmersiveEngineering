from pathlib import Path
p = Path(r'C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport\src\main\java\blusunrize\immersiveengineering\client\ClientProxy.java')
s = p.read_text(encoding='utf-8')
s = s.replace(r'\tpublic static boolean isThorfusionLoaded;', '\tpublic static boolean isThorfusionLoaded;')
if 'public void preInit()' not in s:
    needle = '\t@Override\n\tpublic void init()'
    repl = '\tpublic void preInit()\n\t{\n\t\tisThorfusionLoaded = Loader.isModLoaded("thorfusion");\n\t}\n\n\t@Override\n\tpublic void init()'
    s = s.replace(needle, repl, 1)
if 'maggi373/files/main/capes/cape.json' not in s:
    needle = '\t\tFMLCommonHandler.instance().bus().register(handler);\n\t}\n\n\t@Override\n\tpublic void postInit()'
    repl = '\t\tFMLCommonHandler.instance().bus().register(handler);\n\n\t\tif(!isThorfusionLoaded)\n\t\t{\n\t\t\ttry\n\t\t\t{\n\t\t\t\tDevCapes.getInstance().registerConfig("https://raw.githubusercontent.com/maggi373/files/main/capes/cape.json");\n\t\t\t}\n\t\t\tcatch(Exception e)\n\t\t\t{\n\t\t\t\tSystem.out.print("Cant load capes\\n"+e);\n\t\t\t}\n\t\t}\n\t}\n\n\t@Override\n\tpublic void postInit()'
    s = s.replace(needle, repl, 1)
p.write_text(s, encoding='utf-8', newline='\n')
print('patched', p)
print('literal \\t count:', s.count(r'\t'))