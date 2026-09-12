package blusunrize.immersiveengineering.tests;

import java.util.Arrays;
import java.util.Iterator;

import blusunrize.immersiveengineering.api.crafting.IngredientStack;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public final class MixerM1SelfTest
{
	private static final Item TEST_IRON = new Item();
	private static final Item TEST_GOLD = new Item();

	private MixerM1SelfTest()
	{
	}

	public static void main(String[] args)
	{
		testMixerConsumptionPlanner();
		testRecipeRevision();
		System.out.println("MIXER_M1_SELF_TEST_PASS");
	}

	private static void testMixerConsumptionPlanner()
	{
		IngredientStack broad = new IngredientStack(Arrays.asList(
				new ItemStack(TEST_IRON), new ItemStack(TEST_GOLD)), 1);
		IngredientStack ironOnly = new IngredientStack(new ItemStack(TEST_IRON));
		int[] exact = MixerRecipe.planItemConsumption(
				new IngredientStack[]{broad, ironOnly},
				new ItemStack[]{new ItemStack(TEST_IRON), new ItemStack(TEST_GOLD)});
		check(exact!=null&&exact.length==2&&exact[0]==1&&exact[1]==1,
				"overlapping ingredient assignment");

		IngredientStack threeIron = new IngredientStack(new ItemStack(TEST_IRON, 3));
		int[] split = MixerRecipe.planItemConsumption(
				new IngredientStack[]{threeIron},
				new ItemStack[]{new ItemStack(TEST_IRON, 2), new ItemStack(TEST_IRON, 1)});
		check(split!=null&&split[0]==2&&split[1]==1, "multi-slot quantity split");
		check(MixerRecipe.planItemConsumption(
				new IngredientStack[]{threeIron}, new ItemStack[]{new ItemStack(TEST_IRON, 2)})==null,
				"insufficient quantity accepted");

		int[] empty = MixerRecipe.planItemConsumption(new IngredientStack[0], new ItemStack[3]);
		check(empty!=null&&empty.length==3, "fluid-only recipe planner");
	}

	private static void testRecipeRevision()
	{
		MixerRecipe.recipeList.clear();
		long start = MixerRecipe.getRecipeRevision();
		MixerRecipe.recipeList.add(null);
		long afterAdd = MixerRecipe.getRecipeRevision();
		check(afterAdd>start, "recipe revision add");
		Iterator<MixerRecipe> iterator = MixerRecipe.recipeList.iterator();
		iterator.next();
		iterator.remove();
		check(MixerRecipe.getRecipeRevision()>afterAdd, "recipe revision iterator remove");
	}

	private static void check(boolean condition, String message)
	{
		if(!condition)
			throw new AssertionError(message);
	}
}
