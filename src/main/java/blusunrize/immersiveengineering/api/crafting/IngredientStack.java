package blusunrize.immersiveengineering.api.crafting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Legacy-sized ingredient abstraction backported from later official IE.
 * Supports the item/OreDictionary subset required by 1.7.10 recipes.
 */
public class IngredientStack
{
	private ItemStack stack;
	private List<ItemStack> stackList;
	private String oreName;
	public int inputSize = 1;
	public boolean useNBT;

	public IngredientStack(ItemStack stack)
	{
		if(stack==null)
			throw new IllegalArgumentException("Ingredient stack may not be null");
		this.stack = stack.copy();
		this.inputSize = Math.max(1, stack.stackSize);
	}

	public IngredientStack(String oreName)
	{
		this(oreName, 1);
	}

	public IngredientStack(String oreName, int inputSize)
	{
		if(oreName==null||oreName.isEmpty())
			throw new IllegalArgumentException("OreDictionary name may not be empty");
		this.oreName = oreName;
		this.inputSize = Math.max(1, inputSize);
	}

	public IngredientStack(List<ItemStack> stackList, int inputSize)
	{
		if(stackList==null||stackList.isEmpty())
			throw new IllegalArgumentException("Ingredient stack list may not be empty");
		this.stackList = new ArrayList<ItemStack>(stackList.size());
		for(ItemStack candidate : stackList)
			if(candidate!=null)
				this.stackList.add(candidate.copy());
		if(this.stackList.isEmpty())
			throw new IllegalArgumentException("Ingredient stack list contained no stacks");
		this.inputSize = Math.max(1, inputSize);
	}

	public IngredientStack(IngredientStack other)
	{
		this.inputSize = other.inputSize;
		this.useNBT = other.useNBT;
		this.stack = other.stack==null?null:other.stack.copy();
		this.oreName = other.oreName;
		if(other.stackList!=null)
		{
			this.stackList = new ArrayList<ItemStack>(other.stackList.size());
			for(ItemStack candidate : other.stackList)
				this.stackList.add(candidate==null?null:candidate.copy());
		}
	}

	public static IngredientStack from(Object input)
	{
		if(input instanceof IngredientStack)
			return new IngredientStack((IngredientStack)input);
		if(input instanceof ItemStack)
			return new IngredientStack((ItemStack)input);
		if(input instanceof Item)
			return new IngredientStack(new ItemStack((Item)input));
		if(input instanceof Block)
			return new IngredientStack(new ItemStack((Block)input));
		if(input instanceof String)
			return new IngredientStack((String)input);
		if(input instanceof List)
			return new IngredientStack((List<ItemStack>)input, 1);
		throw new IllegalArgumentException("Unsupported ingredient input: "+input);
	}

	public IngredientStack copyWithSize(int size)
	{
		IngredientStack copy = new IngredientStack(this);
		copy.inputSize = Math.max(1, size);
		return copy;
	}

	public boolean matchesItemStack(ItemStack input)
	{
		if(input==null||input.stackSize < inputSize)
			return false;
		if(oreName!=null)
			return ApiUtils.compareToOreName(input, oreName);
		if(stackList!=null)
		{
			for(ItemStack candidate : stackList)
				if(candidate!=null&&OreDictionary.itemMatches(candidate, input, false))
					return nbtMatches(candidate, input);
			return false;
		}
		return stack!=null&&OreDictionary.itemMatches(stack, input, false)&&nbtMatches(stack, input);
	}

	private boolean nbtMatches(ItemStack expected, ItemStack actual)
	{
		if(!useNBT)
			return true;
		if(expected.hasTagCompound()!=actual.hasTagCompound())
			return false;
		return !expected.hasTagCompound()||expected.getTagCompound().equals(actual.getTagCompound());
	}

	public List<ItemStack> getSizedStackList()
	{
		List<ItemStack> values;
		if(oreName!=null)
			values = OreDictionary.getOres(oreName);
		else if(stackList!=null)
			values = stackList;
		else
			values = stack==null?Collections.<ItemStack>emptyList():Collections.singletonList(stack);
		List<ItemStack> result = new ArrayList<ItemStack>(values.size());
		for(ItemStack value : values)
			if(value!=null)
			{
				ItemStack copy = value.copy();
				copy.stackSize = inputSize;
				result.add(copy);
			}
		return result;
	}

	public ItemStack getExampleStack()
	{
		List<ItemStack> examples = getSizedStackList();
		return examples.isEmpty()?null:examples.get(0);
	}

	public String getOreName()
	{
		return oreName;
	}

	public boolean matchesItemStackIgnoringSize(ItemStack input)
	{
		if(input==null)
			return false;
		if(oreName!=null)
			return ApiUtils.compareToOreName(input, oreName);
		if(stackList!=null)
		{
			for(ItemStack candidate : stackList)
				if(candidate!=null&&OreDictionary.itemMatches(candidate, input, false)&&nbtMatches(candidate, input))
					return true;
			return false;
		}
		return stack!=null&&OreDictionary.itemMatches(stack, input, false)&&nbtMatches(stack, input);
	}

	public IngredientStack setUseNBT(boolean useNBT)
	{
		this.useNBT = useNBT;
		return this;
	}
}
