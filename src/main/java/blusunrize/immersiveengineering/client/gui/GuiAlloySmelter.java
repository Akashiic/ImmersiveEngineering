package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityAlloySmelter;
import blusunrize.immersiveengineering.common.gui.ContainerAlloySmelter;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import org.lwjgl.opengl.GL11;

/** 1.7 renderer for the official modern Alloy Kiln GUI texture. */
public class GuiAlloySmelter extends GuiContainer
{
	private final TileEntityAlloySmelter tile;

	public GuiAlloySmelter(InventoryPlayer playerInventory, TileEntityAlloySmelter tile)
	{
		super(new ContainerAlloySmelter(playerInventory, tile));
		this.tile = tile;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GL11.glColor4f(1, 1, 1, 1);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/alloy_smelter.png");
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		if(tile.lastBurnTime>0&&tile.burnTime>0)
		{
			int height = Math.min(12, Math.max(0,
					(int)Math.ceil(12*tile.burnTime/(double)tile.lastBurnTime)));
			drawTexturedModalRect(guiLeft+51, guiTop+36+12-height, 179, 1+12-height, 9, height);
		}
		if(tile.processMax>0)
		{
			int width = Math.min(22, Math.max(0,
					(int)Math.ceil(22*(tile.processMax-tile.process)/(double)tile.processMax)));
			drawTexturedModalRect(guiLeft+74, guiTop+35, 177, 14, width, 16);
		}
	}
}
