package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDecoration2;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAutoWorkbench;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class MultiblockAutoWorkbench implements IMultiblock
{
	public static final MultiblockAutoWorkbench instance = new MultiblockAutoWorkbench();
	private static final int TREATED_SLAB_META = 2;
	private static final ItemStack[][][] STRUCTURE = new ItemStack[2][3][3];
	static
	{
		for(int h=0; h<2; h++)
			for(int l=0; l<3; l++)
				for(int w=0; w<3; w++)
				{
					if(h==0)
					{
						if(l==0 && w==1)
							STRUCTURE[h][l][w] = redstoneEngineering(1);
						else if(w==0 && l<2)
							STRUCTURE[h][l][w] = lightEngineering(1);
						else if(w==2 && l==2)
							STRUCTURE[h][l][w] = heavyEngineering(1);
						else
							STRUCTURE[h][l][w] = scaffolding(1);
					}
					else if(w==0 && l<2)
						STRUCTURE[h][l][w] = lightEngineering(1);
					else if(w==2 && l==2)
						STRUCTURE[h][l][w] = heavyEngineering(1);
					else if(l==0)
						STRUCTURE[h][l][w] = treatedSlab(1);
					else
						STRUCTURE[h][l][w] = conveyor(1);
				}
	}
	private static ItemStack scaffolding(int amount)
	{
		return new ItemStack(IEContent.blockMetalDecoration, amount, BlockMetalDecoration.META_scaffolding);
	}

	private static ItemStack lightEngineering(int amount)
	{
		return new ItemStack(IEContent.blockMetalDecoration, amount, BlockMetalDecoration.META_lightEngineering);
	}

	private static ItemStack heavyEngineering(int amount)
	{
		return new ItemStack(IEContent.blockMetalDecoration, amount, BlockMetalDecoration.META_heavyEngineering);
	}

	private static ItemStack redstoneEngineering(int amount)
	{
		return new ItemStack(IEContent.blockMetalDecoration2, amount, BlockMetalDecoration2.META_redstoneEngineering);
	}

	private static ItemStack treatedSlab(int amount)
	{
		return new ItemStack(IEContent.blockWoodenDecoration, amount, TREATED_SLAB_META);
	}

	private static ItemStack conveyor(int amount)
	{
		return new ItemStack(IEContent.blockMetalDevice, amount, BlockMetalDevices.META_conveyorBelt);
	}
	@Override
	public String getUniqueName()
	{
		return "IE:AutoWorkbench";
	}

	@Override
	public boolean isBlockTrigger(Block block, int meta)
	{
		return Utils.compareToOreName(new ItemStack(block, 1, meta), "slabTreatedWood");
	}

	@Override
	public ItemStack[][][] getStructureManual()
	{
		return STRUCTURE;
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{
			scaffolding(5),
			treatedSlab(2),
			redstoneEngineering(1),
			lightEngineering(4),
			heavyEngineering(2),
			conveyor(4)
		};
	}
	@Override
	public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player)
	{
		if(side==0 || side==1)
		{
			int quarter = MathHelper.floor_double(player.rotationYaw*4.0F/360.0F+0.5D)&3;
			side = quarter==0?2: quarter==1?5: quarter==2?3:4;
		}
		else
			side = ForgeDirection.OPPOSITES[side];

		boolean mirrored = false;
		if(!structureCheck(world, x, y, z, side, false))
		{
			mirrored = true;
			if(!structureCheck(world, x, y, z, side, true))
				return false;
		}

		ForgeDirection forward = ForgeDirection.getOrientation(side);
		ForgeDirection right = ForgeDirection.getOrientation(rotateY(side));
		int masterX = x+forward.offsetX;
		int masterY = y;
		int masterZ = z+forward.offsetZ;

		for(int l=0; l<3; l++)
			for(int w=-1; w<=1; w++)
				for(int h=-1; h<=0; h++)
					formPart(world, x, y, z, side, mirrored, masterX, masterY, masterZ, l, w, h, forward, right);
		return true;
	}
	private boolean structureCheck(World world, int x, int y, int z, int side, boolean mirrored)
	{
		ForgeDirection forward = ForgeDirection.getOrientation(side);
		ForgeDirection right = ForgeDirection.getOrientation(rotateY(side));
		for(int l=0; l<3; l++)
			for(int h=-1; h<=0; h++)
				for(int w=-1; w<=1; w++)
				{
					int ww = mirrored?-w:w;
					int px = x+forward.offsetX*l+right.offsetX*ww;
					int py = y+h;
					int pz = z+forward.offsetZ*l+right.offsetZ*ww;
					if(!matchesStructurePart(world, px, py, pz, h, l, w))
						return false;
				}
		return true;
	}

	private boolean matchesStructurePart(World world, int x, int y, int z, int h, int l, int w)
	{
		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		if(h==-1)
		{
			if(l==0 && w==0)
				return block==IEContent.blockMetalDecoration2 && meta==BlockMetalDecoration2.META_redstoneEngineering;
			if(w==-1 && l<2)
				return block==IEContent.blockMetalDecoration && meta==BlockMetalDecoration.META_lightEngineering;
			if(w==1 && l==2)
				return block==IEContent.blockMetalDecoration && meta==BlockMetalDecoration.META_heavyEngineering;
			return block==IEContent.blockMetalDecoration && meta==BlockMetalDecoration.META_scaffolding;
		}
		if(w==-1 && l<2)
			return block==IEContent.blockMetalDecoration && meta==BlockMetalDecoration.META_lightEngineering;
		if(w==1 && l==2)
			return block==IEContent.blockMetalDecoration && meta==BlockMetalDecoration.META_heavyEngineering;
		if(l==0)
			return Utils.compareToOreName(new ItemStack(block, 1, meta), "slabTreatedWood");
		return block==IEContent.blockMetalDevice && meta==BlockMetalDevices.META_conveyorBelt;
	}

	private void formPart(World world, int x, int y, int z, int side, boolean mirrored,
			int masterX, int masterY, int masterZ, int l, int w, int h,
			ForgeDirection forward, ForgeDirection right)
	{
		int ww = mirrored?-w:w;
		int px = x+forward.offsetX*l+right.offsetX*ww;
		int py = y+h;
		int pz = z+forward.offsetZ*l+right.offsetZ*ww;
		world.setBlock(px, py, pz, IEContent.blockMetalMultiblocks, BlockMetalMultiblocks.META_autoWorkbench, 3);
		TileEntity current = world.getTileEntity(px, py, pz);
		if(current instanceof TileEntityAutoWorkbench)
		{
			TileEntityAutoWorkbench tile = (TileEntityAutoWorkbench)current;
			tile.facing = side;
			tile.formed = true;
			tile.pos = (h+1)*9+l*3+(w+1);
			tile.offset = new int[]{px-masterX, py-masterY, pz-masterZ};
			tile.mirrored = mirrored;
			tile.markDirty();
			world.markBlockForUpdate(px, py, pz);
		}
	}
	public static int rotateY(int side)
	{
		switch(side)
		{
			case 2: return 5;
			case 3: return 4;
			case 4: return 2;
			case 5: return 3;
			default: return side;
		}
	}

	public static int rotateYCCW(int side)
	{
		switch(side)
		{
			case 2: return 4;
			case 3: return 5;
			case 4: return 3;
			case 5: return 2;
			default: return side;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
	{
		ImmersiveEngineering.proxy.transformAutoWorkbenchManualComponent(iterator);
		return false;
	}
	@Override
	public float getManualScale()
	{
		return 15;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
		ImmersiveEngineering.proxy.renderAutoWorkbenchManual();
	}
}
