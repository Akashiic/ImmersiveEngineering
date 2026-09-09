package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.tool.BelljarHandler.IPlantHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.PlantRenderInfo;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices2;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityGardenCloche;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;

public class TileRenderGardenCloche extends TileRenderIE
{
	public final ModelIEObj model = new ModelIEObj("immersiveengineering:models/gardenCloche.obj")
	{
		@Override
		public IIcon getBlockIcon(String groupName)
		{
			if("farmland".equalsIgnoreCase(groupName))
				return null;
			return IEContent.blockMetalDevice2.getIcon(0, BlockMetalDevices2.META_gardenCloche);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		TileEntityGardenCloche cloche = (TileEntityGardenCloche)tile;
		if(cloche.dummy!=0)
			return;

		applyFacing(cloche.facing, rotationMatrix);
		if(BlockRenderMetalDevices2.renderPass==1)
			model.render(tile, tes, translationMatrix, rotationMatrix, 0, false, "glass");
		else
		{
			model.render(tile, tes, translationMatrix, rotationMatrix, 0, false, "base");
			IIcon soil = getSoilIcon(cloche);
			if(soil!=null)
				ClientUtils.renderStaticWavefrontModelWithIcon(tile, model.model, soil, tes, translationMatrix, rotationMatrix, 0, false, 1f, 1f, 1f, "farmland");
		}
	}

	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float partialTicks)
	{
		TileEntityGardenCloche cloche = (TileEntityGardenCloche)tile;
		if(cloche.dummy!=0 || cloche.getWorldObj()==null)
			return;
		IPlantHandler handler = cloche.getCurrentPlantHandler();
		ItemStack seed = cloche.getStackInSlot(TileEntityGardenCloche.SLOT_SEED);
		ItemStack soil = cloche.getStackInSlot(TileEntityGardenCloche.SLOT_SOIL);
		if(handler==null || seed==null || !handler.isCorrectSoil(seed, soil))
			return;

		PlantRenderInfo[] rendered = handler.getRenderInfo(seed, soil, cloche.getRenderGrowth(), cloche);
		if(rendered==null || rendered.length==0)
			return;

		ClientUtils.bindAtlas(0);
		RenderBlocks renderBlocks = RenderBlocks.getInstance();
		RenderHelper.disableStandardItemLighting();
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x+.5f, (float)y+1.0625f, (float)z+.5f);
		GL11.glDisable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glColor4f(1f, 1f, 1f, 1f);

		for(PlantRenderInfo info : rendered)
		{
			if(info==null || info.block==null || info.scale<=0)
				continue;
			GL11.glPushMatrix();
			GL11.glTranslatef(info.xOffset, info.yOffset, info.zOffset);
			GL11.glScalef(info.scale, info.scale, info.scale);
			renderBlocks.renderBlockAsItem(info.block, info.metadata, 1f);
			GL11.glPopMatrix();
		}

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glPopMatrix();
		RenderHelper.enableStandardItemLighting();
	}

	private static void applyFacing(int facing, Matrix4 matrix)
	{
		double angle;
		switch(facing)
		{
		case 3:
			angle = Math.PI;
			break;
		case 4:
			angle = Math.PI/2;
			break;
		case 5:
			angle = -Math.PI/2;
			break;
		case 2:
		default:
			angle = 0;
			break;
		}
		if(angle!=0)
			matrix.translate(.5, 0, .5).rotate(angle, 0, 1, 0).translate(-.5, 0, -.5);
	}

	private static IIcon getSoilIcon(TileEntityGardenCloche cloche)
	{
		ItemStack stack = cloche.getStackInSlot(TileEntityGardenCloche.SLOT_SOIL);
		if(stack==null || stack.getItem()==null)
			return Blocks.farmland.getIcon(1, 7);
		Block block = Block.getBlockFromItem(stack.getItem());
		if(block==null || block==Blocks.air)
			return Blocks.farmland.getIcon(1, 7);
		int meta = stack.getItemDamage();
		if(block==Blocks.dirt && meta!=2)
			return Blocks.farmland.getIcon(1, 7);
		return block.getIcon(1, meta);
	}
}
