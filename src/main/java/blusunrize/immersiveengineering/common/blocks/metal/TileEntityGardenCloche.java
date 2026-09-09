package blusunrize.immersiveengineering.common.blocks.metal;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.FluidFertilizerHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.IPlantHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.ItemFertilizerHandler;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

/**
 * 1.7.10 implementation of the mature Garden Cloche runtime contract.
 *
 * Common code intentionally has no references to client renderer classes.
 */
public class TileEntityGardenCloche extends TileEntityIEBase implements IEnergyReceiver, IFluidHandler, ISidedInventory
{
	public static final int DATA_VERSION = 1;
	public static final int SLOT_SOIL = 0;
	public static final int SLOT_SEED = 1;
	public static final int SLOT_FERTILIZER = 2;
	public static final int SLOT_OUTPUT_START = 3;
	public static final int SLOT_OUTPUT_END = 6;
	public static final int INVENTORY_SIZE = 7;

	private static final int[] NO_SLOTS = new int[0];
	private static final int[] FERTILIZER_SLOT = new int[]{SLOT_FERTILIZER};
	private static final int[] OUTPUT_SLOTS = new int[]{3,4,5,6};

	public int facing = 2;
	public int dummy = 0;
	public ItemStack[] inventory = new ItemStack[INVENTORY_SIZE];
	public final EnergyStorage energyStorage = new EnergyStorage(16000, 256);
	public final FluidTank tank = new FluidTank(4000);

	public int fertilizerAmount;
	public float fertilizerMod = 1f;
	private float growth;
	private float renderGrowth;
	public boolean renderActive;

	private IPlantHandler currentPlantHandler;
	public transient boolean breaking;
	public transient boolean suppressMachineDrop;
	private transient AxisAlignedBB renderBB;

	public TileEntityGardenCloche()
	{
		BelljarHandler.init();
	}

	@Override
	public void updateEntity()
	{
		if(worldObj==null || dummy!=0)
			return;

		if(worldObj.isRemote)
		{
			updateClientGrowth();
			return;
		}

		if(worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
		{
			setRenderActive(false);
			return;
		}

		ItemStack seed = inventory[SLOT_SEED];
		ItemStack soil = inventory[SLOT_SOIL];
		boolean consumedTick = false;

		if(seed!=null)
		{
			IPlantHandler handler = getCurrentPlantHandler();
			if(handler!=null && handler.isCorrectSoil(seed, soil) && fertilizerAmount>0 && canConsumeEnergy())
			{
				if(growth>=1f)
				{
					ItemStack[] outputs = handler.getOutput(seed, soil, this);
					if(commitOutputsAtomically(outputs))
					{
						growth = handler.resetGrowth(seed, soil, growth, this);
						consumedTick = true;
					}
				}
				else
				{
					growth += getGrowthModifier()*BelljarHandler.getSoilGrowthMultiplier(seed, soil, this)*handler.getGrowthStep(seed, soil, growth, this, fertilizerMod);
					if(growth>1f)
						growth = 1f;
					consumedTick = true;
				}
			}
			else if(handler==null || !handler.isCorrectSoil(seed, soil))
				growth = 0f;
		}
		else
			growth = 0f;

		if(consumedTick)
		{
			energyStorage.extractEnergy(getEnergyConsumption(), false);
			fertilizerAmount--;
			setRenderActive(true);
			markDirty();
			if((worldObj.getTotalWorldTime()&31)==((xCoord^zCoord)&31))
				markForUpdate();
		}
		else
			setRenderActive(false);

		if(seed!=null && fertilizerAmount<=0 && tank.getFluidAmount()>=getFluidPerDose())
			refillFertilizerDose(seed, soil);

		if(worldObj.getTotalWorldTime()%8==0)
			autoEjectOutputs();
	}

	private void updateClientGrowth()
	{
		if(!renderActive)
		{
			renderGrowth = growth;
			return;
		}
		ItemStack seed = inventory[SLOT_SEED];
		ItemStack soil = inventory[SLOT_SOIL];
		IPlantHandler handler = getCurrentPlantHandler();
		if(seed==null || handler==null || !handler.isCorrectSoil(seed, soil))
		{
			renderGrowth = 0f;
			return;
		}
		if(renderGrowth<1f)
		{
			renderGrowth += getGrowthModifier()*BelljarHandler.getSoilGrowthMultiplier(seed, soil, this)*handler.getGrowthStep(seed, soil, renderGrowth, this, fertilizerMod);
			if(renderGrowth>1f)
				renderGrowth = 1f;
		}
		else
			renderGrowth = handler.resetGrowth(seed, soil, renderGrowth, this);
	}

	private boolean canConsumeEnergy()
	{
		int consumption = getEnergyConsumption();
		return consumption<=0 || energyStorage.extractEnergy(consumption, true)==consumption;
	}

	private void refillFertilizerDose(ItemStack seed, ItemStack soil)
	{
		FluidStack fluid = tank.getFluid();
		FluidFertilizerHandler fluidHandler = BelljarHandler.getFluidFertilizerHandler(fluid);
		if(fluidHandler==null)
			return;

		fertilizerMod = fluidHandler.getGrowthMultiplier(fluid, seed, soil, this);
		tank.drain(getFluidPerDose(), true);

		ItemStack fertilizer = inventory[SLOT_FERTILIZER];
		if(fertilizer!=null)
		{
			ItemFertilizerHandler itemHandler = BelljarHandler.getItemFertilizerHandler(fertilizer);
			if(itemHandler!=null)
			{
				fertilizerMod *= itemHandler.getGrowthMultiplier(fertilizer, seed, soil, this);
				fertilizer.stackSize--;
				if(fertilizer.stackSize<=0)
					inventory[SLOT_FERTILIZER] = null;
			}
		}
		fertilizerAmount = getFertilizerDuration();
		markDirty();
		markForUpdate();
	}

	private boolean commitOutputsAtomically(ItemStack[] outputs)
	{
		if(outputs==null)
			return false;

		ItemStack[] staged = new ItemStack[4];
		for(int i=0; i<4; i++)
			staged[i] = inventory[SLOT_OUTPUT_START+i]==null?null:inventory[SLOT_OUTPUT_START+i].copy();

		for(ItemStack output : outputs)
		{
			if(output==null || output.stackSize<=0)
				continue;
			ItemStack remainder = insertIntoStaged(staged, output.copy());
			if(remainder!=null && remainder.stackSize>0)
				return false;
		}

		for(int i=0; i<4; i++)
			inventory[SLOT_OUTPUT_START+i] = staged[i];
		markDirty();
		markForUpdate();
		return true;
	}

	private ItemStack insertIntoStaged(ItemStack[] staged, ItemStack stack)
	{
		for(int i=0; i<staged.length && stack!=null; i++)
		{
			ItemStack existing = staged[i];
			if(existing!=null && canStacksMerge(existing, stack))
			{
				int limit = Math.min(getInventoryStackLimit(), existing.getMaxStackSize());
				int accepted = Math.min(stack.stackSize, limit-existing.stackSize);
				if(accepted>0)
				{
					existing.stackSize += accepted;
					stack.stackSize -= accepted;
					if(stack.stackSize<=0)
						stack = null;
				}
			}
		}
		for(int i=0; i<staged.length && stack!=null; i++)
			if(staged[i]==null)
			{
				int accepted = Math.min(stack.stackSize, Math.min(getInventoryStackLimit(), stack.getMaxStackSize()));
				staged[i] = Utils.copyStackWithAmount(stack, accepted);
				stack.stackSize -= accepted;
				if(stack.stackSize<=0)
					stack = null;
			}
		return stack;
	}

	private static boolean canStacksMerge(ItemStack a, ItemStack b)
	{
		return a!=null && b!=null && a.getItem()==b.getItem() && a.getItemDamage()==b.getItemDamage() && ItemStack.areItemStackTagsEqual(a, b);
	}

	private void autoEjectOutputs()
	{
		ForgeDirection back = ForgeDirection.getOrientation(facing).getOpposite();
		TileEntity target = worldObj.getTileEntity(xCoord+back.offsetX, yCoord+1, zCoord+back.offsetZ);
		if(!(target instanceof net.minecraft.inventory.IInventory))
			return;

		net.minecraft.inventory.IInventory targetInventory = (net.minecraft.inventory.IInventory)target;
		boolean changed = false;
		for(int slot=SLOT_OUTPUT_START; slot<=SLOT_OUTPUT_END; slot++)
		{
			ItemStack stored = inventory[slot];
			if(stored==null)
				continue;
			int attempt = Math.min(stored.stackSize, 16);
			ItemStack moving = Utils.copyStackWithAmount(stored, attempt);
			ItemStack remainder = Utils.insertStackIntoInventory(targetInventory, moving, facing);
			int remaining = remainder==null?0:remainder.stackSize;
			int inserted = attempt-remaining;
			if(inserted>0)
			{
				stored.stackSize -= inserted;
				if(stored.stackSize<=0)
					inventory[slot] = null;
				changed = true;
			}
		}
		if(changed)
		{
			markDirty();
			markForUpdate();
		}
	}

	private void setRenderActive(boolean active)
	{
		if(renderActive!=active)
		{
			renderActive = active;
			markForUpdate();
		}
	}

	private void markForUpdate()
	{
		if(worldObj!=null)
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	public IPlantHandler getCurrentPlantHandler()
	{
		ItemStack seed = inventory[SLOT_SEED];
		if(seed==null)
		{
			currentPlantHandler = null;
			return null;
		}
		if(currentPlantHandler==null || !currentPlantHandler.isValid(seed))
			currentPlantHandler = BelljarHandler.getHandler(seed);
		return currentPlantHandler;
	}

	public TileEntityGardenCloche getMaster()
	{
		if(dummy==0)
			return this;
		if(worldObj==null)
			return null;
		TileEntity te = worldObj.getTileEntity(xCoord, yCoord-dummy, zCoord);
		return te instanceof TileEntityGardenCloche?(TileEntityGardenCloche)te:null;
	}

	public void rotateFacing(boolean reverse)
	{
		TileEntityGardenCloche master = getMaster();
		if(master==null || master.worldObj==null || master.worldObj.isRemote)
			return;
		int next;
		if(reverse)
			next = master.facing==2?4:master.facing==4?3:master.facing==3?5:2;
		else
			next = master.facing==2?5:master.facing==5?3:master.facing==3?4:2;
		for(int i=0; i<3; i++)
		{
			TileEntity part = master.worldObj.getTileEntity(master.xCoord, master.yCoord+i, master.zCoord);
			if(part instanceof TileEntityGardenCloche)
			{
				TileEntityGardenCloche clochePart = (TileEntityGardenCloche)part;
				clochePart.facing = next;
				clochePart.markDirty();
				master.worldObj.markBlockForUpdate(clochePart.xCoord, clochePart.yCoord, clochePart.zCoord);
			}
		}
	}

	public float getGrowth()
	{
		return growth;
	}

	@SideOnly(Side.CLIENT)
	public float getRenderGrowth()
	{
		return renderGrowth;
	}

	public void receiveContainerUpdate(int id, int value)
	{
		switch(id)
		{
		case 0:
			energyStorage.setEnergyStored(value);
			break;
		case 1:
			tank.setFluid(value<=0?null:new FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER, value));
			break;
		case 2:
			fertilizerAmount = value;
			break;
		case 3:
			fertilizerMod = value/1000f;
			break;
		case 4:
			growth = value/10000f;
			renderGrowth = growth;
			break;
		case 5:
			renderActive = value!=0;
			break;
		default:
			break;
		}
	}

	public ResourceLocation getSoilTexture()
	{
		ItemStack soil = inventory[SLOT_SOIL];
		IPlantHandler handler = getCurrentPlantHandler();
		ResourceLocation texture = handler==null?null:handler.getSoilTexture(inventory[SLOT_SEED], soil, this);
		return texture!=null?texture:BelljarHandler.getSoilTexture(soil);
	}

	private static int getEnergyConsumption()
	{
		int configured = Config.getInt("gardenCloche_consumption");
		return configured>0?configured:8;
	}

	private static int getFertilizerDuration()
	{
		int configured = Config.getInt("gardenCloche_fertilizerDuration");
		return configured>0?configured:6000;
	}

	private static int getFluidPerDose()
	{
		int configured = Config.getInt("gardenCloche_fluidPerDose");
		return configured>0?configured:250;
	}

	public float getSoilGrowthMultiplier()
	{
		return BelljarHandler.getSoilGrowthMultiplier(inventory[SLOT_SEED], inventory[SLOT_SOIL], this);
	}

	public float getEffectiveGrowthMultiplier()
	{
		return getGrowthModifier()*fertilizerMod*getSoilGrowthMultiplier();
	}

	private static float getGrowthModifier()
	{
		double configured = Config.getDouble("gardenCloche_growthModifier");
		return configured>0?(float)configured:1f;
	}

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		facing = nbt.getInteger("facing");
		dummy = nbt.getInteger("dummy");
		inventory = Utils.readInventory(nbt.getTagList("inventory", 10), INVENTORY_SIZE);
		energyStorage.readFromNBT(nbt);
		if(nbt.hasKey("tank"))
			tank.readFromNBT(nbt.getCompoundTag("tank"));
		fertilizerAmount = nbt.getInteger("fertilizerAmount");
		fertilizerMod = nbt.hasKey("fertilizerMod")?nbt.getFloat("fertilizerMod"):1f;
		growth = nbt.getFloat("growth");
		renderGrowth = growth;
		renderActive = nbt.getBoolean("renderActive");
		currentPlantHandler = null;
		renderBB = null;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		nbt.setInteger("clocheDataVersion", DATA_VERSION);
		nbt.setInteger("facing", facing);
		nbt.setInteger("dummy", dummy);
		nbt.setTag("inventory", Utils.writeInventory(inventory));
		energyStorage.writeToNBT(nbt);
		nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
		nbt.setInteger("fertilizerAmount", fertilizerAmount);
		nbt.setFloat("fertilizerMod", fertilizerMod);
		nbt.setFloat("growth", growth);
		nbt.setBoolean("renderActive", renderActive);
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		TileEntityGardenCloche master = getMaster();
		if(master==null || !canAcceptEnergyFrom(from))
			return 0;
		return master.energyStorage.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		TileEntityGardenCloche master = getMaster();
		return master==null?0:master.energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		TileEntityGardenCloche master = getMaster();
		return master==null?0:master.energyStorage.getMaxEnergyStored();
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from)
	{
		return getMaster()!=null && canAcceptEnergyFrom(from);
	}

	private boolean canAcceptEnergyFrom(ForgeDirection from)
	{
		if(from==null || from==ForgeDirection.UNKNOWN)
			return true;
		TileEntityGardenCloche master = getMaster();
		if(master==null)
			return false;
		if(dummy==2)
			return from==ForgeDirection.UP;
		if(dummy!=0)
			return false;
		ForgeDirection front = ForgeDirection.getOrientation(master.facing);
		return (front.offsetX!=0 && from.offsetZ!=0) || (front.offsetZ!=0 && from.offsetX!=0);
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if(dummy!=0 || resource==null || !isFluidInputSide(from) || BelljarHandler.getFluidFertilizerHandler(resource)==null)
			return 0;
		int accepted = tank.fill(resource, doFill);
		if(doFill && accepted>0)
		{
			markDirty();
			markForUpdate();
		}
		return accepted;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return dummy==0 && isFluidInputSide(from) && fluid!=null && BelljarHandler.getFluidFertilizerHandler(new FluidStack(fluid, 1))!=null;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if(dummy!=0 || !isFluidInputSide(from))
			return new FluidTankInfo[0];
		return new FluidTankInfo[]{tank.getInfo()};
	}

	private boolean isFluidInputSide(ForgeDirection from)
	{
		if(from==null || from==ForgeDirection.UNKNOWN || from==ForgeDirection.UP || from==ForgeDirection.DOWN)
			return true;
		ForgeDirection front = ForgeDirection.getOrientation(facing);
		return (front.offsetX!=0 && from.offsetX!=0) || (front.offsetZ!=0 && from.offsetZ!=0);
	}

	@Override
	public int getSizeInventory()
	{
		TileEntityGardenCloche master = getMaster();
		return master==null?INVENTORY_SIZE:master.inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		TileEntityGardenCloche master = getMaster();
		return master==null || slot<0 || slot>=INVENTORY_SIZE?null:master.inventory[slot];
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		TileEntityGardenCloche master = getMaster();
		if(master==null || slot<0 || slot>=INVENTORY_SIZE || master.inventory[slot]==null)
			return null;
		ItemStack stack;
		if(master.inventory[slot].stackSize<=amount)
		{
			stack = master.inventory[slot];
			master.inventory[slot] = null;
		}
		else
		{
			stack = master.inventory[slot].splitStack(amount);
			if(master.inventory[slot].stackSize<=0)
				master.inventory[slot] = null;
		}
		master.onInventoryChanged(slot);
		return stack;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		TileEntityGardenCloche master = getMaster();
		if(master==null || slot<0 || slot>=INVENTORY_SIZE)
			return null;
		ItemStack stack = master.inventory[slot];
		master.inventory[slot] = null;
		master.onInventoryChanged(slot);
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack)
	{
		TileEntityGardenCloche master = getMaster();
		if(master==null || slot<0 || slot>=INVENTORY_SIZE)
			return;
		if(stack!=null && stack.stackSize>master.getInventoryStackLimit())
			stack.stackSize = master.getInventoryStackLimit();
		master.inventory[slot] = stack;
		master.onInventoryChanged(slot);
	}

	private void onInventoryChanged(int slot)
	{
		if(slot==SLOT_SOIL || slot==SLOT_SEED)
		{
			currentPlantHandler = null;
			growth = 0f;
			renderGrowth = 0f;
		}
		markDirty();
		markForUpdate();
	}

	@Override
	public String getInventoryName()
	{
		return "container.ImmersiveEngineering.gardenCloche";
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
		TileEntityGardenCloche master = getMaster();
		return master!=null && master.worldObj.getTileEntity(master.xCoord, master.yCoord, master.zCoord)==master && player.getDistanceSq(master.xCoord+.5, master.yCoord+.5, master.zCoord+.5)<=64;
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
		if(slot==SLOT_FERTILIZER)
			return BelljarHandler.getItemFertilizerHandler(stack)!=null;
		return slot==SLOT_SOIL || slot==SLOT_SEED;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int side)
	{
		TileEntityGardenCloche master = getMaster();
		if(master==null)
			return NO_SLOTS;
		ForgeDirection access = ForgeDirection.getOrientation(side);
		if(dummy==0 && isFluidInputSide(access))
			return FERTILIZER_SLOT;
		if(dummy==1 && access==ForgeDirection.getOrientation(master.facing).getOpposite())
			return OUTPUT_SLOTS;
		return NO_SLOTS;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side)
	{
		return slot==SLOT_FERTILIZER && isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side)
	{
		return slot>=SLOT_OUTPUT_START && slot<=SLOT_OUTPUT_END;
	}

	public List<ItemStack> getInventoryDrops()
	{
		List<ItemStack> drops = new ArrayList<ItemStack>();
		for(ItemStack stack : inventory)
			if(stack!=null && stack.stackSize>0)
				drops.add(stack.copy());
		return drops;
	}

	public void clearInventory()
	{
		for(int i=0; i<inventory.length; i++)
			inventory[i] = null;
		markDirty();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if(renderBB==null)
			renderBB = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+1, yCoord+3, zCoord+1);
		return renderBB;
	}
}
