from pathlib import Path

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\network-hardening")
JAVA = ROOT / "src" / "main" / "java"


def patch(rel, old, new):
    path = JAVA / rel
    data = path.read_bytes()
    nl = b"\r\n" if b"\r\n" in data else b"\n"
    old_b = old.replace("\n", "\r\n" if nl == b"\r\n" else "\n").encode()
    new_b = new.replace("\n", "\r\n" if nl == b"\r\n" else "\n").encode()
    count = data.count(old_b)
    if count != 1:
        raise RuntimeError(f"{rel}: expected 1 match, found {count}")
    path.write_bytes(data.replace(old_b, new_b, 1))


patch(
    "blusunrize/immersiveengineering/common/EventHandler.java",
    "import blusunrize.immersiveengineering.common.util.network.MessageMineralListSync;\n",
    "import blusunrize.immersiveengineering.common.util.network.MessageMineralListSync;\n"
    "import blusunrize.immersiveengineering.common.util.network.ServerNetworkTaskQueue;\n",
)
patch(
    "blusunrize/immersiveengineering/common/EventHandler.java",
    "import cpw.mods.fml.common.gameevent.TickEvent;\n"
    "import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;\n",
    "import cpw.mods.fml.common.gameevent.TickEvent;\n"
    "import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;\n"
    "import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;\n",
)

patch(
    "blusunrize/immersiveengineering/common/EventHandler.java",
    "\n\t@SubscribeEvent\n\tpublic void onWorldTick(WorldTickEvent event)\n",
    "\n\t@SubscribeEvent\n\tpublic void onServerTick(ServerTickEvent event)\n"
    "\t{\n\t\tif(event.phase==TickEvent.Phase.START)\n"
    "\t\t\tServerNetworkTaskQueue.drain();\n\t}\n\n"
    "\t@SubscribeEvent\n\tpublic void onWorldTick(WorldTickEvent event)\n",
)

patch(
    "blusunrize/immersiveengineering/common/blocks/TileEntityIEBase.java",
    "import net.minecraft.nbt.NBTTagCompound;\n",
    "import net.minecraft.entity.player.EntityPlayer;\nimport net.minecraft.nbt.NBTTagCompound;\n",
)
patch(
    "blusunrize/immersiveengineering/common/blocks/TileEntityIEBase.java",
    "\n\tpublic void receiveMessageFromClient(NBTTagCompound message)\n\t{\n\t}\n",
    "\n\tpublic void receiveMessageFromClient(EntityPlayer player, NBTTagCompound message)\n"
    "\t{\n"
    "\t\tif(player==null || worldObj==null || player.worldObj!=worldObj\n"
    "\t\t\t\t|| worldObj.getTileEntity(xCoord, yCoord, zCoord)!=this)\n\t\t\treturn;\n"
    "\t\tif(player.getDistanceSq(xCoord+.5D, yCoord+.5D, zCoord+.5D)>64D)\n\t\t\treturn;\n"
    "\t\treceiveMessageFromClient(message);\n\t}\n\n"
    "\tpublic void receiveMessageFromClient(NBTTagCompound message)\n\t{\n\t}\n",
)

patch(
    "blusunrize/immersiveengineering/common/util/network/MessageTileSync.java",
    "import net.minecraft.nbt.NBTTagCompound;\nimport net.minecraft.tileentity.TileEntity;\n"
    "import net.minecraft.world.World;\nimport net.minecraftforge.common.DimensionManager;\n",
    "import net.minecraft.entity.player.EntityPlayerMP;\nimport net.minecraft.nbt.NBTTagCompound;\n"
    "import net.minecraft.tileentity.TileEntity;\nimport net.minecraft.world.World;\n",
)
patch(
    "blusunrize/immersiveengineering/common/util/network/MessageTileSync.java",
    "import cpw.mods.fml.common.network.simpleimpl.MessageContext;\n",
    "import cpw.mods.fml.common.network.simpleimpl.MessageContext;\n"
    "import cpw.mods.fml.relauncher.Side;\n",
)

patch(
    "blusunrize/immersiveengineering/common/util/network/MessageTileSync.java",
    "\t\tpublic IMessage onMessage(MessageTileSync message, MessageContext ctx)\n"
    "\t\t{\n"
    "\t\t\tWorld world = DimensionManager.getWorld(message.dimension);\n"
    "\t\t\tif(world!=null)\n\t\t\t{\n"
    "\t\t\t\tTileEntity tile = world.getTileEntity(message.x,message.y,message.z);\n"
    "\t\t\t\tif(tile instanceof TileEntityIEBase)\n"
    "\t\t\t\t\t((TileEntityIEBase)tile).receiveMessageFromClient(message.nbt);\n"
    "\t\t\t}\n\t\t\treturn null;\n\t\t}\n",
    "\t\tpublic IMessage onMessage(final MessageTileSync message, MessageContext ctx)\n"
    "\t\t{\n"
    "\t\t\tif(ctx.side!=Side.SERVER || ctx.getServerHandler()==null || ctx.getServerHandler().playerEntity==null || message.nbt==null)\n"
    "\t\t\t\treturn null;\n\n"
    "\t\t\tfinal EntityPlayerMP player = ctx.getServerHandler().playerEntity;\n"
    "\t\t\tServerNetworkTaskQueue.enqueue(player, new Runnable()\n\t\t\t{\n"
    "\t\t\t\t@Override\n\t\t\t\tpublic void run()\n\t\t\t\t{\n"
    "\t\t\t\t\tWorld world = player.worldObj;\n"
    "\t\t\t\t\tif(world==null || world.provider.dimensionId!=message.dimension)\n\t\t\t\t\t\treturn;\n"
    "\t\t\t\t\tif(player.getDistanceSq(message.x+.5D, message.y+.5D, message.z+.5D)>64D)\n\t\t\t\t\t\treturn;\n"
    "\t\t\t\t\tif(!world.getChunkProvider().chunkExists(message.x>>4, message.z>>4))\n\t\t\t\t\t\treturn;\n"
    "\t\t\t\t\tTileEntity tile = world.getTileEntity(message.x, message.y, message.z);\n"
    "\t\t\t\t\tif(tile instanceof TileEntityIEBase)\n"
    "\t\t\t\t\t\t((TileEntityIEBase)tile).receiveMessageFromClient(player, message.nbt);\n"
    "\t\t\t\t}\n\t\t\t});\n\t\t\treturn null;\n\t\t}\n",
)
patch(
    "blusunrize/immersiveengineering/common/gui/ContainerAssembler.java",
    "\n\t@Override\n\tpublic boolean canInteractWith(EntityPlayer p_75145_1_)\n",
    "\n\tpublic TileEntityAssembler getTile()\n\t{\n\t\treturn tile;\n\t}\n\n"
    "\t@Override\n\tpublic boolean canInteractWith(EntityPlayer p_75145_1_)\n",
)

patch(
    "blusunrize/immersiveengineering/common/gui/ContainerSorter.java",
    "\n\t@Override\n\tpublic boolean canInteractWith(EntityPlayer p_75145_1_)\n",
    "\n\tpublic TileEntityConveyorSorter getTile()\n\t{\n\t\treturn tile;\n\t}\n\n"
    "\t@Override\n\tpublic boolean canInteractWith(EntityPlayer p_75145_1_)\n",
)

patch(
    "blusunrize/immersiveengineering/common/blocks/metal/TileEntityAutoWorkbench.java",
    "import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockAutoWorkbench;\n",
    "import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockAutoWorkbench;\n"
    "import blusunrize.immersiveengineering.common.gui.ContainerAutoWorkbench;\n",
)
patch(
    "blusunrize/immersiveengineering/common/blocks/metal/TileEntityAutoWorkbench.java",
    "\n\t@Override\n\tpublic void receiveMessageFromClient(NBTTagCompound message)\n",
    "\n\t@Override\n\tpublic void receiveMessageFromClient(EntityPlayer player, NBTTagCompound message)\n"
    "\t{\n"
    "\t\tif(!(player.openContainer instanceof ContainerAutoWorkbench)\n"
    "\t\t\t\t|| ((ContainerAutoWorkbench)player.openContainer).tile!=this)\n"
    "\t\t\treturn;\n"
    "\t\tsuper.receiveMessageFromClient(player, message);\n"
    "\t}\n\n"
    "\t@Override\n\tpublic void receiveMessageFromClient(NBTTagCompound message)\n",
)

patch(
    "blusunrize/immersiveengineering/common/blocks/metal/TileEntityAssembler.java",
    "import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockAssembler;\n",
    "import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockAssembler;\n"
    "import blusunrize.immersiveengineering.common.gui.ContainerAssembler;\n",
)
patch(
    "blusunrize/immersiveengineering/common/blocks/metal/TileEntityAssembler.java",
    "\n\t@Override\n\tpublic void receiveMessageFromClient(NBTTagCompound message)\n",
    "\n\t@Override\n\tpublic void receiveMessageFromClient(EntityPlayer player, NBTTagCompound message)\n"
    "\t{\n"
    "\t\tif(!(player.openContainer instanceof ContainerAssembler)\n"
    "\t\t\t\t|| ((ContainerAssembler)player.openContainer).getTile()!=this)\n"
    "\t\t\treturn;\n"
    "\t\tsuper.receiveMessageFromClient(player, message);\n"
    "\t}\n\n"
    "\t@Override\n\tpublic void receiveMessageFromClient(NBTTagCompound message)\n",
)

patch(
    "blusunrize/immersiveengineering/common/blocks/metal/TileEntityAssembler.java",
    "\t\t\tint r = message.getInteger(\"recipe\");\n"
    "\t\t\tNBTTagList list = message.getTagList(\"patternSync\", 10);\n"
    "\t\t\tCrafterPatternInventory pattern = patterns[r];\n"
    "\t\t\tfor(int i=0; i<list.tagCount(); i++)\n",
    "\t\t\tint r = message.getInteger(\"recipe\");\n"
    "\t\t\tif(r<0 || r>=patterns.length)\n\t\t\t\treturn;\n"
    "\t\t\tNBTTagList list = message.getTagList(\"patternSync\", 10);\n"
    "\t\t\tCrafterPatternInventory pattern = patterns[r];\n"
    "\t\t\tint entries = Math.min(list.tagCount(), pattern.inv.length);\n"
    "\t\t\tfor(int i=0; i<entries; i++)\n",
)
patch(
    "blusunrize/immersiveengineering/common/blocks/metal/TileEntityAssembler.java",
    "\t\t\t\tNBTTagCompound itemTag = list.getCompoundTagAt(i);\n"
    "\t\t\t\tpattern.inv[itemTag.getInteger(\"slot\")] = ItemStack.loadItemStackFromNBT(itemTag);\n",
    "\t\t\t\tNBTTagCompound itemTag = list.getCompoundTagAt(i);\n"
    "\t\t\t\tint slot = itemTag.getInteger(\"slot\");\n"
    "\t\t\t\tif(slot<0 || slot>=pattern.inv.length)\n\t\t\t\t\tcontinue;\n"
    "\t\t\t\tpattern.inv[slot] = ItemStack.loadItemStackFromNBT(itemTag);\n",
)

patch(
    "blusunrize/immersiveengineering/common/blocks/metal/TileEntityConveyorSorter.java",
    "import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;\n",
    "import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;\n"
    "import blusunrize.immersiveengineering.common.gui.ContainerSorter;\n",
)

patch(
    "blusunrize/immersiveengineering/common/blocks/metal/TileEntityConveyorSorter.java",
    "\n\t@Override\n\tpublic void receiveMessageFromClient(NBTTagCompound message)\n",
    "\n\t@Override\n\tpublic void receiveMessageFromClient(EntityPlayer player, NBTTagCompound message)\n"
    "\t{\n\t\tif(!(player.openContainer instanceof ContainerSorter)\n"
    "\t\t\t\t|| ((ContainerSorter)player.openContainer).getTile()!=this)\n\t\t\treturn;\n"
    "\t\tsuper.receiveMessageFromClient(player, message);\n\t}\n\n"
    "\t@Override\n\tpublic void receiveMessageFromClient(NBTTagCompound message)\n",
)
patch(
    "blusunrize/immersiveengineering/common/blocks/metal/TileEntityConveyorSorter.java",
    "\tpublic void receiveMessageFromClient(NBTTagCompound message)\n"
    "\t{\n"
    "\t\tif(message.hasKey(\"sideConfig\"))\n"
    "\t\t\tthis.sideFilter = message.getIntArray(\"sideConfig\");\n"
    "\t}\n",
    "\tpublic void receiveMessageFromClient(NBTTagCompound message)\n"
    "\t{\n"
    "\t\tif(!message.hasKey(\"sideConfig\"))\n\t\t\treturn;\n"
    "\t\tint[] requested = message.getIntArray(\"sideConfig\");\n"
    "\t\tif(requested.length!=6)\n\t\t\treturn;\n"
    "\t\tfor(int value : requested)\n"
    "\t\t\tif((value&~7)!=0)\n\t\t\t\treturn;\n"
    "\t\tthis.sideFilter = requested;\n"
    "\t}\n",
)

print("network hardening patches applied")
