from pathlib import Path

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\network-hardening")
JAVA = ROOT / "src/main/java"


def patch(rel, old, new):
    path = JAVA / rel
    data = path.read_bytes()
    nl = b"\r\n" if b"\r\n" in data else b"\n"
    sep = "\r\n" if nl == b"\r\n" else "\n"
    old_b = old.replace("\n", sep).encode()
    new_b = new.replace("\n", sep).encode()
    count = data.count(old_b)
    if count != 1:
        raise RuntimeError(f"{rel}: expected 1 match, found {count}")
    path.write_bytes(data.replace(old_b, new_b, 1))


patch(
    "blusunrize/immersiveengineering/common/EventHandler.java",
    "import blusunrize.immersiveengineering.common.util.network.MessageMineralListSync;\n"
    "import blusunrize.immersiveengineering.common.util.network.ServerNetworkTaskQueue;\n",
    "import blusunrize.immersiveengineering.common.util.network.MessageMineralListSync;\n"
    "import blusunrize.immersiveengineering.common.util.network.MinecartShaderSyncRequestQueue;\n"
    "import blusunrize.immersiveengineering.common.util.network.ServerNetworkTaskQueue;\n",
)
patch(
    "blusunrize/immersiveengineering/common/EventHandler.java",
    "\t\tif(event.phase==TickEvent.Phase.START)\n"
    "\t\t\tServerNetworkTaskQueue.drain();\n",
    "\t\tif(event.phase==TickEvent.Phase.START)\n"
    "\t\t{\n"
    "\t\t\tServerNetworkTaskQueue.drain();\n"
    "\t\t\tMinecartShaderSyncRequestQueue.drain();\n"
    "\t\t}\n",
)

patch(
    "blusunrize/immersiveengineering/common/util/network/MessageMinecartShaderSync.java",
    "import net.minecraft.entity.Entity;\n",
    "import net.minecraft.entity.Entity;\nimport net.minecraft.entity.player.EntityPlayerMP;\n",
)

patch(
    "blusunrize/immersiveengineering/common/util/network/MessageMinecartShaderSync.java",
    "import net.minecraftforge.common.DimensionManager;\n",
    "",
)
patch(
    "blusunrize/immersiveengineering/common/util/network/MessageMinecartShaderSync.java",
    "import cpw.mods.fml.common.network.simpleimpl.MessageContext;\n",
    "import cpw.mods.fml.common.network.simpleimpl.MessageContext;\n"
    "import cpw.mods.fml.relauncher.Side;\n",
)

patch(
    "blusunrize/immersiveengineering/common/util/network/MessageMinecartShaderSync.java",
    "\t\tpublic IMessage onMessage(MessageMinecartShaderSync message, MessageContext ctx)\n"
    "\t\t{\n"
    "\t\t\tWorld world = DimensionManager.getWorld(message.dimension);\n"
    "\t\t\tif(world!=null)\n\t\t\t{\n"
    "\t\t\t\tEntity entity = world.getEntityByID(message.entityID);\n"
    "\t\t\t\tif(entity!=null)\n\t\t\t\t{\n"
    "\t\t\t\t\tEntityPropertiesShaderCart properties = (EntityPropertiesShaderCart)entity.getExtendedProperties(EntityPropertiesShaderCart.PROPERTY_NAME);\n"
    "\t\t\t\t\tif(properties!=null)\n"
    "\t\t\t\t\t\tImmersiveEngineering.packetHandler.sendToAll(new MessageMinecartShaderSync(entity, properties));\n"
    "\t\t\t\t}\n\t\t\t}\n"
    "\t\t\treturn null;\n\t\t}\n",
    "\t\tpublic IMessage onMessage(MessageMinecartShaderSync message, MessageContext ctx)\n"
    "\t\t{\n"
    "\t\t\tif(ctx.side!=Side.SERVER || !message.request || ctx.getServerHandler()==null\n"
    "\t\t\t\t\t|| ctx.getServerHandler().playerEntity==null)\n"
    "\t\t\t\treturn null;\n"
    "\t\t\tEntityPlayerMP player = ctx.getServerHandler().playerEntity;\n"
    "\t\t\tMinecartShaderSyncRequestQueue.request(player, message.dimension, message.entityID);\n"
    "\t\t\treturn null;\n\t\t}\n",
)

print("minecart shader request hardening applied")
