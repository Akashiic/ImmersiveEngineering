package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

/** Legacy-native implementation of the modern 2x2x2 Alloy Kiln. */
public class TileEntityAlloySmelter extends TileEntityMultiblockPart implements IInventory
{
	public static final int INVENTORY_SIZE = 4;
	public static final int MASTER_POS = 0;

	public int facing = 2;
	public ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];
	public int process;
	public int processMax;
	public int burnTime;
	public int lastBurnTime;
	public boolean active;
	private boolean disassembling;
	private AlloyRecipe cachedRecipe;
	private long cachedRecipeRevision = -1;
	private ItemStack cachedInput0;
	private ItemStack cachedInput1;

	@Override
	public TileEntityAlloySmelter master()
	{
		if(isMasterPart())
			return null;
		if(offset==null||offset.length<3||worldObj==null)
			return null;
		TileEntity te = Utils.getExistingTileEntity(
				worldObj, xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityAlloySmelter?(TileEntityAlloySmelter)te:null;
	}

	public TileEntityAlloySmelter getMasterOrSelf()
	{
		return isMasterPart()?this:master();
	}

	public boolean isMasterPart()
	{
		return pos==MASTER_POS&&offset!=null&&offset.length>=3
				&&offset[0]==0&&offset[1]==0&&offset[2]==0;
	}

	public int getLocalHeight()
	{
		return pos>=0?(pos/4)&1:0;
	}

	public int getLocalDepth()
	{
		return pos>=0?(pos/2)&1:0;
	}

	public int getLocalWidth()
	{
		return pos>=0?pos&1:0;
	}

	public boolean isRenderActive()
	{
		TileEntityAlloySmelter master = getMasterOrSelf();
		return master!=null&&master.active;
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		return new ItemStack(IEContent.blockStoneDecoration2, 1, BlockStoneDecoration2.META_alloyBrick);
	}

	@Override
	public float[] getBlockBounds()
	{
		return new float[]{0, 0, 0, 1, 1, 1};
	}

	@Override
	public void updateEntity()
	{
		if(worldObj==null||worldObj.isRemote||!formed||!isMasterPart())
			return;
		boolean wasActive = active;
		boolean changed = tickFurnace();
		if(wasActive!=active)
		{
			markAllPartsForUpdate();
			changed = true;
		}
		if(changed)
			markDirty();
	}

	private boolean tickFurnace()
	{
		boolean changed = false;
		boolean didProcessWork = false;
		active = false;

		if(burnTime>0)
		{
			burnTime--;
			if(process>0)
			{
				AlloyRecipe recipe = getRecipe();
				if(recipe==null||recipe.time!=processMax)
				{
					cancelProcess();
					changed = true;
				}
				else
				{
					process--;
					didProcessWork = true;
					active = true;
				}
			}

			if(process<=0)
			{
				if(processMax>0)
				{
					changed |= completeRecipe();
					processMax = 0;
				}
				AlloyRecipe next = getRecipe();
				if(next!=null)
				{
					processMax = next.time;
					process = Math.max(0, next.time-(didProcessWork?0:1));
					active = true;
					changed = true;
				}
			}
		}

		if(burnTime<=0&&getRecipe()!=null)
		{
			int added = consumeFuel();
			if(added>0)
			{
				lastBurnTime = added;
				burnTime += added;
				changed = true;
			}
		}

		if(worldObj.getTotalWorldTime()%20==((xCoord^zCoord)&15))
			markDirty();
		return changed;
	}

	private void cancelProcess()
	{
		process = 0;
		processMax = 0;
		active = false;
	}

	public AlloyRecipe getRecipe()
	{
		long revision = AlloyRecipe.getRecipeRevision();
		if(revision!=cachedRecipeRevision
				||!sameStackAndSize(cachedInput0, inventory[0])
				||!sameStackAndSize(cachedInput1, inventory[1]))
		{
			cachedRecipe = inventory[0]!=null&&inventory[1]!=null
					?AlloyRecipe.findRecipe(inventory[0], inventory[1]):null;
			cachedRecipeRevision = revision;
			cachedInput0 = inventory[0]==null?null:inventory[0].copy();
			cachedInput1 = inventory[1]==null?null:inventory[1].copy();
		}
		if(cachedRecipe==null||!canAcceptOutput(cachedRecipe.output))
			return null;
		return cachedRecipe;
	}

	private boolean canAcceptOutput(ItemStack result)
	{
		if(result==null)
			return false;
		ItemStack current = inventory[3];
		if(current==null)
			return result.stackSize<=getInventoryStackLimit();
		if(!OreDictionary.itemMatches(current, result, true))
			return false;
		if(current.hasTagCompound()!=result.hasTagCompound())
			return false;
		if(current.hasTagCompound()&&!current.getTagCompound().equals(result.getTagCompound()))
			return false;
		return current.stackSize+result.stackSize<=getInventoryStackLimit();
	}

	private boolean completeRecipe()
	{
		AlloyRecipe recipe = getRecipe();
		if(recipe==null)
			return false;
		boolean normal = recipe.firstInputMapsToInput0(inventory[0], inventory[1]);
		int firstAmount = normal?recipe.input0.inputSize:recipe.input1.inputSize;
		int secondAmount = normal?recipe.input1.inputSize:recipe.input0.inputSize;
		shrinkSlot(0, firstAmount);
		shrinkSlot(1, secondAmount);

		if(inventory[3]==null)
			inventory[3] = recipe.output.copy();
		else
			inventory[3].stackSize += recipe.output.stackSize;
		markDirty();
		return true;
	}

	private void shrinkSlot(int slot, int amount)
	{
		ItemStack stack = inventory[slot];
		if(stack==null)
			return;
		stack.stackSize -= amount;
		if(stack.stackSize<=0)
			inventory[slot] = null;
	}

	private int consumeFuel()
	{
		ItemStack fuel = inventory[2];
		if(fuel==null)
			return 0;
		int burn = TileEntityFurnace.getItemBurnTime(fuel);
		if(burn<=0)
			return 0;
		Item item = fuel.getItem();
		if(fuel.stackSize==1&&item!=null&&item.hasContainerItem(fuel))
		{
			ItemStack container = item.getContainerItem(fuel);
			inventory[2] = container!=null?container.copy():null;
		}
		else
		{
			fuel.stackSize--;
			if(fuel.stackSize<=0)
				inventory[2] = null;
		}
		markDirty();
		return burn;
	}

	private void markAllPartsForUpdate()
	{
		for(int targetPos=0; targetPos<8; targetPos++)
		{
			int[] coords = getPartCoordinates(targetPos);
			TileEntity te = Utils.getExistingTileEntity(worldObj, coords[0], coords[1], coords[2]);
			if(te instanceof TileEntityAlloySmelter)
			{
				te.markDirty();
				worldObj.markBlockForUpdate(coords[0], coords[1], coords[2]);
			}
		}
	}

	public int[] getPartCoordinates(int targetPos)
	{
		int h = (targetPos/4)&1;
		int depth = (targetPos/2)&1;
		int width = targetPos&1;
		ForgeDirection back = ForgeDirection.getOrientation(BlockStoneDevices2.opposite(facing));
		ForgeDirection right = ForgeDirection.getOrientation(BlockStoneDevices2.rightOf(facing));
		return new int[]{
			xCoord+back.offsetX*depth+right.offsetX*width,
			yCoord+h,
			zCoord+back.offsetZ*depth+right.offsetZ*width
		};
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		process = nbt.getInteger("process");
		processMax = nbt.getInteger("processMax");
		burnTime = nbt.getInteger("burnTime");
		lastBurnTime = nbt.getInteger("lastBurnTime");
		active = nbt.getBoolean("active");
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), INVENTORY_SIZE);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		nbt.setInteger("process", process);
		nbt.setInteger("processMax", processMax);
		nbt.setInteger("burnTime", burnTime);
		nbt.setInteger("lastBurnTime", lastBurnTime);
		nbt.setBoolean("active", active);
		if(!descPacket)
			nbt.setTag("inventory", Utils.writeInventory(inventory));
	}

	@Override
	public int getSizeInventory()
	{
		return formed?INVENTORY_SIZE:0;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		TileEntityAlloySmelter master = getMasterOrSelf();
		return master!=null&&slot>=0&&slot<INVENTORY_SIZE?master.inventory[slot]:null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		TileEntityAlloySmelter master = getMasterOrSelf();
		if(master==null||slot<0||slot>=INVENTORY_SIZE||amount<=0)
			return null;
		ItemStack stack = master.inventory[slot];
		if(stack==null)
			return null;
		if(stack.stackSize<=amount)
		{
			master.inventory[slot] = null;
		}
		else
			stack = stack.splitStack(amount);
		if(slot==0||slot==1)
			master.cancelProcess();
		master.markDirty();
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		TileEntityAlloySmelter master = getMasterOrSelf();
		if(master==null||slot<0||slot>=INVENTORY_SIZE)
			return null;
		ItemStack stack = master.inventory[slot];
		master.inventory[slot] = null;
		if(slot==0||slot==1)
			master.cancelProcess();
		master.markDirty();
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		TileEntityAlloySmelter master = getMasterOrSelf();
		if(master==null||slot<0||slot>=INVENTORY_SIZE)
			return;
		if(stack!=null&&stack.stackSize>getInventoryStackLimit())
			stack.stackSize = getInventoryStackLimit();
		ItemStack old = master.inventory[slot];
		master.inventory[slot] = stack;
		if((slot==0||slot==1)&&!sameStackAndSize(old, stack))
			master.cancelProcess();
		master.markDirty();
	}

	private static boolean sameStackAndSize(ItemStack first, ItemStack second)
	{
		if(first==second)
			return true;
		if(first==null||second==null)
			return false;
		return first.stackSize==second.stackSize&&ItemStack.areItemStacksEqual(first, second);
	}

	@Override
	public String getInventoryName()
	{
		return "IEAlloySmelter";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		TileEntityAlloySmelter master = getMasterOrSelf();
		return master!=null&&master.worldObj.getTileEntity(master.xCoord, master.yCoord, master.zCoord)==master
				&&player.getDistanceSq(master.xCoord+.5, master.yCoord+.5, master.zCoord+.5)<=64;
	}

	@Override
	public void openInventory()
	{
	}

	@Override
	public void closeInventory()
	{
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack)
	{
		if(stack==null)
			return false;
		if(slot==0||slot==1)
			return AlloyRecipe.isValidInput(stack);
		if(slot==2)
			return TileEntityFurnace.isItemFuel(stack);
		return false;
	}

	public int getProcessProgress()
	{
		return processMax>0?processMax-process:0;
	}

	public int getProcessMax()
	{
		return processMax;
	}

	public int getBurnTime()
	{
		return burnTime;
	}

	public int getLastBurnTime()
	{
		return lastBurnTime;
	}

	public void receiveContainerUpdate(int id, int value)
	{
		if(id==0)
			process = value;
		else if(id==1)
			processMax = value;
		else if(id==2)
			burnTime = value;
		else if(id==3)
			lastBurnTime = value;
	}

	public void disassemble(int brokenX, int brokenY, int brokenZ)
	{
		if(!formed||worldObj==null||worldObj.isRemote)
			return;
		TileEntityAlloySmelter master = getMasterOrSelf();
		if(master==null||master.disassembling)
			return;
		master.disassembling = true;

		for(int targetPos=0; targetPos<8; targetPos++)
		{
			int[] coords = master.getPartCoordinates(targetPos);
			TileEntity te = Utils.getExistingTileEntity(worldObj, coords[0], coords[1], coords[2]);
			if(te instanceof TileEntityAlloySmelter&&belongsToMaster((TileEntityAlloySmelter)te, master, targetPos, coords))
			{
				TileEntityAlloySmelter part = (TileEntityAlloySmelter)te;
				part.formed = false;
				part.disassembling = true;
			}
		}

		boolean doDrops = worldObj.getGameRules().getGameRuleBooleanValue("doTileDrops");
		master.dropInventory(doDrops);

		for(int targetPos=0; targetPos<8; targetPos++)
		{
			int[] coords = master.getPartCoordinates(targetPos);
			boolean broken = coords[0]==brokenX&&coords[1]==brokenY&&coords[2]==brokenZ;
			TileEntity te = Utils.getExistingTileEntity(worldObj, coords[0], coords[1], coords[2]);
			boolean owned = te instanceof TileEntityAlloySmelter
					&&belongsToMaster((TileEntityAlloySmelter)te, master, targetPos, coords);
			if(broken)
			{
				if(doDrops&&owned)
					spawnStack(new ItemStack(IEContent.blockStoneDecoration2, 1, BlockStoneDecoration2.META_alloyBrick), brokenX, brokenY, brokenZ);
				continue;
			}
			if(!owned||worldObj.getBlock(coords[0], coords[1], coords[2])!=IEContent.blockStoneDevice2)
				continue;
			worldObj.setBlock(coords[0], coords[1], coords[2], IEContent.blockStoneDecoration2,
					BlockStoneDecoration2.META_alloyBrick, 3);
		}
	}

	private boolean belongsToMaster(TileEntityAlloySmelter part, TileEntityAlloySmelter master, int targetPos, int[] coords)
	{
		return part.pos==targetPos&&part.offset!=null&&part.offset.length>=3
				&&part.offset[0]==coords[0]-master.xCoord
				&&part.offset[1]==coords[1]-master.yCoord
				&&part.offset[2]==coords[2]-master.zCoord;
	}

	private void dropInventory(boolean doDrops)
	{
		for(int slot=0; slot<inventory.length; slot++)
		{
			ItemStack stack = inventory[slot];
			inventory[slot] = null;
			if(doDrops&&stack!=null&&stack.stackSize>0)
				spawnStack(stack, xCoord, yCoord, zCoord);
		}
		cancelProcess();
		burnTime = 0;
		lastBurnTime = 0;
		markDirty();
	}

	private void spawnStack(ItemStack stack, int x, int y, int z)
	{
		if(stack==null||stack.stackSize<=0)
			return;
		EntityItem item = new EntityItem(worldObj, x+.5, y+.5, z+.5, stack.copy());
		item.motionX = worldObj.rand.nextGaussian()*.03;
		item.motionY = .15+worldObj.rand.nextDouble()*.05;
		item.motionZ = worldObj.rand.nextGaussian()*.03;
		worldObj.spawnEntityInWorld(item);
	}
}
