package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.IPlantHandler;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityGardenCloche;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerGardenCloche extends Container
{
	public final TileEntityGardenCloche tile;
	private static final int SLOT_COUNT = TileEntityGardenCloche.INVENTORY_SIZE;

	private int lastEnergy = -1;
	private int lastTank = -1;
	private int lastFertilizer = -1;
	private int lastFertilizerMod = -1;
	private int lastGrowth = -1;
	private int lastActive = -1;

	public ContainerGardenCloche(InventoryPlayer playerInventory, TileEntityGardenCloche tile)
	{
		this.tile = tile;

		addSlotToContainer(new Slot(tile, TileEntityGardenCloche.SLOT_SOIL, 62, 54)
		{
			@Override
			public int getSlotStackLimit()
			{
				return 1;
			}
		});
		addSlotToContainer(new Slot(tile, TileEntityGardenCloche.SLOT_SEED, 62, 34)
		{
			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return BelljarHandler.getHandler(stack)!=null;
			}

			@Override
			public int getSlotStackLimit()
			{
				return 1;
			}
		});
		addSlotToContainer(new Slot(tile, TileEntityGardenCloche.SLOT_FERTILIZER, 8, 59)
		{
			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return BelljarHandler.getItemFertilizerHandler(stack)!=null;
			}
		});

		for(int i=0; i<4; i++)
			addSlotToContainer(new IESlot.Output(this, tile, TileEntityGardenCloche.SLOT_OUTPUT_START+i, 116+i%2*18, 34+i/2*18));

		for(int row=0; row<3; row++)
			for(int col=0; col<9; col++)
				addSlotToContainer(new Slot(playerInventory, col+row*9+9, 8+col*18, 85+row*18));
		for(int col=0; col<9; col++)
			addSlotToContainer(new Slot(playerInventory, col, 8+col*18, 143));
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return tile.isUseableByPlayer(player);
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		int energy = tile.energyStorage.getEnergyStored();
		int tankAmount = tile.tank.getFluidAmount();
		int fertilizer = tile.fertilizerAmount;
		int fertilizerMod = Math.round(tile.fertilizerMod*1000f);
		int growth = Math.round(tile.getGrowth()*10000f);
		int active = tile.renderActive?1:0;
		for(Object crafterObject : crafters)
		{
			ICrafting crafter = (ICrafting)crafterObject;
			if(energy!=lastEnergy)
				crafter.sendProgressBarUpdate(this, 0, energy);
			if(tankAmount!=lastTank)
				crafter.sendProgressBarUpdate(this, 1, tankAmount);
			if(fertilizer!=lastFertilizer)
				crafter.sendProgressBarUpdate(this, 2, fertilizer);
			if(fertilizerMod!=lastFertilizerMod)
				crafter.sendProgressBarUpdate(this, 3, fertilizerMod);
			if(growth!=lastGrowth)
				crafter.sendProgressBarUpdate(this, 4, growth);
			if(active!=lastActive)
				crafter.sendProgressBarUpdate(this, 5, active);
		}
		lastEnergy = energy;
		lastTank = tankAmount;
		lastFertilizer = fertilizer;
		lastFertilizerMod = fertilizerMod;
		lastGrowth = growth;
		lastActive = active;
	}

	@Override
	public void updateProgressBar(int id, int value)
	{
		tile.receiveContainerUpdate(id, value);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
	{
		ItemStack original = null;
		Slot slot = (Slot)inventorySlots.get(slotIndex);
		if(slot==null || !slot.getHasStack())
			return null;

		ItemStack inSlot = slot.getStack();
		original = inSlot.copy();
		if(slotIndex<SLOT_COUNT)
		{
			if(!mergeItemStack(inSlot, SLOT_COUNT, SLOT_COUNT+36, true))
				return null;
		}
		else
		{
			boolean moved = false;
			if(BelljarHandler.getItemFertilizerHandler(inSlot)!=null)
				moved = mergeItemStack(inSlot, TileEntityGardenCloche.SLOT_FERTILIZER, TileEntityGardenCloche.SLOT_FERTILIZER+1, false);
			else
			{
				IPlantHandler handler = BelljarHandler.getHandler(inSlot);
				if(handler!=null)
					moved = mergeItemStack(inSlot, TileEntityGardenCloche.SLOT_SEED, TileEntityGardenCloche.SLOT_SEED+1, false);
				else
				{
					ItemStack seed = tile.getStackInSlot(TileEntityGardenCloche.SLOT_SEED);
					IPlantHandler current = BelljarHandler.getHandler(seed);
					if(current!=null && current.isCorrectSoil(seed, inSlot))
						moved = mergeItemStack(inSlot, TileEntityGardenCloche.SLOT_SOIL, TileEntityGardenCloche.SLOT_SOIL+1, false);
				}
			}
			if(!moved)
				return null;
		}

		if(inSlot.stackSize==0)
			slot.putStack(null);
		else
			slot.onSlotChanged();
		if(inSlot.stackSize==original.stackSize)
			return null;
		slot.onPickupFromSlot(player, inSlot);
		return original;
	}
}
