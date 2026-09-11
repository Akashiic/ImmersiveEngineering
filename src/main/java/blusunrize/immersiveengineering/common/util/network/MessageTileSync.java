package blusunrize.immersiveengineering.common.util.network;

import io.netty.buffer.ByteBuf;
import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

public class MessageTileSync implements IMessage
{
	private static final int MAX_ENCODED_NBT_BYTES = 32769;
	private static final long MAX_DECODED_NBT_BYTES = 262144L;

	int dimension;
	int x;
	int y;
	int z;
	NBTTagCompound nbt;
	byte[] encodedNbt;
	public MessageTileSync(TileEntityIEBase tile, NBTTagCompound nbt)
	{
		this.dimension = tile.getWorldObj().provider.dimensionId;
		this.x = tile.xCoord;
		this.y = tile.yCoord;
		this.z = tile.zCoord;
		this.nbt = nbt;
	}
	public MessageTileSync()
	{
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		this.dimension = buf.readInt();
		this.x = buf.readInt();
		this.y = buf.readInt();
		this.z = buf.readInt();
		int remaining = buf.readableBytes();
		if(remaining<2 || remaining>MAX_ENCODED_NBT_BYTES)
		{
			buf.skipBytes(remaining);
			encodedNbt = null;
			return;
		}
		encodedNbt = new byte[remaining];
		buf.readBytes(encodedNbt);
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeInt(this.dimension);
		buf.writeInt(this.x);
		buf.writeInt(this.y);
		buf.writeInt(this.z);
		ByteBufUtils.writeTag(buf,this.nbt); 
	}

	private NBTTagCompound decodeTag()
	{
		if(encodedNbt==null || encodedNbt.length<2)
			return null;
		short declared = (short)(((encodedNbt[0]&255)<<8)|(encodedNbt[1]&255));
		if(declared<0 || declared!=encodedNbt.length-2)
			return null;
		try
		{
			byte[] compressed = Arrays.copyOfRange(encodedNbt, 2, encodedNbt.length);
			return CompressedStreamTools.func_152457_a(compressed, new NBTSizeTracker(MAX_DECODED_NBT_BYTES));
		}
		catch(Exception ex)
		{
			return null;
		}
	}

	public static class Handler implements IMessageHandler<MessageTileSync, IMessage>
	{
		@Override
		public IMessage onMessage(final MessageTileSync message, MessageContext ctx)
		{
			if(ctx.side!=Side.SERVER || ctx.getServerHandler()==null || ctx.getServerHandler().playerEntity==null || message.encodedNbt==null)
				return null;

			final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
			ServerNetworkTaskQueue.enqueue(player, new Runnable()
			{
				@Override
				public void run()
				{
					World world = player.worldObj;
					if(world==null || world.provider.dimensionId!=message.dimension)
						return;
					if(player.getDistanceSq(message.x+.5D, message.y+.5D, message.z+.5D)>64D)
						return;
					if(!world.getChunkProvider().chunkExists(message.x>>4, message.z>>4))
						return;
					TileEntity tile = world.getTileEntity(message.x, message.y, message.z);
					if(!(tile instanceof TileEntityIEBase))
						return;
					NBTTagCompound decoded = message.decodeTag();
					if(decoded!=null)
						((TileEntityIEBase)tile).receiveMessageFromClient(player, decoded);
				}
			});
			return null;
		}
	}
}