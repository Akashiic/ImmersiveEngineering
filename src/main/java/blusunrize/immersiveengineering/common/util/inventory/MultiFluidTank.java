package blusunrize.immersiveengineering.common.util.inventory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidTank;

/**
 * Ordered multi-fluid storage used by modern IE process backports.
 *
 * The tank is deliberately world-agnostic: face rules and automation policy
 * belong to the owning TileEntity, not this data structure.
 */
public class MultiFluidTank implements IFluidTank
{
	private final ArrayList<FluidStack> fluids = new ArrayList<FluidStack>();
	private final int capacity;

	public MultiFluidTank(int capacity)
	{
		if(capacity<=0)
			throw new IllegalArgumentException("MultiFluidTank capacity must be positive");
		this.capacity = capacity;
	}

	public MultiFluidTank readFromNBT(NBTTagCompound nbt)
	{
		fluids.clear();
		if(nbt==null||!nbt.hasKey("fluids"))
			return this;

		NBTTagList list = nbt.getTagList("fluids", 10);
		for(int i=0; i<list.tagCount()&&getFluidAmount()<capacity; i++)
		{
			FluidStack stack = FluidStack.loadFluidStackFromNBT(list.getCompoundTagAt(i));
			if(stack==null||stack.getFluid()==null||stack.amount<=0)
				continue;
			fill(stack, true);
		}
		return this;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList list = new NBTTagList();
		for(FluidStack stack : fluids)
			if(stack!=null&&stack.getFluid()!=null&&stack.amount>0)
				list.appendTag(stack.writeToNBT(new NBTTagCompound()));
		nbt.setTag("fluids", list);
		return nbt;
	}

	public int getFluidTypes()
	{
		return fluids.size();
	}

	public FluidStack getFluidAt(int index)
	{
		return index>=0&&index<fluids.size()?fluids.get(index):null;
	}

	public List<FluidStack> getFluids()
	{
		return java.util.Collections.unmodifiableList(fluids);
	}

	@Override
	public FluidStack getFluid()
	{
		return fluids.isEmpty()?null:fluids.get(fluids.size()-1);
	}

	@Override
	public int getFluidAmount()
	{
		long total = 0;
		for(FluidStack stack : fluids)
			if(stack!=null&&stack.amount>0)
				total += stack.amount;
		return (int)Math.min(Integer.MAX_VALUE, total);
	}

	@Override
	public int getCapacity()
	{
		return capacity;
	}

	public int getSpace()
	{
		return Math.max(0, capacity-getFluidAmount());
	}

	@Override
	public FluidTankInfo getInfo()
	{
		FluidStack visible = getFluid();
		return new FluidTankInfo(visible==null?null:visible.copy(), capacity);
	}

	@Override
	public int fill(FluidStack resource, boolean doFill)
	{
		if(resource==null||resource.getFluid()==null||resource.amount<=0)
			return 0;
		int accepted = Math.min(resource.amount, getSpace());
		if(accepted<=0||!doFill)
			return accepted;

		FluidStack existing = findEquivalent(resource);
		if(existing!=null)
			existing.amount += accepted;
		else
			fluids.add(copyWithAmount(resource, accepted));
		return accepted;
	}

	public FluidStack drain(FluidStack resource, boolean doDrain)
	{
		if(resource==null||resource.getFluid()==null||resource.amount<=0)
			return null;
		Iterator<FluidStack> iterator = fluids.iterator();
		while(iterator.hasNext())
		{
			FluidStack stored = iterator.next();
			if(!sameKind(stored, resource))
				continue;
			int amount = Math.min(resource.amount, stored.amount);
			FluidStack result = copyWithAmount(stored, amount);
			if(doDrain)
			{
				stored.amount -= amount;
				if(stored.amount<=0)
					iterator.remove();
			}
			return result;
		}
		return null;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain)
	{
		if(maxDrain<=0||fluids.isEmpty())
			return null;
		return drain(copyWithAmount(getFluid(), maxDrain), doDrain);
	}

	public FluidStack drainAt(int index, int maxDrain, boolean doDrain)
	{
		if(index<0||index>=fluids.size()||maxDrain<=0)
			return null;
		FluidStack stored = fluids.get(index);
		int amount = Math.min(maxDrain, stored.amount);
		FluidStack result = copyWithAmount(stored, amount);
		if(doDrain)
		{
			stored.amount -= amount;
			if(stored.amount<=0)
				fluids.remove(index);
		}
		return result;
	}

	private FluidStack findEquivalent(FluidStack resource)
	{
		for(FluidStack stack : fluids)
			if(sameKind(stack, resource))
				return stack;
		return null;
	}

	private static boolean sameKind(FluidStack first, FluidStack second)
	{
		return first!=null&&second!=null
				&&first.isFluidEqual(second)
				&&FluidStack.areFluidStackTagsEqual(first, second);
	}

	private static FluidStack copyWithAmount(FluidStack stack, int amount)
	{
		if(stack==null||stack.getFluid()==null||amount<=0)
			return null;
		FluidStack copy = stack.copy();
		copy.amount = amount;
		return copy;
	}

	public void clear()
	{
		fluids.clear();
	}
}
