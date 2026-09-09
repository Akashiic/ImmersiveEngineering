package blusunrize.immersiveengineering.client.gui;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityGardenCloche;
import blusunrize.immersiveengineering.common.gui.ContainerGardenCloche;
import blusunrize.immersiveengineering.common.util.Lib;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.StatCollector;

public class GuiGardenCloche extends GuiContainer
{
	private static final String TEXTURE = "immersiveengineering:textures/gui/belljar.png";
	public final TileEntityGardenCloche tile;

	public GuiGardenCloche(InventoryPlayer playerInventory, TileEntityGardenCloche tile)
	{
		super(new ContainerGardenCloche(playerInventory, tile));
		this.tile = tile;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		super.drawScreen(mouseX, mouseY, partialTicks);
		ArrayList<String> tooltip = new ArrayList<String>();
		ClientUtils.handleGuiTank(tile.tank, guiLeft+8, guiTop+8, 16, 47, 176, 30, 20, 51, mouseX, mouseY, TEXTURE, tooltip);

		if(mouseX>guiLeft+30 && mouseX<guiLeft+37 && mouseY>guiTop+22 && mouseY<guiTop+68)
		{
			int duration = Math.max(1, Config.getInt("gardenCloche_fertilizerDuration"));
			int percent = Math.round(100*tile.fertilizerAmount/(float)duration);
			tooltip.add(StatCollector.translateToLocalFormatted(Lib.DESC_INFO+"gardenClocheFertilizer", percent));
			tooltip.add(StatCollector.translateToLocalFormatted(Lib.DESC_INFO+"gardenClocheFertilizerMod", String.format("%.2f", tile.fertilizerMod)));
			tooltip.add(StatCollector.translateToLocalFormatted(Lib.DESC_INFO+"gardenClocheSoilMod", String.format("%.2f", tile.getSoilGrowthMultiplier())));
			tooltip.add(StatCollector.translateToLocalFormatted(Lib.DESC_INFO+"gardenClocheGrowthMod", String.format("%.2f", tile.getEffectiveGrowthMultiplier())));
		}
		if(mouseX>guiLeft+158 && mouseX<guiLeft+165 && mouseY>guiTop+22 && mouseY<guiTop+68)
			tooltip.add(tile.energyStorage.getEnergyStored()+"/"+tile.energyStorage.getMaxEnergyStored()+" RF");

		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj, guiLeft+xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		fontRendererObj.drawString(StatCollector.translateToLocal(Lib.GUI+"gardenCloche"), 8, 6, 0x404040);
		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize-96+2, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GL11.glColor4f(1f, 1f, 1f, 1f);
		ClientUtils.bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		ClientUtils.handleGuiTank(tile.tank, guiLeft+8, guiTop+8, 16, 47, 176, 30, 20, 51, mouseX, mouseY, TEXTURE, null);

		int duration = Math.max(1, Config.getInt("gardenCloche_fertilizerDuration"));
		int fertilizerPixels = Math.max(0, Math.min(46, Math.round(46*tile.fertilizerAmount/(float)duration)));
		ClientUtils.drawGradientRect(guiLeft+30, guiTop+22+(46-fertilizerPixels), guiLeft+37, guiTop+68, 0xff95ed00, 0xff8a5a00);

		int energyPixels = Math.max(0, Math.min(46, Math.round(46*tile.energyStorage.getEnergyStored()/(float)tile.energyStorage.getMaxEnergyStored())));
		ClientUtils.drawGradientRect(guiLeft+158, guiTop+22+(46-energyPixels), guiLeft+165, guiTop+68, 0xffb51500, 0xff600b00);
	}
}
