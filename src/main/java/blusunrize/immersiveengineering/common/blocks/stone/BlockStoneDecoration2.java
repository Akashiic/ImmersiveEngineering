package blusunrize.immersiveengineering.common.blocks.stone;

import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/** Expansion family for post-0.7.9 stone decorations. */
public class BlockStoneDecoration2 extends BlockIEBase
{
	public static final int META_alloyBrick = 0;

	public BlockStoneDecoration2()
	{
		super("stoneDecoration2", Material.rock, 1, ItemBlockIEBase.class, "alloyBrick");
		setHardness(2.0F);
		setResistance(20.0F);
		setMetaLightOpacity(META_alloyBrick, 255);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		icons[META_alloyBrick][0] = iconRegister.registerIcon("immersiveengineering:stoneDecoration2_alloyBrick");
	}

	@Override
	public boolean allowHammerHarvest(int metadata)
	{
		return false;
	}

	@Override
	public boolean hasTileEntity(int meta)
	{
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return null;
	}
}
