package blusunrize.immersiveengineering.client.gui;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAutoWorkbench;
import blusunrize.immersiveengineering.common.gui.ContainerAutoWorkbench;
import blusunrize.immersiveengineering.common.items.ItemEngineersBlueprint;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.network.MessageTileSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class GuiAutoWorkbench extends GuiContainer
{
	private static final String TEXTURE = "immersiveengineering:textures/gui/auto_workbench.png";
	public final TileEntityAutoWorkbench tile;
	private String lastBlueprintCategory = "";

	public GuiAutoWorkbench(InventoryPlayer playerInventory, TileEntityAutoWorkbench tile)
	{
		super(new ContainerAutoWorkbench(playerInventory, tile));
		this.tile = tile;
		this.ySize = 184;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		buttonList.clear();
		lastBlueprintCategory = getBlueprintCategory();
		Slot blueprintSlot = inventorySlots.getSlot(0);
		if(blueprintSlot==null || !blueprintSlot.getHasStack())
			return;
		ItemStack blueprint = blueprintSlot.getStack();
		if(!(blueprint.getItem() instanceof ItemEngineersBlueprint))
			return;
		BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(lastBlueprintCategory);
		if(recipes==null || recipes.length<1)
			return;
		int count = recipes.length;
		int startX = guiLeft+121;
		int startY = guiTop+(count>6?59-(count-3)/3*18:count>3?59:68);
		for(int i=0; i<count; i++)
		{
			BlueprintCraftingRecipe recipe = recipes[i];
			if(recipe!=null && recipe.output!=null && recipe.output.stackSize>0)
				buttonList.add(new RecipeButton(i, startX+(i%3)*18, startY+(i/3)*18,
						recipe.output.copy(), i==tile.selectedRecipe));
		}
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();
		String category = getBlueprintCategory();
		if(!category.equals(lastBlueprintCategory))
			initGui();
	}

	private String getBlueprintCategory()
	{
		Slot slot = inventorySlots!=null && inventorySlots.inventorySlots.size()>0?inventorySlots.getSlot(0):null;
		if(slot==null || !slot.getHasStack() || !(slot.getStack().getItem() instanceof ItemEngineersBlueprint))
			return "";
		return ItemNBTHelper.getString(slot.getStack(), "blueprint");
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if(!(button instanceof RecipeButton))
			return;
		tile.selectedRecipe = button.id==tile.selectedRecipe?-1:button.id;
		NBTTagCompound message = new NBTTagCompound();
		message.setInteger("recipe", tile.selectedRecipe);
		ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, message));
		initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		super.drawScreen(mouseX, mouseY, partialTicks);
		ArrayList<String> tooltip = new ArrayList<String>();
		if(mouseX>guiLeft+80 && mouseX<guiLeft+87 && mouseY>guiTop+36 && mouseY<guiTop+82)
			tooltip.add(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" RF");
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mouseX, mouseY, fontRendererObj, guiLeft+xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GL11.glColor4f(1f, 1f, 1f, 1f);
		ClientUtils.bindTexture(TEXTURE);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		int maxEnergy = Math.max(1, tile.getMaxEnergyStored(null));
		int stored = Math.max(0, Math.min(46, (int)(46*tile.getEnergyStored(null)/(float)maxEnergy)));
		ClientUtils.drawGradientRect(guiLeft+80, guiTop+36+(46-stored), guiLeft+87, guiTop+82,
				0xffb51500, 0xff600b00);
	}

	public static class RecipeButton extends GuiButton
	{
		private final ItemStack stack;
		private final boolean active;

		public RecipeButton(int id, int x, int y, ItemStack stack, boolean active)
		{
			super(id, x, y, 18, 18, "");
			this.stack = stack;
			this.active = active;
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY)
		{
			if(!visible)
				return;
			ClientUtils.bindTexture("immersiveengineering:textures/gui/auto_workbench_buttons.png");
			GL11.glColor4f(1f, 1f, 1f, 1f);
			field_146123_n = mouseX>=xPosition && mouseY>=yPosition
					&& mouseX<xPosition+width && mouseY<yPosition+height;
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			drawTexturedModalRect(xPosition, yPosition, 24+(active?18:0), 128, width, height);
			mouseDragged(mc, mouseX, mouseY);
			if(stack==null)
				return;
			RenderHelper.enableGUIStandardItemLighting();
			RenderItem renderer = RenderItem.getInstance();
			renderer.zLevel = 200f;
			FontRenderer font = stack.getItem().getFontRenderer(stack);
			if(font==null)
				font = mc.fontRenderer;
			renderer.renderItemAndEffectIntoGUI(font, mc.renderEngine, stack, xPosition+1, yPosition+1);
			renderer.zLevel = 0f;
			if(!active)
			{
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				ClientUtils.drawColouredRect(xPosition+1, yPosition+1, 16, 16, 0x77444444);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glEnable(GL11.GL_LIGHTING);
			}
		}
	}
}
