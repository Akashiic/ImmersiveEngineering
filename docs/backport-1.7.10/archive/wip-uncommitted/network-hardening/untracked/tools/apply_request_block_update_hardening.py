from pathlib import Path

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\network-hardening")
PATH = ROOT / "src/main/java/blusunrize/immersiveengineering/common/util/network/MessageRequestBlockUpdate.java"


def patch(old, new):
    data = PATH.read_bytes()
    nl = b"\r\n" if b"\r\n" in data else b"\n"
    sep = "\r\n" if nl == b"\r\n" else "\n"
    old_b = old.replace("\n", sep).encode()
    new_b = new.replace("\n", sep).encode()
    count = data.count(old_b)
    if count != 1:
        raise RuntimeError(f"expected 1 match, found {count}")
    PATH.write_bytes(data.replace(old_b, new_b, 1))


patch(
    "import cpw.mods.fml.common.FMLCommonHandler;\n",
    "import blusunrize.immersiveengineering.common.IEContent;\n"
    "import net.minecraft.entity.player.EntityPlayerMP;\n"
    "import net.minecraft.item.ItemStack;\n",
)
patch(
    "import net.minecraft.server.MinecraftServer;\nimport net.minecraft.world.World;\n",
    "import net.minecraft.nbt.NBTTagCompound;\nimport net.minecraft.world.World;\n",
)

patch(
    "public class MessageRequestBlockUpdate implements IMessage\n{\n",
    "public class MessageRequestBlockUpdate implements IMessage\n{\n"
    "\tprivate static final int MIN_REQUEST_INTERVAL_TICKS = 10;\n"
    "\tprivate static final String LAST_REQUEST_TICK = \"IEVoltmeterBlockUpdateTick\";\n\n",
)
patch(
    "\t\tpublic IMessage onMessage(MessageRequestBlockUpdate message, MessageContext ctx)\n"
    "\t\t{\n"
    "\t\t\tif (FMLCommonHandler.instance().getEffectiveSide()==Side.SERVER)\n"
    "\t\t\t{\n"
    "\t\t\t\tWorld w = MinecraftServer.getServer().worldServerForDimension(message.dim);\n"
    "\t\t\t\tif (w!=null)\n\t\t\t\t{\n"
    "\t\t\t\t\tw.markBlockForUpdate(message.x, message.y, message.z);\n"
    "\t\t\t\t}\n\t\t\t}\n"
    "\t\t\treturn null;\n\t\t}\n",
    "\t\tpublic IMessage onMessage(final MessageRequestBlockUpdate message, MessageContext ctx)\n"
    "\t\t{\n"
    "\t\t\tif(ctx.side!=Side.SERVER || ctx.getServerHandler()==null || ctx.getServerHandler().playerEntity==null)\n"
    "\t\t\t\treturn null;\n"
    "\t\t\tfinal EntityPlayerMP player = ctx.getServerHandler().playerEntity;\n"
    "\t\t\tServerNetworkTaskQueue.enqueue(player, new Runnable()\n\t\t\t{\n"
    "\t\t\t\t@Override\n\t\t\t\tpublic void run()\n\t\t\t\t{\n"
    "\t\t\t\t\tWorld world = player.worldObj;\n"
    "\t\t\t\t\tif(world==null || world.provider.dimensionId!=message.dim)\n\t\t\t\t\t\treturn;\n"
    "\t\t\t\t\tif(player.getDistanceSq(message.x+.5D, message.y+.5D, message.z+.5D)>64D)\n\t\t\t\t\t\treturn;\n"
    "\t\t\t\t\tItemStack held = player.getCurrentEquippedItem();\n"
    "\t\t\t\t\tif(held==null || held.getItem()!=IEContent.itemTool || held.getItemDamage()!=2)\n\t\t\t\t\t\treturn;\n"
    "\t\t\t\t\tif(!world.getChunkProvider().chunkExists(message.x>>4, message.z>>4))\n\t\t\t\t\t\treturn;\n",
)
patch(
    "\t\t\t\t\tif(!world.getChunkProvider().chunkExists(message.x>>4, message.z>>4))\n\t\t\t\t\t\treturn;\n",
    "\t\t\t\t\tif(!world.getChunkProvider().chunkExists(message.x>>4, message.z>>4))\n\t\t\t\t\t\treturn;\n"
    "\t\t\t\t\tlong now = world.getTotalWorldTime();\n"
    "\t\t\t\t\tNBTTagCompound playerData = player.getEntityData();\n"
    "\t\t\t\t\tif(playerData.hasKey(LAST_REQUEST_TICK))\n\t\t\t\t\t{\n"
    "\t\t\t\t\t\tlong delta = now-playerData.getLong(LAST_REQUEST_TICK);\n"
    "\t\t\t\t\t\tif(delta>=0 && delta<MIN_REQUEST_INTERVAL_TICKS)\n\t\t\t\t\t\t\treturn;\n"
    "\t\t\t\t\t}\n"
    "\t\t\t\t\tplayerData.setLong(LAST_REQUEST_TICK, now);\n"
    "\t\t\t\t\tworld.markBlockForUpdate(message.x, message.y, message.z);\n"
    "\t\t\t\t}\n\t\t\t});\n\t\t\treturn null;\n\t\t}\n",
)

print("request block update hardening applied")
