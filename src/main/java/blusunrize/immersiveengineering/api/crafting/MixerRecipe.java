package blusunrize.immersiveengineering.api.crafting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

/**
 * Recipe contract for the Mixer backport.
 *
 * Item quantities may be spread across multiple inventory slots. Callers can
 * use {@link #getItemConsumption(ItemStack[])} as the authoritative slot plan.
 */
public class MixerRecipe
{
	public static float energyModifier = 1;
	public static float timeModifier = 1;
	private static long recipeRevision;

	public final IngredientStack[] itemInputs;
	public final FluidStack fluidInput;
	public final FluidStack fluidOutput;
	public final int fluidAmount;
	public final int totalProcessEnergy;
	public final int totalProcessTime;

	public static final ArrayList<MixerRecipe> recipeList = new TrackedRecipeList();

	public MixerRecipe(FluidStack output, FluidStack input, IngredientStack[] items, int energy)
	{
		if(output==null||output.getFluid()==null||output.amount<=0)
			throw new IllegalArgumentException("Mixer output fluid must be non-empty");
		if(input==null||input.getFluid()==null||input.amount<=0)
			throw new IllegalArgumentException("Mixer input fluid must be non-empty");
		if(energy<=0)
			throw new IllegalArgumentException("Mixer energy must be positive");

		this.fluidOutput = output.copy();
		this.fluidInput = input.copy();
		this.fluidAmount = output.amount;
		this.itemInputs = copyInputs(items);
		this.totalProcessEnergy = Math.max(1, (int)Math.floor(energy*energyModifier));
		this.totalProcessTime = Math.max(1, (int)Math.floor(output.amount*timeModifier));
	}

	private static IngredientStack[] copyInputs(IngredientStack[] inputs)
	{
		if(inputs==null||inputs.length==0)
			return new IngredientStack[0];
		IngredientStack[] copy = new IngredientStack[inputs.length];
		for(int i=0; i<inputs.length; i++)
		{
			if(inputs[i]==null)
				throw new IllegalArgumentException("Mixer item ingredient "+i+" is null");
			copy[i] = inputs[i].copyWithSize(inputs[i].getInputSize());
		}
		return copy;
	}

	public static long getRecipeRevision()
	{
		return recipeRevision;
	}

	public static MixerRecipe addRecipe(FluidStack output, FluidStack input, Object[] items, int energy)
	{
		IngredientStack[] converted = new IngredientStack[items==null?0:items.length];
		for(int i=0; i<converted.length; i++)
			converted[i] = IngredientStack.from(items[i]);
		MixerRecipe recipe = new MixerRecipe(output, input, converted, energy);
		recipeList.add(recipe);
		return recipe;
	}

	public static MixerRecipe findRecipe(FluidStack fluid, ItemStack[] components)
	{
		if(fluid==null||fluid.getFluid()==null||fluid.amount<=0)
			return null;
		for(MixerRecipe recipe : recipeList)
			if(recipe.matches(fluid, components))
				return recipe;
		return null;
	}

	public boolean matches(FluidStack fluid, ItemStack[] components)
	{
		return fluidMatches(fluid)&&getItemConsumption(components)!=null;
	}

	public boolean fluidMatches(FluidStack fluid)
	{
		return fluid!=null&&fluid.amount>=fluidInput.amount
				&&fluid.isFluidEqual(fluidInput)
				&&FluidStack.areFluidStackTagsEqual(fluid, fluidInput);
	}

	/**
	 * Returns required consumption per component slot, or null if unavailable.
	 */
	public int[] getItemConsumption(ItemStack[] components)
	{
		return planItemConsumption(itemInputs, components);
	}

	public static int[] planItemConsumption(IngredientStack[] ingredients, ItemStack[] components)
	{
		if(components==null)
			components = new ItemStack[0];
		int ingredientCount = ingredients==null?0:ingredients.length;
		int slotCount = components.length;
		int ingredientBase = 1;
		int slotBase = ingredientBase+ingredientCount;
		int sink = slotBase+slotCount;
		int nodeCount = sink+1;
		int[][] capacity = new int[nodeCount][nodeCount];
		int totalDemand = 0;

		for(int ingredient=0; ingredient<ingredientCount; ingredient++)
		{
			int demand = ingredients[ingredient].getInputSize();
			capacity[0][ingredientBase+ingredient] = demand;
			totalDemand += demand;
			for(int slot=0; slot<slotCount; slot++)
			{
				ItemStack stack = components[slot];
				if(stack!=null&&stack.stackSize>0&&ingredients[ingredient].matchesItemStackIgnoringSize(stack))
					capacity[ingredientBase+ingredient][slotBase+slot] = Math.min(demand, stack.stackSize);
			}
		}
		for(int slot=0; slot<slotCount; slot++)
			capacity[slotBase+slot][sink] = components[slot]==null?0:Math.max(0, components[slot].stackSize);

		int[][] residual = new int[nodeCount][nodeCount];
		for(int node=0; node<nodeCount; node++)
			residual[node] = capacity[node].clone();
		int flow = 0;
		int[] parent = new int[nodeCount];
		int[] queue = new int[nodeCount];
		while(flow<totalDemand)
		{
			Arrays.fill(parent, -1);
			parent[0] = 0;
			int head = 0, tail = 0;
			queue[tail++] = 0;
			while(head<tail&&parent[sink]<0)
			{
				int from = queue[head++];
				for(int to=0; to<nodeCount; to++)
					if(parent[to]<0&&residual[from][to]>0)
					{
						parent[to] = from;
						queue[tail++] = to;
					}
			}
			if(parent[sink]<0)
				return null;

			int augment = Integer.MAX_VALUE;
			for(int node=sink; node!=0; node=parent[node])
				augment = Math.min(augment, residual[parent[node]][node]);
			for(int node=sink; node!=0; node=parent[node])
			{
				int from = parent[node];
				residual[from][node] -= augment;
				residual[node][from] += augment;
			}
			flow += augment;
		}

		int[] consume = new int[slotCount];
		for(int ingredient=0; ingredient<ingredientCount; ingredient++)
			for(int slot=0; slot<slotCount; slot++)
				consume[slot] += capacity[ingredientBase+ingredient][slotBase+slot]
						-residual[ingredientBase+ingredient][slotBase+slot];
		return consume;
	}

	public int[] getUsedSlots(ItemStack[] components)
	{
		int[] consumption = getItemConsumption(components);
		if(consumption==null)
			return new int[0];
		int count = 0;
		for(int amount : consumption)
			if(amount>0)
				count++;
		int[] used = new int[count];
		int out = 0;
		for(int i=0; i<consumption.length; i++)
			if(consumption[i]>0)
				used[out++] = i;
		return used;
	}

	public FluidStack getFluidOutput(FluidStack input, ItemStack[] components)
	{
		return matches(input, components)?fluidOutput.copy():null;
	}

	public int getMultipleProcessTicks()
	{
		return 7;
	}

	public static List<MixerRecipe> removeRecipes(FluidStack output)
	{
		List<MixerRecipe> removed = new ArrayList<MixerRecipe>();
		if(output==null)
			return removed;

		Iterator<MixerRecipe> it = recipeList.iterator();
		while(it.hasNext())
		{
			MixerRecipe recipe = it.next();
			if(sameFluidKind(recipe.fluidOutput, output))
			{
				removed.add(recipe);
				it.remove();
			}
		}
		return removed;
	}

	private static boolean sameFluidKind(FluidStack first, FluidStack second)
	{
		return first!=null&&second!=null
				&&first.isFluidEqual(second)
				&&FluidStack.areFluidStackTagsEqual(first, second);
	}

	private static class TrackedRecipeList extends ArrayList<MixerRecipe>
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean add(MixerRecipe recipe)
		{
			boolean changed = super.add(recipe);
			if(changed)
				recipeRevision++;
			return changed;
		}

		@Override
		public void add(int index, MixerRecipe recipe)
		{
			super.add(index, recipe);
			recipeRevision++;
		}

		@Override
		public boolean addAll(Collection<? extends MixerRecipe> recipes)
		{
			boolean changed = super.addAll(recipes);
			if(changed)
				recipeRevision++;
			return changed;
		}

		@Override
		public boolean addAll(int index, Collection<? extends MixerRecipe> recipes)
		{
			boolean changed = super.addAll(index, recipes);
			if(changed)
				recipeRevision++;
			return changed;
		}

		@Override
		public MixerRecipe remove(int index)
		{
			MixerRecipe removed = super.remove(index);
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
		public MixerRecipe set(int index, MixerRecipe recipe)
		{
			MixerRecipe previous = super.set(index, recipe);
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
