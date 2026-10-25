import pathlib
import subprocess

repo = pathlib.Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport")
rel = "src/main/java/blusunrize/immersiveengineering/common/IERecipes.java"
p = repo / rel
base = subprocess.check_output(["git", "-C", str(repo), "show", "0c803472:" + rel])
anchor = b"\t\taddOredictRecipe(new ItemStack(IEContent.blockMetalDevice2,1, BlockMetalDevices2.META_blastFurnacePreheater)"
pos = base.find(anchor)
assert pos >= 0
end = base.find(b"\n", pos) + 1
nl = b"\r\n" if b"\r\n" in base else b"\n"
line = ("\t\taddOredictRecipe(new ItemStack(IEContent.blockMetalDevice2,1, BlockMetalDevices2.META_gardenCloche), \" L \",\"G G\",\"WCW\", "
        "'L',new ItemStack(IEContent.blockMetalDecoration,1,BlockMetalDecoration.META_lantern), 'G',\"blockGlass\", "
        "'W',\"plankTreatedWood\", 'C',componentIron);").encode("ascii") + nl
p.write_bytes(base[:end] + line + base[end:])
print("baseline", len(base), "new", p.stat().st_size, "newline", repr(nl))
