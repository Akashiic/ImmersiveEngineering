package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public abstract class TileEntityIEBase extends TileEntity
{
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.readCustomNBT(nbt, false);
	}
	public abstract void readCustomNBT(NBTTagCompound nbt, boolean descPacket);
	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		this.writeCustomNBT(nbt, false);
	}
	public abstract void writeCustomNBT(NBTTagCompound nbt, boolean descPacket);
	
	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		this.writeCustomNBT(nbttagcompound, true);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 3, nbttagcompound);
	}
	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
		this.readCustomNBT(pkt.func_148857_g(), true);
    }
	
	public void receiveMessageFromClient(EntityPlayer player, NBTTagCompound message)
	{
		if(player==null || worldObj==null || player.worldObj!=worldObj
				|| worldObj.getTileEntity(xCoord, yCoord, zCoord)!=this)
			return;
		if(player.getDistanceSq(xCoord+.5D, yCoord+.5D, zCoord+.5D)>64D)
			return;
		receiveMessageFromClient(message);
	}

	public void receiveMessageFromClient(NBTTagCompound message)
	{
	}
}
