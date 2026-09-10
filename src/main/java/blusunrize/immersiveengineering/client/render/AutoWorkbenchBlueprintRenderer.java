package blusunrize.immersiveengineering.client.render;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.HashMultimap;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAutoWorkbench;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.client.resources.IResource;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

final class AutoWorkbenchBlueprintRenderer
{
	private static final Map<BlueprintCraftingRecipe, BlueprintLines> CACHE =
			new HashMap<BlueprintCraftingRecipe, BlueprintLines>();
	private static final Set<BlueprintCraftingRecipe> FAILED =
			new HashSet<BlueprintCraftingRecipe>();

	private AutoWorkbenchBlueprintRenderer()
	{
	}

	public static void draw(TileEntityAutoWorkbench tile)
	{
		if(tile==null || tile.inventory==null || tile.inventory.length<1 || tile.inventory[0]==null
				|| ClientUtils.mc().thePlayer==null)
			return;
		double distanceSq = ClientUtils.mc().thePlayer.getDistanceSq(
				tile.xCoord+.5, tile.yCoord+.5, tile.zCoord+.5);
		if(distanceSq>=1000)
			return;

		BlueprintCraftingRecipe recipe = getSelectedRecipe(tile);
		if(recipe==null)
			return;
		BlueprintLines blueprint = getBlueprintDrawable(recipe);
		if(blueprint==null)
			return;
		float lineWidth = distanceSq<6?3:distanceSq<25?2:distanceSq<40?1:.5f;
		drawBlueprint(blueprint, lineWidth);
	}

	private static BlueprintCraftingRecipe getSelectedRecipe(TileEntityAutoWorkbench tile)
	{
		ItemStack blueprint = tile.inventory[0];
		String category = ItemNBTHelper.getString(blueprint, "blueprint");
		BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(category);
		if(recipes==null || tile.selectedRecipe<0 || tile.selectedRecipe>=recipes.length)
			return null;
		return recipes[tile.selectedRecipe];
	}

	private static BlueprintLines getBlueprintDrawable(BlueprintCraftingRecipe recipe)
	{
		if(CACHE.containsKey(recipe))
			return CACHE.get(recipe);
		if(FAILED.contains(recipe))
			return null;
		BlueprintLines generated;
		try
		{
			generated = createBlueprint(recipe.output);
		}
		catch(Exception ignored)
		{
			generated = null;
		}
		if(generated==null)
			FAILED.add(recipe);
		else
			CACHE.put(recipe, generated);
		return generated;
	}

	private static BlueprintLines createBlueprint(ItemStack stack)
	{
		if(stack==null || stack.getItem()==null)
			return null;
		List<BufferedImage> images = loadImages(stack);
		if(images.isEmpty())
			return null;
		HashSet<Edge> lines = new HashSet<Edge>();
		HashSet<TexturePoint> tested = new HashSet<TexturePoint>();
		HashMultimap<Integer, TexturePoint> areas = HashMultimap.create();
		int maxWidth = 0;
		for(BufferedImage image : images)
		{
			int width = image.getWidth();
			int height = Math.min(image.getHeight(), width);
			maxWidth = Math.max(maxWidth, width);
			for(int y=0; y<height; y++)
				for(int x=0; x<width; x++)
					analyzePixel(image, width, height, x, y, lines, tested, areas);
		}
		if(maxWidth<=0 || lines.isEmpty())
			return null;

		ArrayList<Integer> luminanceOrder = new ArrayList<Integer>(areas.keySet());
		Collections.sort(luminanceOrder, new Comparator<Integer>()
		{
			@Override
			public int compare(Integer first, Integer second)
			{
				return Double.compare(getLuminance(first), getLuminance(second));
			}
		});
		HashMultimap<ShadeStyle, BlueprintPoint> scaledAreas = HashMultimap.create();
		int stripeAmount = 2;
		int stripeDirection = 0;
		for(Integer colour : luminanceOrder)
		{
			ShadeStyle style = new ShadeStyle(stripeAmount, stripeDirection);
			for(TexturePoint point : areas.get(colour))
				scaledAreas.put(style, point.scaleTo(maxWidth));
			stripeDirection = (stripeDirection+1)%3;
			if(stripeDirection==0)
				stripeAmount++;
		}

		HashSet<Edge> scaledLines = new HashSet<Edge>();
		for(Edge edge : lines)
			scaledLines.add(edge.scaleTo(maxWidth));
		return new BlueprintLines(maxWidth, scaledLines, scaledAreas);
	}

	private static List<BufferedImage> loadImages(ItemStack stack)
	{
		HashSet<ResourceLocation> resources = new HashSet<ResourceLocation>();
		Item item = stack.getItem();
		Block block = Block.getBlockFromItem(item);
		if(block!=null && block!=Blocks.air)
			for(int side=0; side<6; side++)
				addIconResource(resources, block.getIcon(side, stack.getItemDamage()), true);
		int passes = Math.max(1, item.getRenderPasses(stack.getItemDamage()));
		boolean blockAtlas = item.getSpriteNumber()==0;
		for(int pass=0; pass<passes; pass++)
			addIconResource(resources, item.getIcon(stack, pass), blockAtlas);

		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
		for(ResourceLocation resource : resources)
		{
			BufferedImage image = readImage(resource);
			if(image!=null)
				images.add(image);
		}
		return images;
	}

	private static void addIconResource(Set<ResourceLocation> resources, IIcon icon, boolean blockAtlas)
	{
		if(icon==null || icon.getIconName()==null || icon.getIconName().isEmpty())
			return;
		ResourceLocation name = new ResourceLocation(icon.getIconName());
		String path = "textures/"+(blockAtlas?"blocks/":"items/")+name.getResourcePath()+".png";
		resources.add(new ResourceLocation(name.getResourceDomain(), path));
	}

	private static BufferedImage readImage(ResourceLocation location)
	{
		InputStream stream = null;
		try
		{
			IResource resource = ClientUtils.mc().getResourceManager().getResource(location);
			stream = resource.getInputStream();
			return ImageIO.read(stream);
		}
		catch(Exception ignored)
		{
			return null;
		}
		finally
		{
			if(stream!=null)
				try
				{
					stream.close();
				}
				catch(Exception ignored)
				{
				}
		}
	}

	private static void analyzePixel(BufferedImage image, int width, int height, int x, int y,
			Set<Edge> lines, Set<TexturePoint> tested,
			HashMultimap<Integer, TexturePoint> areas)
	{
		int argb = image.getRGB(x, y);
		if(((argb>>24)&255)<=0)
			return;
		float red = (argb>>16&255)/255f;
		float green = (argb>>8&255)/255f;
		float blue = (argb&255)/255f;
		float intensity = (red+green+blue)/3f;
		TexturePoint point = new TexturePoint(x, y, width);
		if(!tested.contains(point))
		{
			boolean added = false;
			for(Integer key : areas.keySet())
			{
				for(TexturePoint existing : areas.get(key))
				{
					int sampleX = scaleCoordinate(existing.x, existing.scale, width, width);
					int sampleY = scaleCoordinate(existing.y, existing.scale, width, height);
					int colour = image.getRGB(sampleX, sampleY);
					float deltaRed = red-(colour>>16&255)/255f;
					float deltaGreen = green-(colour>>8&255)/255f;
					float deltaBlue = blue-(colour&255)/255f;
					double delta = Math.sqrt(deltaRed*deltaRed+deltaGreen*deltaGreen+deltaBlue*deltaBlue);
					if(delta<.25)
					{
						areas.put(key, point);
						added = true;
						break;
					}
				}
				if(added)
					break;
			}
			if(!added)
				areas.put(argb, point);
			tested.add(point);
		}

		for(int side=0; side<4; side++)
		{
			int dx = side==0?-1:side==1?1:0;
			int dy = side==2?-1:side==3?1:0;
			int otherX = x+dx;
			int otherY = y+dy;
			float delta = 1;
			if(otherX>=0 && otherX<width && otherY>=0 && otherY<height)
			{
				int neighbour = image.getRGB(otherX, otherY);
				if(((neighbour>>24)&255)>0)
				{
					float neighbourIntensity = ((neighbour>>16&255)+(neighbour>>8&255)+(neighbour&255))/765f;
					float intensityDelta = clamp01(Math.abs(intensity-neighbourIntensity));
					float redDelta = clamp01(Math.abs(red-(neighbour>>16&255)/255f));
					float greenDelta = clamp01(Math.abs(green-(neighbour>>8&255)/255f));
					float blueDelta = clamp01(Math.abs(blue-(neighbour&255)/255f));
					delta = Math.max(intensityDelta, Math.max(redDelta, Math.max(greenDelta, blueDelta)));
					delta = delta<.25f?0:delta>.4f?1:delta;
				}
			}
			if(delta>0)
			{
				TexturePoint first = new TexturePoint(
						x+(side==1?1:0), y+(side==3?1:0), width);
				TexturePoint second = new TexturePoint(
						x+(side==0?0:side==1?1:1), y+(side==2?0:side==3?1:1), width);
				lines.add(new Edge(first, second));
			}
		}
	}

	private static int scaleCoordinate(int coordinate, int sourceScale, int targetScale, int upperBound)
	{
		if(sourceScale<=0 || targetScale<=0 || upperBound<=1)
			return 0;
		int scaled = (int)(coordinate*(targetScale/(float)sourceScale));
		return Math.max(0, Math.min(upperBound-1, scaled));
	}

	private static float clamp01(float value)
	{
		return Math.max(0, Math.min(1, value));
	}

	private static double getLuminance(int rgb)
	{
		return Math.sqrt(.241*(rgb>>16&255)+.691*(rgb>>8&255)+.068*(rgb&255));
	}

	private static void drawBlueprint(BlueprintLines blueprint, float lineWidth)
	{
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT|GL11.GL_LINE_BIT|GL11.GL_POINT_BIT
				|GL11.GL_COLOR_BUFFER_BIT|GL11.GL_CURRENT_BIT);
		GL11.glPushMatrix();
		try
		{
			GL11.glTranslatef(-.195f, .125f, .97f);
			GL11.glRotatef(-45, 1, 0, 0);
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			float scale = .0375f/(blueprint.textureScale/16f);
			GL11.glScalef(scale, -scale, scale);
			GL11.glColor4f(1, 1, 1, 1);
			blueprint.draw(lineWidth);
		}
		finally
		{
			GL11.glPopMatrix();
			GL11.glPopAttrib();
		}
	}

	private static class BlueprintLines
	{
		private final int textureScale;
		private final Set<Edge> lines;
		private final HashMultimap<ShadeStyle, BlueprintPoint> areas;

		private BlueprintLines(int textureScale, Set<Edge> lines,
				HashMultimap<ShadeStyle, BlueprintPoint> areas)
		{
			this.textureScale = textureScale;
			this.lines = lines;
			this.areas = areas;
		}

		private void draw(float lineWidth)
		{
			GL11.glLineWidth(lineWidth);
			GL11.glBegin(GL11.GL_LINES);
			for(Edge edge : lines)
			{
				GL11.glVertex3f(edge.first.x, edge.first.y, 0);
				GL11.glVertex3f(edge.second.x, edge.second.y, 0);
			}
			GL11.glEnd();
			if(lineWidth>=1)
			{
				GL11.glLineWidth(lineWidth*.66f);
				GL11.glPointSize(4);
				GL11.glBegin(GL11.GL_LINES);
				for(ShadeStyle style : areas.keySet())
					for(BlueprintPoint pixel : areas.get(style))
						style.drawShading(pixel);
				GL11.glEnd();
			}
		}
	}

	private static class ShadeStyle
	{
		private final int stripeAmount;
		private final int stripeDirection;

		private ShadeStyle(int stripeAmount, int stripeDirection)
		{
			this.stripeAmount = stripeAmount;
			this.stripeDirection = stripeDirection;
		}

		private void drawShading(BlueprintPoint pixel)
		{
			float step = 1/(float)stripeAmount;
			float offset = step/2;
			if(stripeDirection>1)
			{
				int perSide = stripeAmount/2+(stripeAmount%2==1?1:0);
				step = 1/(float)perSide;
				offset = stripeAmount%2==1?step:step/2;
			}
			for(int i=0; i<stripeAmount; i++)
				if(stripeDirection==0)
				{
					GL11.glVertex3f(pixel.x+offset+step*i, pixel.y, 0);
					GL11.glVertex3f(pixel.x+offset+step*i, pixel.y+1, 0);
				}
				else if(stripeDirection==1)
				{
					GL11.glVertex3f(pixel.x, pixel.y+offset+step*i, 0);
					GL11.glVertex3f(pixel.x+1, pixel.y+offset+step*i, 0);
				}
				else if(i==stripeAmount-1 && stripeAmount%2==1)
				{
					GL11.glVertex3f(pixel.x, pixel.y+1, 0);
					GL11.glVertex3f(pixel.x+1, pixel.y, 0);
				}
				else if(i%2==0)
				{
					GL11.glVertex3f(pixel.x, pixel.y+offset+step*(i/2), 0);
					GL11.glVertex3f(pixel.x+offset+step*(i/2), pixel.y, 0);
				}
				else
				{
					GL11.glVertex3f(pixel.x+1-offset-step*(i/2), pixel.y+1, 0);
					GL11.glVertex3f(pixel.x+1, pixel.y+1-offset-step*(i/2), 0);
				}
		}
	}

	private static class BlueprintPoint
	{
		protected final int x;
		protected final int y;

		private BlueprintPoint(int x, int y)
		{
			this.x = x;
			this.y = y;
		}

		private BlueprintPoint(BlueprintPoint other)
		{
			this(other.x, other.y);
		}

		@Override
		public boolean equals(Object object)
		{
			if(this==object)
				return true;
			if(object==null || getClass()!=object.getClass())
				return false;
			BlueprintPoint other = (BlueprintPoint)object;
			return x==other.x && y==other.y;
		}

		@Override
		public int hashCode()
		{
			return 31*x+y;
		}
	}

	private static class TexturePoint extends BlueprintPoint
	{
		private final int scale;

		private TexturePoint(int x, int y, int scale)
		{
			super(x, y);
			this.scale = scale;
		}

		private BlueprintPoint scaleTo(int targetScale)
		{
			return new BlueprintPoint((int)(x/(float)scale*targetScale), (int)(y/(float)scale*targetScale));
		}

		@Override
		public boolean equals(Object object)
		{
			if(this==object)
				return true;
			if(!(object instanceof TexturePoint))
				return false;
			TexturePoint other = (TexturePoint)object;
			return x==other.x && y==other.y && scale==other.scale;
		}

		@Override
		public int hashCode()
		{
			return 31*(31*x+y)+scale;
		}
	}

	private static class Edge
	{
		private final BlueprintPoint first;
		private final BlueprintPoint second;

		private Edge(BlueprintPoint first, BlueprintPoint second)
		{
			this.first = first;
			this.second = second;
		}

		private Edge scaleTo(int targetScale)
		{
			BlueprintPoint scaledFirst = first instanceof TexturePoint
					?((TexturePoint)first).scaleTo(targetScale):new BlueprintPoint(first);
			BlueprintPoint scaledSecond = second instanceof TexturePoint
					?((TexturePoint)second).scaleTo(targetScale):new BlueprintPoint(second);
			return new Edge(scaledFirst, scaledSecond);
		}

		@Override
		public boolean equals(Object object)
		{
			if(this==object)
				return true;
			if(!(object instanceof Edge))
				return false;
			Edge other = (Edge)object;
			return first.equals(other.first) && second.equals(other.second);
		}

		@Override
		public int hashCode()
		{
			return 31*first.hashCode()+second.hashCode();
		}
	}
}
