package blusunrize.immersiveengineering.common.blocks.stone;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/** Formed stone machines introduced by the modern backport program. */
public class BlockStoneDevices2 extends BlockIEBase
{
	public static final int META_alloySmelter = 0;
	private final IIcon[] bottom = new IIcon[4];
	private final IIcon[] frontOff = new IIcon[4];
	private final IIcon[] frontOn = new IIcon[4];
	private final IIcon[] sideIcons = new IIcon[4];
	private final IIcon[] top = new IIcon[4];

	public BlockStoneDevices2()
	{
		super("stoneDevice2", Material.rock, 1, ItemBlockIEBase.class, "alloySmelter");
		setHardness(2.0F);
		setResistance(20.0F);
		setMetaLightOpacity(META_alloySmelter, 255);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{
		// Formed-machine blocks are never exposed as standalone items.
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		for(int row=0; row<2; row++)
			for(int col=0; col<2; col++)
			{
				int index = quadrant(col, row);
				String suffix = "_"+col+row;
				bottom[index] = iconRegister.registerIcon("immersiveengineering:stoneDevice2_alloySmelter_bottom"+suffix);
				frontOff[index] = iconRegister.registerIcon("immersiveengineering:stoneDevice2_alloySmelter_front_off"+suffix);
				frontOn[index] = iconRegister.registerIcon("immersiveengineering:stoneDevice2_alloySmelter_front_on"+suffix);
				sideIcons[index] = iconRegister.registerIcon("immersiveengineering:stoneDevice2_alloySmelter_side"+suffix);
				top[index] = iconRegister.registerIcon("immersiveengineering:stoneDevice2_alloySmelter_top"+suffix);
			}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int blockSide, int meta)
	{
		return frontOff[quadrant(0, 0)]!=null?frontOff[quadrant(0, 0)]:super.getIcon(blockSide, meta);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int blockSide)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(!(te instanceof TileEntityAlloySmelter))
			return getIcon(blockSide, META_alloySmelter);
		TileEntityAlloySmelter kiln = (TileEntityAlloySmelter)te;
		int h = kiln.getLocalHeight();
		int depth = kiln.getLocalDepth();
		int width = kiln.getLocalWidth();
		int facing = kiln.facing;
		int row = 1-h;

		if(blockSide==0)
			return bottom[quadrant(width, depth)];
		if(blockSide==1)
			return top[quadrant(1-width, depth)];
		if(blockSide==facing)
			return (kiln.isRenderActive()?frontOn:frontOff)[quadrant(width, row)];
		if(blockSide==opposite(facing))
			return sideIcons[quadrant(1-width, row)];
		if(blockSide==rightOf(facing))
			return sideIcons[quadrant(depth, row)];
		return sideIcons[quadrant(1-depth, row)];
	}

	private static int quadrant(int col, int row)
	{
		return (row&1)*2+(col&1);
	}

	public static int opposite(int direction)
	{
		return direction>=0&&direction<6?net.minecraftforge.common.util.ForgeDirection.OPPOSITES[direction]:direction;
	}

	public static int rightOf(int facing)
	{
		switch(facing)
		{
		case 2: return 4; // north -> west
		case 3: return 5; // south -> east
		case 4: return 3; // west -> south
		case 5: return 2; // east -> north
		default: return 5;
		}
	}

	@Override
	public boolean hasTileEntity(int meta)
	{
		return meta==META_alloySmelter;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return meta==META_alloySmelter?new TileEntityAlloySmelter():null;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return true;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return true;
	}

	@Override
	public int getRenderType()
	{
		return 0;
	}

	@Override
	public boolean allowHammerHarvest(int metadata)
	{
		return false;
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		return new ArrayList<ItemStack>();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player,
			int side, float hitX, float hitY, float hitZ)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(!(te instanceof TileEntityAlloySmelter)||player.isSneaking())
			return false;
		TileEntityAlloySmelter master = ((TileEntityAlloySmelter)te).getMasterOrSelf();
		if(master==null||!master.formed)
			return false;
		if(!world.isRemote)
			player.openGui(ImmersiveEngineering.instance, Lib.GUIID_AlloySmelter,
					world, master.xCoord, master.yCoord, master.zCoord);
		return true;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityAlloySmelter)
			return ((TileEntityAlloySmelter)te).getOriginalBlock();
		return new ItemStack(IEContent.blockStoneDecoration2, 1, BlockStoneDecoration2.META_alloyBrick);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if(te instanceof TileEntityAlloySmelter)
			((TileEntityAlloySmelter)te).disassemble(x, y, z);
		super.breakBlock(world, x, y, z, block, meta);
	}
}
