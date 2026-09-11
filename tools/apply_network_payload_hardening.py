from pathlib import Path

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\network-hardening")
PATH = ROOT / "src/main/java/blusunrize/immersiveengineering/common/util/network/MessageTileSync.java"


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
    "import net.minecraft.entity.player.EntityPlayerMP;\nimport net.minecraft.nbt.NBTTagCompound;\n",
    "import java.util.Arrays;\n\n"
    "import net.minecraft.entity.player.EntityPlayerMP;\n"
    "import net.minecraft.nbt.CompressedStreamTools;\n"
    "import net.minecraft.nbt.NBTTagCompound;\n"
    "import net.minecraft.nbt.NBTSizeTracker;\n",
)
patch(
    "public class MessageTileSync implements IMessage\n{\n"
    "\tint dimension;\n\tint x;\n\tint y;\n\tint z;\n\tNBTTagCompound nbt;\n",
    "public class MessageTileSync implements IMessage\n{\n"
    "\tprivate static final int MAX_ENCODED_NBT_BYTES = 32769;\n"
    "\tprivate static final long MAX_DECODED_NBT_BYTES = 262144L;\n\n"
    "\tint dimension;\n\tint x;\n\tint y;\n\tint z;\n"
    "\tNBTTagCompound nbt;\n\tbyte[] encodedNbt;\n",
)

patch(
    "\t\tthis.z = buf.readInt();\n\t\tthis.nbt =  ByteBufUtils.readTag(buf);\n",
    "\t\tthis.z = buf.readInt();\n"
    "\t\tint remaining = buf.readableBytes();\n"
    "\t\tif(remaining<2 || remaining>MAX_ENCODED_NBT_BYTES)\n"
    "\t\t{\n\t\t\tbuf.skipBytes(remaining);\n\t\t\tencodedNbt = null;\n\t\t\treturn;\n\t\t}\n"
    "\t\tencodedNbt = new byte[remaining];\n\t\tbuf.readBytes(encodedNbt);\n",
)
patch(
    "\n\tpublic static class Handler implements IMessageHandler<MessageTileSync, IMessage>\n",
    "\n\tprivate NBTTagCompound decodeTag()\n"
    "\t{\n"
    "\t\tif(encodedNbt==null || encodedNbt.length<2)\n\t\t\treturn null;\n"
    "\t\tshort declared = (short)(((encodedNbt[0]&255)<<8)|(encodedNbt[1]&255));\n"
    "\t\tif(declared<0 || declared!=encodedNbt.length-2)\n\t\t\treturn null;\n"
    "\t\ttry\n\t\t{\n"
    "\t\t\tbyte[] compressed = Arrays.copyOfRange(encodedNbt, 2, encodedNbt.length);\n"
    "\t\t\treturn CompressedStreamTools.func_152457_a(compressed, new NBTSizeTracker(MAX_DECODED_NBT_BYTES));\n"
    "\t\t}\n\t\tcatch(Exception ex)\n\t\t{\n\t\t\treturn null;\n\t\t}\n\t}\n\n"
    "\tpublic static class Handler implements IMessageHandler<MessageTileSync, IMessage>\n",
)

patch(
    "ctx.getServerHandler().playerEntity==null || message.nbt==null",
    "ctx.getServerHandler().playerEntity==null || message.encodedNbt==null",
)
patch(
    "\t\t\t\t\tTileEntity tile = world.getTileEntity(message.x, message.y, message.z);\n"
    "\t\t\t\t\tif(tile instanceof TileEntityIEBase)\n"
    "\t\t\t\t\t\t((TileEntityIEBase)tile).receiveMessageFromClient(player, message.nbt);\n",
    "\t\t\t\t\tTileEntity tile = world.getTileEntity(message.x, message.y, message.z);\n"
    "\t\t\t\t\tif(!(tile instanceof TileEntityIEBase))\n\t\t\t\t\t\treturn;\n"
    "\t\t\t\t\tNBTTagCompound decoded = message.decodeTag();\n"
    "\t\t\t\t\tif(decoded!=null)\n"
    "\t\t\t\t\t\t((TileEntityIEBase)tile).receiveMessageFromClient(player, decoded);\n",
)

print("network payload hardening applied")
