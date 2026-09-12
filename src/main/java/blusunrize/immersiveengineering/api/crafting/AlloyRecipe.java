package blusunrize.immersiveengineering.api.crafting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Recipe for the Alloy Kiln. Backported from the official 1.12+ API contract.
 */
public class AlloyRecipe
{
	public final IngredientStack input0;
	public final IngredientStack input1;
	public final ItemStack output;
	public final int time;

	public AlloyRecipe(ItemStack output, Object input0, Object input1, int time)
	{
		this(output, IngredientStack.from(input0), IngredientStack.from(input1), time);
	}

	public AlloyRecipe(ItemStack output, IngredientStack input0, IngredientStack input1, int time)
	{
		if(output==null||input0==null||input1==null)
			throw new IllegalArgumentException("Alloy recipe output and inputs may not be null");
		this.output = output.copy();
		this.input0 = new IngredientStack(input0);
		this.input1 = new IngredientStack(input1);
		this.time = Math.max(1, time);
	}

	public boolean matches(ItemStack first, ItemStack second)
	{
		return input0.matchesItemStack(first)&&input1.matchesItemStack(second)
				||input0.matchesItemStack(second)&&input1.matchesItemStack(first);
	}

	public boolean firstInputMapsToInput0(ItemStack first, ItemStack second)
	{
		return input0.matchesItemStack(first)&&input1.matchesItemStack(second);
	}

	private static long recipeRevision;
	public static final ArrayList<AlloyRecipe> recipeList = new TrackedRecipeList();

	public static long getRecipeRevision()
	{
		return recipeRevision;
	}

	public static AlloyRecipe addRecipe(ItemStack output, Object input0, Object input1, int time)
	{
		return addRecipe(output, IngredientStack.from(input0), IngredientStack.from(input1), time);
	}

	public static AlloyRecipe addRecipe(ItemStack output, IngredientStack input0, IngredientStack input1, int time)
	{
		AlloyRecipe recipe = new AlloyRecipe(output, input0, input1, time);
		recipeList.add(recipe);
		return recipe;
	}

	public static AlloyRecipe findRecipe(ItemStack input0, ItemStack input1)
	{
		if(input0==null||input1==null)
			return null;
		for(AlloyRecipe recipe : recipeList)
			if(recipe.matches(input0, input1))
				return recipe;
		return null;
	}

	public static boolean isValidInput(ItemStack stack)
	{
		if(stack==null)
			return false;
		for(AlloyRecipe recipe : recipeList)
			if(recipe.input0.matchesItemStackIgnoringSize(stack)||recipe.input1.matchesItemStackIgnoringSize(stack))
				return true;
		return false;
	}

	public static List<AlloyRecipe> removeRecipes(ItemStack stack)
	{
		List<AlloyRecipe> removed = new ArrayList<AlloyRecipe>();
		if(stack==null)
			return removed;
		Iterator<AlloyRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			AlloyRecipe recipe = it.next();
			if(OreDictionary.itemMatches(recipe.output, stack, true))
			{
				removed.add(recipe);
				it.remove();
			}
		}
		return removed;
	}

	private static class TrackedRecipeList extends ArrayList<AlloyRecipe>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean add(AlloyRecipe recipe)
		{
			boolean changed = super.add(recipe);
			if(changed)
				recipeRevision++;
			return changed;
		}

		@Override
		public void add(int index, AlloyRecipe recipe)
		{
			super.add(index, recipe);
			recipeRevision++;
		}

		@Override
		public boolean addAll(Collection<? extends AlloyRecipe> recipes)
		{
			boolean changed = super.addAll(recipes);
			if(changed)
				recipeRevision++;
			return changed;
		}

		@Override
		public boolean addAll(int index, Collection<? extends AlloyRecipe> recipes)
		{
			boolean changed = super.addAll(index, recipes);
			if(changed)
				recipeRevision++;
			return changed;
		}

		@Override
		public AlloyRecipe remove(int index)
		{
			AlloyRecipe removed = super.remove(index);
			recipeRevision++;
			return removed;
		}

		@Override
		public boolean remove(Object recipe)
		{
			boolean changed = super.remove(recipe);
			if(changed)
				recipeRevision++;
			return changed;
		}

		@Override
		public AlloyRecipe set(int index, AlloyRecipe recipe)
		{
			AlloyRecipe previous = super.set(index, recipe);
			recipeRevision++;
			return previous;
		}

		@Override
		public void clear()
		{
			if(!isEmpty())
			{
				super.clear();
				recipeRevision++;
			}
		}

		@Override
		public boolean removeAll(Collection<?> recipes)
		{
			boolean changed = super.removeAll(recipes);
			if(changed)
				recipeRevision++;
			return changed;
		}

		@Override
		public boolean retainAll(Collection<?> recipes)
		{
			boolean changed = super.retainAll(recipes);
			if(changed)
				recipeRevision++;
			return changed;
		}
	}
}
