package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityAlloySmelter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAlloySmelter extends Container
{
	public final TileEntityAlloySmelter tile;
	private static final int SLOT_COUNT = TileEntityAlloySmelter.INVENTORY_SIZE;
	private int lastProcess = -1;
	private int lastProcessMax = -1;
	private int lastBurnTime = -1;
	private int lastBurnTimeMax = -1;

	public ContainerAlloySmelter(InventoryPlayer playerInventory, TileEntityAlloySmelter tile)
	{
		this.tile = tile;
		addSlotToContainer(new Slot(tile, 0, 38, 17)
		{
			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return AlloyRecipe.isValidInput(stack);
			}
		});
		addSlotToContainer(new Slot(tile, 1, 66, 17)
		{
			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return AlloyRecipe.isValidInput(stack);
			}
		});
		addSlotToContainer(new Slot(tile, 2, 52, 53)
		{
			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return net.minecraft.tileentity.TileEntityFurnace.isItemFuel(stack);
			}
		});
		addSlotToContainer(new IESlot.Output(this, tile, 3, 120, 35));

		for(int row=0; row<3; row++)
			for(int col=0; col<9; col++)
				addSlotToContainer(new Slot(playerInventory, col+row*9+9, 8+col*18, 84+row*18));
		for(int col=0; col<9; col++)
			addSlotToContainer(new Slot(playerInventory, col, 8+col*18, 142));
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
		for(Object object : crafters)
		{
			ICrafting crafter = (ICrafting)object;
			if(tile.process!=lastProcess)
				crafter.sendProgressBarUpdate(this, 0, tile.process);
			if(tile.processMax!=lastProcessMax)
				crafter.sendProgressBarUpdate(this, 1, tile.processMax);
			if(tile.burnTime!=lastBurnTime)
				crafter.sendProgressBarUpdate(this, 2, tile.burnTime);
			if(tile.lastBurnTime!=lastBurnTimeMax)
				crafter.sendProgressBarUpdate(this, 3, tile.lastBurnTime);
		}
		lastProcess = tile.process;
		lastProcessMax = tile.processMax;
		lastBurnTime = tile.burnTime;
		lastBurnTimeMax = tile.lastBurnTime;
	}

	@Override
	public void updateProgressBar(int id, int value)
	{
		tile.receiveContainerUpdate(id, value);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
	{
		Slot slot = (Slot)inventorySlots.get(slotIndex);
		if(slot==null||!slot.getHasStack())
			return null;
		ItemStack inSlot = slot.getStack();
		ItemStack original = inSlot.copy();

		if(slotIndex<SLOT_COUNT)
		{
			if(!mergeItemStack(inSlot, SLOT_COUNT, SLOT_COUNT+36, true))
				return null;
		}
		else
		{
			boolean moved;
			if(AlloyRecipe.isValidInput(inSlot))
				moved = mergeItemStack(inSlot, 0, 2, false);
			else if(net.minecraft.tileentity.TileEntityFurnace.isItemFuel(inSlot))
				moved = mergeItemStack(inSlot, 2, 3, false);
			else
				moved = false;
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
