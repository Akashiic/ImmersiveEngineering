package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.Iterator;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.multiblocks.MultiblockAutoWorkbench;
import blusunrize.immersiveengineering.common.items.ItemEngineersBlueprint;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
public class TileEntityAutoWorkbench extends TileEntityMultiblockPart implements ISidedInventory, IEnergyReceiver
{
	public static final int MASTER_POS = 13;
	public static final int ENERGY_POS = 9;
	public static final int REDSTONE_POS = 1;
	public static final int INVENTORY_SIZE = 17;
	public static final int PROCESS_QUEUE_MAX = 3;
	public static final float PROCESS_SPACING = .4375f;
	public static final float TRANSFORMATION_POINT = .78f;
	public static final int PROCESS_DATA_VERSION = 1;
	private static final int BASE_PROCESS_TIME = 180;
	private static final int BASE_PROCESS_ENERGY = 23040;

	public int facing = 2;
	public final EnergyStorage energyStorage = new EnergyStorage(32000);
	public ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];
	public int selectedRecipe = -1;
	public boolean redstoneControlInverted = false;
	public final ArrayList<AutoWorkbenchProcess> processQueue = new ArrayList<AutoWorkbenchProcess>();
	public int clientTickedProcesses = 0;

	private int lastTickedProcesses = -1;
	private boolean disassembling = false;
	@Override
	public TileEntityAutoWorkbench master()
	{
		if(offset[0]==0 && offset[1]==0 && offset[2]==0)
			return null;
		TileEntity te = Utils.getExistingTileEntity(worldObj, xCoord-offset[0], yCoord-offset[1], zCoord-offset[2]);
		return te instanceof TileEntityAutoWorkbench?(TileEntityAutoWorkbench)te:null;
	}

	private TileEntityAutoWorkbench getMasterOrSelf()
	{
		if(isMasterPart())
			return this;
		return master();
	}

	@Override
	public ItemStack getOriginalBlock()
	{
		if(pos<0 || pos>=18)
			return null;
		ItemStack stack = MultiblockAutoWorkbench.instance.getStructureManual()[pos/9][pos%9/3][pos%3];
		return stack!=null?stack.copy():null;
	}

	private boolean isMasterPart()
	{
		return pos==MASTER_POS && offset[0]==0 && offset[1]==0 && offset[2]==0;
	}
	@Override
	public void updateEntity()
	{
		if(!formed || !isMasterPart())
			return;
		if(worldObj.isRemote)
		{
			tickClientProcesses();
			return;
		}

		boolean sync = tickServerProcesses();
		if(!isRSDisabled() && worldObj.getTotalWorldTime()%16==((xCoord^zCoord)&15))
			if(tryQueueRecipe())
				sync = true;
		if(sync)
			markMasterForUpdate();
	}

	private void tickClientProcesses()
	{
		int count = Math.min(clientTickedProcesses, processQueue.size());
		for(int i=0; i<count; i++)
		{
			AutoWorkbenchProcess process = processQueue.get(i);
			if(process.processTick<process.maxTicks)
				process.processTick++;
		}
	}
	private boolean tickServerProcesses()
	{
		boolean changed = false;
		int ticked = 0;
		if(!isRSDisabled())
		{
			Iterator<AutoWorkbenchProcess> iterator = processQueue.iterator();
			while(iterator.hasNext())
			{
				AutoWorkbenchProcess process = iterator.next();
				int energy = process.energyPerTick;
				if(energy<=0 || energyStorage.extractEnergy(energy, true)==energy)
				{
					if(energy>0)
						energyStorage.extractEnergy(energy, false);
					process.processTick++;
					ticked++;
					if(process.processTick>=process.maxTicks)
					{
						finishProcess(process);
						iterator.remove();
						changed = true;
					}
				}
			}
		}
		if(ticked>0)
			markDirty();
		if(ticked!=lastTickedProcesses)
		{
			lastTickedProcesses = ticked;
			clientTickedProcesses = ticked;
			changed = true;
		}
		return changed;
	}
	private boolean tryQueueRecipe()
	{
		if(inventory[0]==null || !(inventory[0].getItem() instanceof ItemEngineersBlueprint))
			return false;
		if(processQueue.size()>=PROCESS_QUEUE_MAX)
			return false;
		if(!processQueue.isEmpty())
		{
			AutoWorkbenchProcess last = processQueue.get(processQueue.size()-1);
			if(last.maxTicks<=0 || last.processTick/(float)last.maxTicks<PROCESS_SPACING)
				return false;
		}

		String category = ItemNBTHelper.getString(inventory[0], "blueprint");
		BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(category);
		if(selectedRecipe<0 || selectedRecipe>=recipes.length)
			return false;
		BlueprintCraftingRecipe recipe = recipes[selectedRecipe];
		if(recipe==null || recipe.output==null || recipe.output.stackSize<=0)
			return false;

		ItemStack[] query = copyComponentInventory();
		if(recipe.getMaxCrafted(query)<=0)
			return false;
		ItemStack[] consumed = recipe.consumeInputsForAutomation(query, 1);
		if(consumed==null || consumed.length<1)
			return false;

		int maxTicks = getConfiguredProcessTime();
		int totalEnergy = getConfiguredProcessEnergy();
		AutoWorkbenchProcess process = new AutoWorkbenchProcess(recipe.output.copy(), consumed, maxTicks, totalEnergy);
		commitComponentInventory(query);
		processQueue.add(process);
		return true;
	}
	private ItemStack[] copyComponentInventory()
	{
		ItemStack[] query = new ItemStack[16];
		for(int i=0; i<query.length; i++)
			query[i] = inventory[i+1]!=null?inventory[i+1].copy():null;
		return query;
	}

	private void commitComponentInventory(ItemStack[] query)
	{
		for(int i=0; i<query.length; i++)
			inventory[i+1] = query[i]!=null?query[i].copy():null;
	}

	private int getConfiguredProcessTime()
	{
		double modifier = Math.max(.001d, Config.getDouble("autoWorkbench_timeModifier"));
		return Math.max(1, (int)Math.floor(BASE_PROCESS_TIME*modifier));
	}

	private int getConfiguredProcessEnergy()
	{
		double modifier = Math.max(.001d, Config.getDouble("autoWorkbench_energyModifier"));
		return Math.max(1, (int)Math.floor(BASE_PROCESS_ENERGY*modifier));
	}

	private void finishProcess(AutoWorkbenchProcess process)
	{
		ItemStack output = process.output!=null?process.output.copy():null;
		if(output==null || output.stackSize<=0)
			return;
		int outputSide = mirrored?MultiblockAutoWorkbench.rotateYCCW(facing):MultiblockAutoWorkbench.rotateY(facing);
		ForgeDirection direction = ForgeDirection.getOrientation(outputSide);
		int outputX = xCoord+direction.offsetX*2;
		int outputZ = zCoord+direction.offsetZ*2;
		TileEntity target = Utils.getExistingTileEntity(worldObj, outputX, yCoord, outputZ);
		if(target instanceof IInventory)
			output = Utils.insertStackIntoInventory((IInventory)target, output, ForgeDirection.OPPOSITES[outputSide]);
		if(output!=null && output.stackSize>0)
		{
			EntityItem entity = new EntityItem(worldObj, outputX+.5, yCoord+.5, outputZ+.5, output.copy());
			entity.motionX = .075f*direction.offsetX;
			entity.motionY = .025f;
			entity.motionZ = .075f*direction.offsetZ;
			worldObj.spawnEntityInWorld(entity);
		}
	}

	private void markMasterForUpdate()
	{
		markDirty();
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public boolean isRSDisabled()
	{
		TileEntityAutoWorkbench master = getMasterOrSelf();
		if(master==null)
			return true;
		if(master!=this)
			return master.isRSDisabled();
		TileEntityAutoWorkbench control = getPart(REDSTONE_POS);
		boolean powered = control!=null && worldObj.isBlockIndirectlyGettingPowered(control.xCoord, control.yCoord, control.zCoord);
		return redstoneControlInverted!=powered;
	}

	public boolean toggleRedstoneControl()
	{
		TileEntityAutoWorkbench master = getMasterOrSelf();
		if(master==null)
			return false;
		master.redstoneControlInverted = !master.redstoneControlInverted;
		master.markMasterForUpdate();
		return true;
	}
	public TileEntityAutoWorkbench getPart(int targetPos)
	{
		TileEntityAutoWorkbench master = getMasterOrSelf();
		if(master==null || targetPos<0 || targetPos>=18)
			return null;
		int[] coords = master.getPartCoordinates(targetPos);
		TileEntity te = Utils.getExistingTileEntity(worldObj, coords[0], coords[1], coords[2]);
		return te instanceof TileEntityAutoWorkbench?(TileEntityAutoWorkbench)te:null;
	}

	private int[] getPartCoordinates(int targetPos)
	{
		int targetH = targetPos/9;
		int targetL = targetPos%9/3;
		int targetW = targetPos%3;
		int deltaH = targetH-1;
		int deltaL = targetL-1;
		int deltaW = targetW-1;
		if(mirrored)
			deltaW = -deltaW;
		ForgeDirection forward = ForgeDirection.getOrientation(facing);
		ForgeDirection right = ForgeDirection.getOrientation(MultiblockAutoWorkbench.rotateY(facing));
		return new int[]{
			xCoord+forward.offsetX*deltaL+right.offsetX*deltaW,
			yCoord+deltaH,
			zCoord+forward.offsetZ*deltaL+right.offsetZ*deltaW
		};
	}
	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		facing = nbt.getInteger("facing");
		if(!isMasterPart())
			return;

		selectedRecipe = nbt.getInteger("selectedRecipe");
		redstoneControlInverted = nbt.getBoolean("redstoneControlInverted");
		energyStorage.readFromNBT(nbt);
		inventory = Utils.readInventory(nbt.getTagList("inventory", 10), INVENTORY_SIZE);

		processQueue.clear();
		NBTTagList processes = nbt.getTagList("processQueue", 10);
		for(int i=0; i<processes.tagCount() && processQueue.size()<PROCESS_QUEUE_MAX; i++)
		{
			AutoWorkbenchProcess process = AutoWorkbenchProcess.readFromNBT(processes.getCompoundTagAt(i));
			if(process!=null)
				processQueue.add(process);
		}
		clientTickedProcesses = descPacket?nbt.getInteger("tickedProcesses"):0;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setInteger("facing", facing);
		if(!isMasterPart())
			return;
		nbt.setInteger("autoWorkbenchDataVersion", PROCESS_DATA_VERSION);
		nbt.setInteger("selectedRecipe", selectedRecipe);
		nbt.setBoolean("redstoneControlInverted", redstoneControlInverted);
		energyStorage.writeToNBT(nbt);
		nbt.setTag("inventory", Utils.writeInventory(inventory));

		NBTTagList processes = new NBTTagList();
		for(AutoWorkbenchProcess process : processQueue)
			processes.appendTag(process.writeToNBT(new NBTTagCompound()));
		nbt.setTag("processQueue", processes);
		if(descPacket)
			nbt.setInteger("tickedProcesses", clientTickedProcesses);
	}

	@Override
	public void receiveMessageFromClient(NBTTagCompound message)
	{
		TileEntityAutoWorkbench master = getMasterOrSelf();
		if(master==null || master!=this || !message.hasKey("recipe"))
			return;
		int requested = message.getInteger("recipe");
		if(requested<-1)
			requested = -1;
		if(requested>=0)
		{
			if(inventory[0]==null || !(inventory[0].getItem() instanceof ItemEngineersBlueprint))
				requested = -1;
			else
			{
				String category = ItemNBTHelper.getString(inventory[0], "blueprint");
				if(requested>=BlueprintCraftingRecipe.findRecipes(category).length)
					requested = -1;
			}
		}
		selectedRecipe = requested;
		markMasterForUpdate();
	}
	@Override
	public float[] getBlockBounds()
	{
		if(pos<10 || pos==12)
			return new float[]{0,0,0,1,1,1};
		if(pos>=13 && pos<=16)
			return new float[]{0,0,0,1,.125f,1};

		float xMin=0, yMin=0, zMin=0, xMax=1, yMax=1, zMax=1;
		if(pos==10 || pos==11)
		{
			yMax = .8125f;
			if(facing==2)
			{
				zMin = .1875f;
				if(pos==11) xMax = .875f;
			}
			else if(facing==3)
			{
				zMax = .8125f;
				if(pos==11) xMin = .125f;
			}
			else if(facing==4)
			{
				xMin = .1875f;
				if(pos==11) zMin = .125f;
			}
			else if(facing==5)
			{
				xMax = .8125f;
				if(pos==11) zMax = .875f;
			}
		}
		if(pos==17)
		{
			yMax = .3125f;
			if(facing==2)
			{
				zMin = .25f;
				xMax = .875f;
			}
			else if(facing==3)
			{
				zMax = .75f;
				xMin = .125f;
			}
			else if(facing==4)
			{
				xMin = .25f;
				zMin = .125f;
			}
			else if(facing==5)
			{
				xMax = .75f;
				zMax = .875f;
			}
		}
		return new float[]{xMin,yMin,zMin,xMax,yMax,zMax};
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(!isMasterPart())
			return super.getRenderBoundingBox();
		int[] origin = getPartCoordinates(0);
		ForgeDirection forward = ForgeDirection.getOrientation(facing);
		int widthSide = mirrored?MultiblockAutoWorkbench.rotateYCCW(facing):MultiblockAutoWorkbench.rotateY(facing);
		ForgeDirection width = ForgeDirection.getOrientation(widthSide);
		int endX = origin[0]+forward.offsetX*3+width.offsetX*3;
		int endY = origin[1]+2;
		int endZ = origin[2]+forward.offsetZ*3+width.offsetZ*3;
		return AxisAlignedBB.getBoundingBox(
			Math.min(origin[0],endX), Math.min(origin[1],endY), Math.min(origin[2],endZ),
			Math.max(origin[0],endX), Math.max(origin[1],endY), Math.max(origin[2],endZ));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public double getMaxRenderDistanceSquared()
	{
		return super.getMaxRenderDistanceSquared()*Config.getDouble("increasedTileRenderdistance");
	}

	@Override
	public int getSizeInventory()
	{
		TileEntityAutoWorkbench master = getMasterOrSelf();
		return formed && master!=null?INVENTORY_SIZE:0;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		TileEntityAutoWorkbench master = getMasterOrSelf();
		return master!=null && slot>=0 && slot<INVENTORY_SIZE?master.inventory[slot]:null;
	}
	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		TileEntityAutoWorkbench master = getMasterOrSelf();
		if(master==null || slot<0 || slot>=INVENTORY_SIZE || amount<=0)
			return null;
		ItemStack stack = master.inventory[slot];
		if(stack==null)
			return null;
		ItemStack ret;
		if(stack.stackSize<=amount)
		{
			ret = stack;
			master.inventory[slot] = null;
		}
		else
		{
			ret = stack.splitStack(amount);
			if(stack.stackSize<=0)
				master.inventory[slot] = null;
		}
		master.markMasterForUpdate();
		return ret;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		TileEntityAutoWorkbench master = getMasterOrSelf();
		if(master==null || slot<0 || slot>=INVENTORY_SIZE)
			return null;
		ItemStack stack = master.inventory[slot];
		master.inventory[slot] = null;
		if(stack!=null)
			master.markMasterForUpdate();
		return stack;
	}
	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		TileEntityAutoWorkbench master = getMasterOrSelf();
		if(master==null || slot<0 || slot>=INVENTORY_SIZE)
			return;
		int limit = slot==0?1:master.getInventoryStackLimit();
		if(stack!=null && stack.stackSize>limit)
			stack.stackSize = limit;
		master.inventory[slot] = stack;
		master.markMasterForUpdate();
	}

	@Override
	public String getInventoryName()
	{
		return "container.ImmersiveEngineering.autoWorkbench";
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
		TileEntityAutoWorkbench master = getMasterOrSelf();
		return master!=null && master.worldObj.getTileEntity(master.xCoord, master.yCoord, master.zCoord)==master
				&& player.getDistanceSq(master.xCoord+.5, master.yCoord+.5, master.zCoord+.5)<=64;
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
		if(stack==null || slot<0 || slot>=INVENTORY_SIZE)
			return false;
		if(slot==0)
			return stack.getItem() instanceof ItemEngineersBlueprint;
		return true;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		if(!formed || pos!=ENERGY_POS || master()==null)
			return new int[0];
		return new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		return formed && pos==ENERGY_POS && slot>=1 && slot<INVENTORY_SIZE && isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		return false;
	}
	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return formed && pos==ENERGY_POS && master()!=null;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		if(!formed || pos!=ENERGY_POS)
			return 0;
		TileEntityAutoWorkbench master = master();
		if(master==null)
			return 0;
		int received = master.energyStorage.receiveEnergy(maxReceive, simulate);
		if(received>0 && !simulate)
			master.markDirty();
		return received;
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		TileEntityAutoWorkbench master = getMasterOrSelf();
		return master!=null?master.energyStorage.getEnergyStored():0;
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		TileEntityAutoWorkbench master = getMasterOrSelf();
		return master!=null?master.energyStorage.getMaxEnergyStored():0;
	}
	public void disassemble(int brokenX, int brokenY, int brokenZ)
	{
		if(!formed || worldObj==null || worldObj.isRemote)
			return;
		TileEntityAutoWorkbench master = getMasterOrSelf();
		if(master==null || master.disassembling)
			return;
		master.disassembling = true;

		for(int targetPos=0; targetPos<18; targetPos++)
		{
			int[] coords = master.getPartCoordinates(targetPos);
			TileEntity te = Utils.getExistingTileEntity(worldObj, coords[0], coords[1], coords[2]);
			if(te instanceof TileEntityAutoWorkbench)
			{
				TileEntityAutoWorkbench part = (TileEntityAutoWorkbench)te;
				if(belongsToMaster(part, master, coords))
				{
					part.formed = false;
					part.disassembling = true;
				}
			}
		}

		for(int targetPos=0; targetPos<18; targetPos++)
		{
			int[] coords = master.getPartCoordinates(targetPos);
			boolean brokenPart = coords[0]==brokenX && coords[1]==brokenY && coords[2]==brokenZ;
			TileEntity te = Utils.getExistingTileEntity(worldObj, coords[0], coords[1], coords[2]);
			if(!brokenPart && (!(te instanceof TileEntityAutoWorkbench)
					|| !belongsToMaster((TileEntityAutoWorkbench)te, master, coords)))
				continue;
			ItemStack original = MultiblockAutoWorkbench.instance.getStructureManual()[targetPos/9][targetPos%9/3][targetPos%3];
			if(original==null || Block.getBlockFromItem(original.getItem())==null)
				continue;
			original = original.copy();
			if(brokenPart)
			{
				if(worldObj.getGameRules().getGameRuleBooleanValue("doTileDrops"))
					worldObj.spawnEntityInWorld(new EntityItem(worldObj, brokenX+.5, brokenY+.5, brokenZ+.5, original));
				continue;
			}
			restoreStructurePart(master, targetPos, coords, original);
		}
	}

	private boolean belongsToMaster(TileEntityAutoWorkbench part, TileEntityAutoWorkbench master, int[] coords)
	{
		return part.offset!=null && part.offset.length>=3
				&& part.offset[0]==coords[0]-master.xCoord
				&& part.offset[1]==coords[1]-master.yCoord
				&& part.offset[2]==coords[2]-master.zCoord;
	}

	private void restoreStructurePart(TileEntityAutoWorkbench master, int targetPos, int[] coords, ItemStack original)
	{
		Block originalBlock = Block.getBlockFromItem(original.getItem());
		if(worldObj.getBlock(coords[0], coords[1], coords[2])==IEContent.blockMetalMultiblocks)
			worldObj.setBlockToAir(coords[0], coords[1], coords[2]);
		worldObj.setBlock(coords[0], coords[1], coords[2], originalBlock, original.getItemDamage(), 3);

		TileEntity restored = worldObj.getTileEntity(coords[0], coords[1], coords[2]);
		if(restored instanceof TileEntityConveyorBelt)
		{
			int l = targetPos%9/3;
			int w = targetPos%3;
			int expectedFacing = (l==1 || (l==2 && w==0))
					?MultiblockAutoWorkbench.rotateY(master.facing)
					:ForgeDirection.OPPOSITES[master.facing];
			((TileEntityConveyorBelt)restored).facing = expectedFacing;
			restored.markDirty();
			worldObj.markBlockForUpdate(coords[0], coords[1], coords[2]);
		}
	}

	public static class AutoWorkbenchProcess
	{
		public int processTick;
		public final int maxTicks;
		public final int totalEnergy;
		public final int energyPerTick;
		public final ItemStack output;
		public final ItemStack[] displayItems;
		public final float transformationPoint;
		public AutoWorkbenchProcess(ItemStack output, ItemStack[] displayItems, int maxTicks, int totalEnergy)
		{
			this(output, displayItems, 0, maxTicks, totalEnergy,
					maxTicks>0?totalEnergy/maxTicks:0, TRANSFORMATION_POINT);
		}

		private AutoWorkbenchProcess(ItemStack output, ItemStack[] displayItems, int processTick,
				int maxTicks, int totalEnergy, int energyPerTick, float transformationPoint)
		{
			this.output = output;
			this.displayItems = displayItems;
			this.processTick = processTick;
			this.maxTicks = maxTicks;
			this.totalEnergy = totalEnergy;
			this.energyPerTick = energyPerTick;
			this.transformationPoint = transformationPoint;
		}

		public float getProgress(float partialTicks)
		{
			if(maxTicks<=0)
				return 0;
			return Math.min(1, (processTick+partialTicks)/(float)maxTicks);
		}

		public boolean displaysOutput(float partialTicks)
		{
			return getProgress(partialTicks)>transformationPoint;
		}
		public NBTTagCompound writeToNBT(NBTTagCompound tag)
		{
			tag.setInteger("processDataVersion", PROCESS_DATA_VERSION);
			tag.setInteger("processTick", processTick);
			tag.setInteger("maxTicks", maxTicks);
			tag.setInteger("totalEnergy", totalEnergy);
			tag.setInteger("energyPerTick", energyPerTick);
			tag.setFloat("transformationPoint", transformationPoint);
			if(output!=null)
				tag.setTag("output", output.writeToNBT(new NBTTagCompound()));
			tag.setInteger("displayCount", displayItems!=null?displayItems.length:0);
			if(displayItems!=null)
				tag.setTag("displayItems", Utils.writeInventory(displayItems));
			return tag;
		}

		public static AutoWorkbenchProcess readFromNBT(NBTTagCompound tag)
		{
			int version = tag.getInteger("processDataVersion");
			if(version<1 || version>PROCESS_DATA_VERSION)
				return null;
			int maxTicks = tag.getInteger("maxTicks");
			int totalEnergy = tag.getInteger("totalEnergy");
			int energyPerTick = tag.getInteger("energyPerTick");
			int displayCount = tag.getInteger("displayCount");
			if(maxTicks<=0 || totalEnergy<0 || energyPerTick<0 || displayCount<1)
				return null;
			if(!tag.hasKey("output"))
				return null;
			ItemStack output = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("output"));
			if(output==null || output.stackSize<=0)
				return null;
			ItemStack[] displayItems = Utils.readInventory(tag.getTagList("displayItems", 10), displayCount);
			boolean hasDisplayItem = false;
			for(ItemStack display : displayItems)
				if(display!=null && display.stackSize>0)
				{
					hasDisplayItem = true;
					break;
				}
			if(!hasDisplayItem)
				return null;
			float transformationPoint = tag.getFloat("transformationPoint");
			int processTick = Math.max(0, tag.getInteger("processTick"));
			return new AutoWorkbenchProcess(output, displayItems, processTick,
					maxTicks, totalEnergy, energyPerTick, transformationPoint);
		}
	}
}
