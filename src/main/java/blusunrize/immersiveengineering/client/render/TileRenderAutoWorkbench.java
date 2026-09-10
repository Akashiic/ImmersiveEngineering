package blusunrize.immersiveengineering.client.render;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAutoWorkbench;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAutoWorkbench.AutoWorkbenchProcess;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

public class TileRenderAutoWorkbench extends TileRenderIE
{
	private final ModelIEObj model = createStaticModel("immersiveengineering:models/autoWorkbench.obj");
	private final ModelIEObj mirroredModel = createStaticModel("immersiveengineering:models/autoWorkbenchMirrored.obj");
	private final ModelIEObj animatedModel = new ModelIEObj("immersiveengineering:models/autoWorkbenchAnimated.obj")
	{
		@Override
		public IIcon getBlockIcon(String groupName)
		{
			if("blueprint".equalsIgnoreCase(groupName))
				return ((BlockMetalMultiblocks)IEContent.blockMetalMultiblocks).autoWorkbenchBlueprintIcon;
			return IEContent.blockMetalMultiblocks.getIcon(0, BlockMetalMultiblocks.META_autoWorkbench);
		}
	};

	private static ModelIEObj createStaticModel(String path)
	{
		return new ModelIEObj(path)
		{
			@Override
			public IIcon getBlockIcon(String groupName)
			{
				if("conveyors_conveyor".equalsIgnoreCase(groupName))
					return IEContent.blockMetalDevice.getIcon(0, BlockMetalDevices.META_conveyorBelt);
				if("base_assembler".equalsIgnoreCase(groupName))
					return IEContent.blockMetalMultiblocks.getIcon(0, BlockMetalMultiblocks.META_assembler);
				return IEContent.blockMetalMultiblocks.getIcon(0, BlockMetalMultiblocks.META_autoWorkbench);
			}
		};
	}

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		TileEntityAutoWorkbench workbench = (TileEntityAutoWorkbench)tile;
		if(!workbench.formed || workbench.pos!=TileEntityAutoWorkbench.MASTER_POS)
			return;
		applyFacing(workbench.facing, rotationMatrix);
		(workbench.mirrored?mirroredModel:model).render(tile, tes, translationMatrix, rotationMatrix, 0, false);
	}

	private static void applyFacing(int facing, Matrix4 matrix)
	{
		double angle = getFacingAngleRadians(facing);
		if(angle!=0)
			matrix.translate(.5, 0, .5).rotate(angle, 0, 1, 0).translate(-.5, 0, -.5);
	}

	private static double getFacingAngleRadians(int facing)
	{
		switch(facing)
		{
			case 3: return Math.PI;
			case 4: return Math.PI/2;
			case 5: return -Math.PI/2;
			case 2:
			default: return 0;
		}
	}

	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float partialTicks)
	{
		TileEntityAutoWorkbench workbench = (TileEntityAutoWorkbench)tile;
		if(!workbench.formed || workbench.pos!=TileEntityAutoWorkbench.MASTER_POS || workbench.getWorldObj()==null)
			return;

		float[][] itemDisplays = new float[workbench.processQueue.size()][];
		float drill = 0;
		float lift = 0;
		float press = 0;
		float liftPress = 0;
		for(int i=0; i<itemDisplays.length; i++)
		{
			AutoWorkbenchProcess process = workbench.processQueue.get(i);
			if(process==null || process.processTick<=0 || process.processTick>=process.maxTicks)
				continue;
			float processTimer = process.processTick/(float)process.maxTicks*180f;
			if(processTimer<=9)
				continue;

			float itemX = -1f;
			float itemY = -.34375f;
			float itemZ = -.9375f;
			float itemAngle = 90f;
			if(processTimer<=24)
			{
				itemAngle = 67.5f;
				if(processTimer<=19)
				{
					itemZ += .25f+(19-processTimer)/10f*.5f;
					itemY += .25f+(19-processTimer)/10f*.21875f;
				}
				else
				{
					itemZ += (24-processTimer)/5f*.25f;
					itemY += (24-processTimer)/5f*.25f;
				}
			}
			else if(processTimer<=40)
				itemX += (processTimer-24)/16f;
			else if(processTimer<=100)
			{
				itemX += 1;
				float drillStep;
				if(processTimer<=60)
				{
					lift = (processTimer-40)/20f*.3125f;
					drillStep = 4+(60-processTimer)*4;
				}
				else if(processTimer<=80)
				{
					lift = .3125f;
					drillStep = 4;
				}
				else
				{
					lift = (100-processTimer)/20f*.3125f;
					drillStep = 4+(processTimer-80)*4;
				}
				if(drillStep>0)
					drill = processTimer%drillStep/drillStep*360f;
				itemY += Math.max(0, lift-.0625f);
			}
			else if(processTimer<=116)
			{
				itemX += 1;
				itemZ += (processTimer-100)/16f;
			}
			else if(processTimer<=132)
			{
				itemX += 1+(processTimer-116)/16f;
				itemZ += 1;
			}
			else if(processTimer<=172)
			{
				itemX += 2;
				itemZ += 1;
				if(processTimer<=142)
					press = (processTimer-132)/10f;
				else if(processTimer<=162)
					press = 1;
				else
					press = (172-processTimer)/10f;
				liftPress = press*.0625f;
				itemY += liftPress;
			}
			else if(processTimer<=180)
			{
				itemX += 2+(processTimer-172)/16f;
				itemZ += 1;
			}
			itemDisplays[i] = new float[]{processTimer, itemX, itemY, itemZ, itemAngle};
		}

		GL11.glPushMatrix();
		GL11.glTranslatef((float)x+.5f, (float)y+.5f, (float)z+.5f);
		GL11.glRotatef((float)Math.toDegrees(getFacingAngleRadians(workbench.facing)), 0, 1, 0);
		if(workbench.mirrored)
			GL11.glScalef(-1, 1, 1);

		ClientUtils.bindAtlas(0);
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable(GL11.GL_CULL_FACE);
		if(workbench.inventory[0]!=null)
			renderCenteredPart("blueprint");

		GL11.glPushMatrix();
		GL11.glTranslatef(0, lift, 0);
		renderCenteredPart("lift");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glTranslatef(0, 0, -.9375f);
		GL11.glRotatef(drill, 0, 1, 0);
		renderCenteredPart("drill");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glTranslatef(0, -.21875f, -.59375f);
		GL11.glRotatef(press*90f, 1, 0, 0);
		renderCenteredPart("press");
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		GL11.glTranslatef(0, liftPress, 0);
		renderCenteredPart("pressLift");
		GL11.glPopMatrix();
		GL11.glEnable(GL11.GL_CULL_FACE);
		RenderHelper.enableStandardItemLighting();

		for(int i=0; i<itemDisplays.length; i++)
			if(itemDisplays[i]!=null)
			{
				AutoWorkbenchProcess process = workbench.processQueue.get(i);
				List<ItemStack> display = getDisplayItems(process);
				if(display.isEmpty())
					continue;
				if(display.size()<2)
					renderProcessItem(tile, display.get(0), itemDisplays[i][1], itemDisplays[i][2],
							itemDisplays[i][3], itemDisplays[i][4]);
				else
					renderProcessItems(tile, display, itemDisplays[i]);
			}
		AutoWorkbenchBlueprintRenderer.draw(workbench);
		GL11.glPopMatrix();
	}

	private void renderCenteredPart(String part)
	{
		GL11.glPushMatrix();
		GL11.glTranslatef(-.5f, -.5f, -.5f);
		animatedModel.model.renderPart(part);
		GL11.glPopMatrix();
	}

	private static List<ItemStack> getDisplayItems(AutoWorkbenchProcess process)
	{
		ArrayList<ItemStack> display = new ArrayList<ItemStack>();
		if(process.displaysOutput(0) && process.output!=null)
			display.add(process.output);
		else if(process.displayItems!=null)
			for(ItemStack stack : process.displayItems)
				if(stack!=null && stack.stackSize>0)
					display.add(stack);
		return display;
	}

	private void renderProcessItems(TileEntity tile, List<ItemStack> display, float[] state)
	{
		int size = display.size();
		int lines = (int)Math.ceil(size/2f);
		float spacer = (lines-1)*.234375f;
		for(int i=0; i<size; i++)
		{
			float offsetX = (size>2?-.3125f:0)+(lines-i/2)*.0625f+i%2*.3125f;
			float offsetZ = -spacer/2f+i/2*.234375f;
			float localX = state[1]+offsetX;
			float localY = state[2];
			float localZ = state[3]+offsetZ;
			float subProcess = state[0]-i/2*4;
			float angle = state[4];
			if(subProcess<=24)
			{
				angle = 67.5f;
				if(subProcess<=19)
				{
					localZ = -1+.25f+(19-subProcess)/10f*.5f;
					localY = -.34375f+.25f+(19-subProcess)/10f*.21875f;
				}
				else
				{
					localZ = -1+(offsetZ-(24-subProcess)/5f*offsetZ);
					localY = -.34375f+(24-subProcess)/5f*.25f;
				}
			}
			renderProcessItem(tile, display.get(i), localX, localY, localZ, angle);
		}
	}

	private void renderProcessItem(TileEntity tile, ItemStack stack, float x, float y, float z, float angle)
	{
		if(stack==null)
			return;
		GL11.glPushMatrix();
		GL11.glTranslatef(x, y, z);
		GL11.glRotatef(-angle, 1, 0, 0);
		GL11.glScalef(.625f, .625f, .625f);
		ItemStack rendered = stack.copy();
		rendered.stackSize = 1;
		EntityItem entity = new EntityItem(tile.getWorldObj(), 0, 0, 0, rendered);
		entity.hoverStart = 0;
		boolean previousInFrame = RenderItem.renderInFrame;
		try
		{
			RenderItem.renderInFrame = true;
			RenderManager.instance.renderEntityWithPosYaw(entity, 0, 0, 0, 0, 0);
		}
		finally
		{
			RenderItem.renderInFrame = previousInFrame;
			GL11.glPopMatrix();
		}
	}
}
