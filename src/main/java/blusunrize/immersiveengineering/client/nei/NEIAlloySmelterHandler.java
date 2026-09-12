package blusunrize.immersiveengineering.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;

public class NEIAlloySmelterHandler extends TemplateRecipeHandler
{
	public class CachedAlloyRecipe extends CachedRecipe
	{
		private final PositionedStack input0;
		private final PositionedStack input1;
		private final PositionedStack output;
		private final int time;

		public CachedAlloyRecipe(AlloyRecipe recipe)
		{
			input0 = new PositionedStack(recipe.input0.getSizedStackList(), 33, 9);
			input1 = new PositionedStack(recipe.input1.getSizedStackList(), 61, 9);
			output = new PositionedStack(recipe.output, 115, 27);
			time = recipe.time;
		}

		@Override
		public PositionedStack getResult()
		{
			return output;
		}

		@Override
		public List<PositionedStack> getIngredients()
		{
			return Arrays.asList(input0, input1);
		}
	}

	@Override
	public void loadTransferRects()
	{
		transferRects.add(new RecipeTransferRect(new Rectangle(69, 20, 32, 28), "ieAlloySmelter"));
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId.equals(getOverlayIdentifier()))
		{
			for(AlloyRecipe recipe : AlloyRecipe.recipeList)
				if(recipe!=null)
					arecipes.add(new CachedAlloyRecipe(recipe));
		}
		else
			super.loadCraftingRecipes(outputId, results);
	}

	@Override
	public void loadCraftingRecipes(ItemStack result)
	{
		if(result==null)
			return;
		for(AlloyRecipe recipe : AlloyRecipe.recipeList)
			if(recipe!=null&&OreDictionary.itemMatches(recipe.output, result, true))
				arecipes.add(new CachedAlloyRecipe(recipe));
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		if(ingredient==null)
			return;
		for(AlloyRecipe recipe : AlloyRecipe.recipeList)
			if(recipe!=null&&(recipe.input0.matchesItemStackIgnoringSize(ingredient)
					||recipe.input1.matchesItemStackIgnoringSize(ingredient)))
				arecipes.add(new CachedAlloyRecipe(recipe));
	}

	@Override
	public String getRecipeName()
	{
		return StatCollector.translateToLocal("tile.ImmersiveEngineering.stoneDevice2.alloySmelter.name");
	}

	@Override
	public String getGuiTexture()
	{
		return "immersiveengineering:textures/gui/alloy_smelter.png";
	}

	@Override
	public String getOverlayIdentifier()
	{
		return "ieAlloySmelter";
	}

	@Override
	public int recipiesPerPage()
	{
		return 2;
	}

	@Override
	public void drawBackground(int recipe)
	{
		GL11.glColor4f(1, 1, 1, 1);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(-5, -8, 0, 8, 176, 68);
		CachedAlloyRecipe cached = (CachedAlloyRecipe)arecipes.get(recipe%arecipes.size());
		String timeLabel = cached.time+" Ticks";
		ClientUtils.font().drawString(timeLabel,
				88-ClientUtils.font().getStringWidth(timeLabel)/2, 51, 0xaaaaaa, true);
	}
}
