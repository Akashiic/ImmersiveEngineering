package blusunrize.immersiveengineering.common.util.compat.minetweaker;

import java.util.List;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersiveengineering.AlloySmelter")
public class AlloySmelter
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient first, IIngredient second, int time)
	{
		ItemStack out = MTHelper.toStack(output);
		IngredientStack in0 = toIngredient(first);
		IngredientStack in1 = toIngredient(second);
		if(out==null||in0==null||in1==null||time<=0)
			return;
		MineTweakerAPI.apply(new Add(new AlloyRecipe(out, in0, in1, time)));
	}

	private static IngredientStack toIngredient(IIngredient ingredient)
	{
		if(ingredient==null)
			return null;
		Object value = MTHelper.toObject(ingredient);
		if(value==null)
			return null;
		try
		{
			return IngredientStack.from(value).copyWithSize(Math.max(1, ingredient.getAmount()));
		}
		catch(IllegalArgumentException ex)
		{
			return null;
		}
	}

	@ZenMethod
	public static void removeRecipe(IItemStack output)
	{
		ItemStack stack = MTHelper.toStack(output);
		if(stack!=null)
			MineTweakerAPI.apply(new Remove(stack));
	}

	private static class Add implements IUndoableAction
	{
		private final AlloyRecipe recipe;

		private Add(AlloyRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			AlloyRecipe.recipeList.add(recipe);
		}

		@Override
		public void undo()
		{
			AlloyRecipe.recipeList.remove(recipe);
		}

		@Override
		public boolean canUndo()
		{
			return true;
		}

		@Override
		public String describe()
		{
			return "Adding Alloy Smelter Recipe for "+recipe.output.getDisplayName();
		}

		@Override
		public String describeUndo()
		{
			return "Removing Alloy Smelter Recipe for "+recipe.output.getDisplayName();
		}

		@Override
		public Object getOverrideKey()
		{
			return null;
		}
	}

	private static class Remove implements IUndoableAction
	{
		private final ItemStack output;
		private List<AlloyRecipe> removed;

		private Remove(ItemStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			removed = AlloyRecipe.removeRecipes(output);
		}

		@Override
		public void undo()
		{
			if(removed!=null)
				AlloyRecipe.recipeList.addAll(removed);
		}

		@Override
		public boolean canUndo()
		{
			return true;
		}

		@Override
		public String describe()
		{
			return "Removing Alloy Smelter Recipe for "+output.getDisplayName();
		}

		@Override
		public String describeUndo()
		{
			return "Re-adding Alloy Smelter Recipe for "+output.getDisplayName();
		}

		@Override
		public Object getOverrideKey()
		{
			return null;
		}
	}
}
