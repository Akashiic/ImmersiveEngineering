package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAutoWorkbench;
import blusunrize.immersiveengineering.common.items.ItemEngineersBlueprint;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAutoWorkbench extends Container
{
	public static final int MACHINE_SLOT_COUNT = TileEntityAutoWorkbench.INVENTORY_SIZE;
	public final TileEntityAutoWorkbench tile;

	public ContainerAutoWorkbench(InventoryPlayer playerInventory, TileEntityAutoWorkbench tile)
	{
		this.tile = tile;
		addSlotToContainer(new Slot(tile, 0, 102, 69)
		{
			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return stack!=null && stack.getItem() instanceof ItemEngineersBlueprint;
			}

			@Override
			public int getSlotStackLimit()
			{
				return 1;
			}
		});
		for(int i=0; i<16; i++)
			addSlotToContainer(new Slot(tile, i+1, 7+(i%4)*18, 24+(i/4)*18));

		for(int row=0; row<3; row++)
			for(int col=0; col<9; col++)
				addSlotToContainer(new Slot(playerInventory, col+row*9+9, 8+col*18, 103+row*18));
		for(int col=0; col<9; col++)
			addSlotToContainer(new Slot(playerInventory, col, 8+col*18, 161));
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return tile.isUseableByPlayer(player);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex)
	{
		if(slotIndex<0 || slotIndex>=inventorySlots.size())
			return null;
		Slot slot = (Slot)inventorySlots.get(slotIndex);
		if(slot==null || !slot.getHasStack())
			return null;

		ItemStack inSlot = slot.getStack();
		ItemStack original = inSlot.copy();
		if(slotIndex<MACHINE_SLOT_COUNT)
		{
			if(!mergeItemStack(inSlot, MACHINE_SLOT_COUNT, MACHINE_SLOT_COUNT+36, true))
				return null;
		}
		else if(inSlot.getItem() instanceof ItemEngineersBlueprint)
		{
			if(!mergeItemStack(inSlot, 0, 1, false))
				return null;
		}
		else if(!mergeItemStack(inSlot, 1, MACHINE_SLOT_COUNT, false))
			return null;

		if(inSlot.stackSize<=0)
			slot.putStack(null);
		else
			slot.onSlotChanged();
		if(inSlot.stackSize==original.stackSize)
			return null;
		slot.onPickupFromSlot(player, inSlot);
		return original;
	}
}
