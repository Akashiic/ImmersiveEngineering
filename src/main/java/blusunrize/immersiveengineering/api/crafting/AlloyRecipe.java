package blusunrize.immersiveengineering.api.crafting;

import java.util.ArrayList;
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

	public static final ArrayList<AlloyRecipe> recipeList = new ArrayList<AlloyRecipe>();

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
}
