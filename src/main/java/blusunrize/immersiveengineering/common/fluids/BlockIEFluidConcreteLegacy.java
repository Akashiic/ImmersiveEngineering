package blusunrize.immersiveengineering.common.fluids;

import java.util.Random;

import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

/** 1.21-style aging concrete translated onto Forge 1.7 classic-fluid metadata. */
public class BlockIEFluidConcreteLegacy extends BlockIEFluidLegacy
{
	private int propagationAge;

	public BlockIEFluidConcreteLegacy(String name, Fluid fluid)
	{
		super(name, fluid, Material.water);
		setQuantaPerBlock(8);
		setTickRate(20);
		setDensity(2400);
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		if(!world.isRemote)
			ConcreteAgeData.get(world).setAge(x, y, z, 0);
		super.onBlockAdded(world, x, y, z);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta)
	{
		if(!world.isRemote)
		{
			ConcreteAgeData data = ConcreteAgeData.get(world);
			if(data!=null)
				data.removeAge(x, y, z);
		}
		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand)
	{
		if(world.isRemote)
		{
			super.updateTick(world, x, y, z, rand);
			return;
		}

		ConcreteAgeData data = ConcreteAgeData.get(world);
		int age = data==null?0:data.getAge(x, y, z);
		int level = world.getBlockMetadata(x, y, z);
		boolean mayDry = age>=31;

		if(level==0&&hasHorizontalConcrete(world, x, y, z))
			mayDry = false;

		if(mayDry)
		{
			if(data!=null)
				data.removeAge(x, y, z);
			Block target = level>=5?IEContent.blockStoneSlabs:IEContent.blockStoneDecoration;
			int targetMeta = level>=5?1:4;
			world.setBlock(x, y, z, target, targetMeta, 3);
			return;
		}

		propagationAge = Math.min(31, age+1);
		super.updateTick(world, x, y, z, rand);
		if(world.getBlock(x, y, z)==this)
		{
			if(data!=null)
				data.setAge(x, y, z, propagationAge);
			world.scheduleBlockUpdate(x, y, z, this, tickRate);
		}
		else if(data!=null)
			data.removeAge(x, y, z);
	}

	@Override
	protected void flowIntoBlock(World world, int x, int y, int z, int meta)
	{
		super.flowIntoBlock(world, x, y, z, meta);
		if(!world.isRemote&&world.getBlock(x, y, z)==this)
		{
			ConcreteAgeData data = ConcreteAgeData.get(world);
			if(data!=null)
				data.setAge(x, y, z, propagationAge);
		}
	}

	private boolean hasHorizontalConcrete(World world, int x, int y, int z)
	{
		return world.getBlock(x+1, y, z)==this
				||world.getBlock(x-1, y, z)==this
				||world.getBlock(x, y, z+1)==this
				||world.getBlock(x, y, z-1)==this;
	}
}
