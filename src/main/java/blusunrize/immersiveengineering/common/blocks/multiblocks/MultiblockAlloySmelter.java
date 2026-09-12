package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.stone.BlockStoneDecoration2;
import blusunrize.immersiveengineering.common.blocks.stone.BlockStoneDevices2;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityAlloySmelter;
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

/** 2x2x2 Alloy Kiln formation using the modern IE structure contract. */
public class MultiblockAlloySmelter implements IMultiblock
{
	public static final MultiblockAlloySmelter instance = new MultiblockAlloySmelter();
	private static final ItemStack[][][] STRUCTURE = new ItemStack[2][2][2];

	static
	{
		for(int h=0; h<2; h++)
			for(int depth=0; depth<2; depth++)
				for(int width=0; width<2; width++)
					STRUCTURE[h][depth][width] = alloyBrick();
	}

	private static ItemStack alloyBrick()
	{
		return new ItemStack(IEContent.blockStoneDecoration2, 1, BlockStoneDecoration2.META_alloyBrick);
	}

	@Override
	public ItemStack[][][] getStructureManual()
	{
		return STRUCTURE;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean overwriteBlockRender(ItemStack stack, int iterator)
	{
		return false;
	}

	@Override
	public float getManualScale()
	{
		return 20;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderFormedStructure()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderFormedStructure()
	{
	}

	@Override
	public String getUniqueName()
	{
		return "IE:AlloySmelter";
	}

	@Override
	public boolean isBlockTrigger(Block block, int meta)
	{
		return block==IEContent.blockStoneDecoration2&&meta==BlockStoneDecoration2.META_alloyBrick;
	}

	@Override
	public boolean createStructure(World world, int x, int y, int z, int side, EntityPlayer player)
	{
		int view = MathHelper.floor_double(player.rotationYaw*4.0F/360.0F+0.5D)&3;
		int front = view==0?2:view==1?5:view==2?3:4;
		int back = BlockStoneDevices2.opposite(front);
		int right = BlockStoneDevices2.rightOf(front);
		ForgeDirection backDir = ForgeDirection.getOrientation(back);
		ForgeDirection rightDir = ForgeDirection.getOrientation(right);

		if(isAlloyBrick(world, x, y-1, z))
			y--;
		if(!isAlloyBrick(world, x+rightDir.offsetX, y, z+rightDir.offsetZ))
		{
			x -= rightDir.offsetX;
			z -= rightDir.offsetZ;
		}

		for(int h=0; h<2; h++)
			for(int depth=0; depth<2; depth++)
				for(int width=0; width<2; width++)
				{
					int px = x+backDir.offsetX*depth+rightDir.offsetX*width;
					int py = y+h;
					int pz = z+backDir.offsetZ*depth+rightDir.offsetZ*width;
					if(!isAlloyBrick(world, px, py, pz))
						return false;
				}

		for(int targetPos=0; targetPos<8; targetPos++)
		{
			int h = (targetPos/4)&1;
			int depth = (targetPos/2)&1;
			int width = targetPos&1;
			int px = x+backDir.offsetX*depth+rightDir.offsetX*width;
			int py = y+h;
			int pz = z+backDir.offsetZ*depth+rightDir.offsetZ*width;
			if(!world.setBlock(px, py, pz, IEContent.blockStoneDevice2,
					BlockStoneDevices2.META_alloySmelter, 3))
			{
				rollback(world, x, y, z, backDir, rightDir);
				return false;
			}
			TileEntity te = world.getTileEntity(px, py, pz);
			if(!(te instanceof TileEntityAlloySmelter))
			{
				rollback(world, x, y, z, backDir, rightDir);
				return false;
			}
			TileEntityAlloySmelter kiln = (TileEntityAlloySmelter)te;
			kiln.offset = new int[]{px-x, py-y, pz-z};
			kiln.pos = targetPos;
			kiln.facing = front;
			kiln.formed = true;
			kiln.markDirty();
			world.markBlockForUpdate(px, py, pz);
		}
		return true;
	}

	private boolean isAlloyBrick(World world, int x, int y, int z)
	{
		return world.blockExists(x, y, z)
				&&world.getBlock(x, y, z)==IEContent.blockStoneDecoration2
				&&world.getBlockMetadata(x, y, z)==BlockStoneDecoration2.META_alloyBrick;
	}

	private void rollback(World world, int x, int y, int z,
			ForgeDirection backDir, ForgeDirection rightDir)
	{
		// Prevent breakBlock from treating a failed formation as a real dismantle.
		for(int h=0; h<2; h++)
			for(int depth=0; depth<2; depth++)
				for(int width=0; width<2; width++)
				{
					int px = x+backDir.offsetX*depth+rightDir.offsetX*width;
					int py = y+h;
					int pz = z+backDir.offsetZ*depth+rightDir.offsetZ*width;
					TileEntity te = Utils.getExistingTileEntity(world, px, py, pz);
					if(te instanceof TileEntityAlloySmelter)
					{
						TileEntityAlloySmelter part = (TileEntityAlloySmelter)te;
						part.formed = false;
					}
				}

		for(int h=0; h<2; h++)
			for(int depth=0; depth<2; depth++)
				for(int width=0; width<2; width++)
				{
					int px = x+backDir.offsetX*depth+rightDir.offsetX*width;
					int py = y+h;
					int pz = z+backDir.offsetZ*depth+rightDir.offsetZ*width;
					if(world.getBlock(px, py, pz)==IEContent.blockStoneDevice2)
						world.setBlock(px, py, pz, IEContent.blockStoneDecoration2,
								BlockStoneDecoration2.META_alloyBrick, 3);
				}
	}

	@Override
	public ItemStack[] getTotalMaterials()
	{
		return new ItemStack[]{new ItemStack(
				IEContent.blockStoneDecoration2, 8, BlockStoneDecoration2.META_alloyBrick)};
	}
}
