package blusunrize.immersiveengineering.api.tool;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Server-safe crop registry for the 1.7.10 Garden Cloche backport.
 *
 * The public name is kept as BelljarHandler for source/API continuity with the
 * upstream implementation that introduced the machine in IE 0.10-57.
 * Rendering data intentionally contains no client-only classes.
 */
public final class BelljarHandler
{
	private static final List<IPlantHandler> plantHandlers = new ArrayList<IPlantHandler>();
	private static final List<FluidFertilizerHandler> fluidFertilizers = new ArrayList<FluidFertilizerHandler>();
	private static final List<ItemFertilizerHandler> itemFertilizers = new ArrayList<ItemFertilizerHandler>();
	private static final List<SoilHandler> soilHandlers = new ArrayList<SoilHandler>();
	private static final Map<SeedKey, ResourceLocation> soilTextures = new LinkedHashMap<SeedKey, ResourceLocation>();
	private static final CompatCropHandler compatCropHandler = new CompatCropHandler();
	private static boolean initialized;

	public static float solidFertilizerModifier = 1f;
	public static float fluidFertilizerModifier = 1f;

	private BelljarHandler()
	{
	}

	public static synchronized void init()
	{
		if(initialized)
			return;
		initialized = true;

		soilTextures.put(new SeedKey(new ItemStack(Blocks.dirt)), new ResourceLocation("minecraft:blocks/farmland_wet"));

		registerHandler(new BasicCropHandler());
		registerHandler(new StemHandler());
		registerHandler(new StackingHandler());
		registerHandler(compatCropHandler);

		registerFluidFertilizer(new FluidFertilizerHandler()
		{
			@Override
			public boolean isValid(FluidStack fertilizer)
			{
				return fertilizer!=null && fertilizer.getFluid()==FluidRegistry.WATER;
			}

			@Override
			public float getGrowthMultiplier(FluidStack fertilizer, ItemStack seed, ItemStack soil, TileEntity tile)
			{
				return fluidFertilizerModifier;
			}
		});

		registerItemFertilizer(new ItemFertilizerHandler()
		{
			private final ItemStack bonemeal = new ItemStack(Items.dye, 1, 15);

			@Override
			public boolean isValid(ItemStack fertilizer)
			{
				return fertilizer!=null && OreDictionary.itemMatches(bonemeal, fertilizer, true);
			}

			@Override
			public float getGrowthMultiplier(ItemStack fertilizer, ItemStack seed, ItemStack soil, TileEntity tile)
			{
				return solidFertilizerModifier*1.25f;
			}
		});
	}

	public static void registerHandler(IPlantHandler handler)
	{
		if(handler!=null && !plantHandlers.contains(handler))
			plantHandlers.add(handler);
	}

	public static void registerHandlerFirst(IPlantHandler handler)
	{
		if(handler!=null)
		{
			plantHandlers.remove(handler);
			plantHandlers.add(0, handler);
		}
	}

	public static IPlantHandler getHandler(ItemStack seed)
	{
		if(seed==null)
			return null;
		for(IPlantHandler handler : plantHandlers)
			if(handler.isValid(seed))
				return handler;
		return null;
	}

	public static void registerFluidFertilizer(FluidFertilizerHandler handler)
	{
		if(handler!=null && !fluidFertilizers.contains(handler))
			fluidFertilizers.add(handler);
	}

	public static FluidFertilizerHandler getFluidFertilizerHandler(FluidStack fertilizer)
	{
		if(fertilizer==null)
			return null;
		for(FluidFertilizerHandler handler : fluidFertilizers)
			if(handler.isValid(fertilizer))
				return handler;
		return null;
	}

	public static void registerItemFertilizer(ItemFertilizerHandler handler)
	{
		if(handler!=null && !itemFertilizers.contains(handler))
			itemFertilizers.add(handler);
	}

	public static ItemFertilizerHandler getItemFertilizerHandler(ItemStack fertilizer)
	{
		if(fertilizer==null)
			return null;
		for(ItemFertilizerHandler handler : itemFertilizers)
			if(handler.isValid(fertilizer))
				return handler;
		return null;
	}

	/**
	 * Registers a server-safe soil extension. This is intentionally independent
	 * from client rendering and allows compat modules to provide dirt substitutes
	 * and evidence-backed growth modifiers.
	 */
	public static void registerSoilHandler(SoilHandler handler)
	{
		if(handler!=null && !soilHandlers.contains(handler))
			soilHandlers.add(handler);
	}

	public static boolean isDirtSoil(ItemStack soil)
	{
		if(soil==null)
			return false;
		if(OreDictionary.itemMatches(new ItemStack(Blocks.dirt), soil, false))
			return true;
		for(SoilHandler handler : soilHandlers)
			if(handler.isDirtReplacement(soil))
				return true;
		return false;
	}

	public static float getSoilGrowthMultiplier(ItemStack seed, ItemStack soil, TileEntity tile)
	{
		float multiplier = 1f;
		for(SoilHandler handler : soilHandlers)
			if(handler.isValid(soil))
				multiplier *= Math.max(0f, handler.getGrowthMultiplier(seed, soil, tile));
		return multiplier;
	}

	public static void registerBasicItemFertilizer(final ItemStack fertilizer, final float multiplier)
	{
		registerItemFertilizer(new ItemFertilizerHandler()
		{
			@Override
			public boolean isValid(ItemStack stack)
			{
				return stack!=null && OreDictionary.itemMatches(fertilizer, stack, false);
			}

			@Override
			public float getGrowthMultiplier(ItemStack stack, ItemStack seed, ItemStack soil, TileEntity tile)
			{
				return solidFertilizerModifier*multiplier;
			}
		});
	}

	public static ResourceLocation getSoilTexture(ItemStack soil)
	{
		if(soil==null)
			return null;
		ResourceLocation exact = soilTextures.get(new SeedKey(soil));
		if(exact!=null)
			return exact;
		return null;
	}

	public static void registerSoilTexture(ItemStack soil, ResourceLocation texture)
	{
		if(soil!=null && texture!=null)
			soilTextures.put(new SeedKey(soil), texture);
	}

	/**
	 * Registers a conventional single-block crop discovered by a soft compat
	 * module. The seed remains in the machine and is never emitted by this
	 * definition; callers must provide only the actual harvest outputs.
	 */
	public static void registerCompatCrop(ItemStack seed, ItemStack[] outputs, Block renderBlock, int maxMetadata)
	{
		if(seed==null || seed.getItem()==null || renderBlock==null)
			return;
		init();
		compatCropHandler.register(seed, outputs, dirtSoil(), renderBlock, maxMetadata);
	}

	public interface IPlantHandler
	{
		boolean isValid(ItemStack seed);

		boolean isCorrectSoil(ItemStack seed, ItemStack soil);

		float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile, float fertilizer);

		ItemStack[] getOutput(ItemStack seed, ItemStack soil, TileEntity tile);

		float resetGrowth(ItemStack seed, ItemStack soil, float growth, TileEntity tile);

		PlantRenderInfo[] getRenderInfo(ItemStack seed, ItemStack soil, float growth, TileEntity tile);

		ResourceLocation getSoilTexture(ItemStack seed, ItemStack soil, TileEntity tile);
	}

	public abstract static class PlantHandlerBase implements IPlantHandler
	{
		@Override
		public float resetGrowth(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			return 0f;
		}

		@Override
		public ResourceLocation getSoilTexture(ItemStack seed, ItemStack soil, TileEntity tile)
		{
			return null;
		}
	}

	public interface FluidFertilizerHandler
	{
		boolean isValid(FluidStack fertilizer);

		float getGrowthMultiplier(FluidStack fertilizer, ItemStack seed, ItemStack soil, TileEntity tile);
	}

	public interface ItemFertilizerHandler
	{
		boolean isValid(ItemStack fertilizer);

		float getGrowthMultiplier(ItemStack fertilizer, ItemStack seed, ItemStack soil, TileEntity tile);
	}

	public interface SoilHandler
	{
		boolean isValid(ItemStack soil);

		boolean isDirtReplacement(ItemStack soil);

		float getGrowthMultiplier(ItemStack seed, ItemStack soil, TileEntity tile);
	}

	public static final class PlantRenderInfo
	{
		public final Block block;
		public final int metadata;
		public final float scale;
		public final float xOffset;
		public final float yOffset;
		public final float zOffset;

		public PlantRenderInfo(Block block, int metadata, float scale, float xOffset, float yOffset, float zOffset)
		{
			this.block = block;
			this.metadata = metadata;
			this.scale = scale;
			this.xOffset = xOffset;
			this.yOffset = yOffset;
			this.zOffset = zOffset;
		}
	}

	private static final class SeedKey
	{
		private final Item item;
		private final int metadata;

		SeedKey(ItemStack stack)
		{
			this.item = stack.getItem();
			this.metadata = stack.getItemDamage();
		}

		@Override
		public int hashCode()
		{
			return 31*(item==null?0:item.hashCode())+metadata;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(!(obj instanceof SeedKey))
				return false;
			SeedKey other = (SeedKey)obj;
			return item==other.item && metadata==other.metadata;
		}
	}

	private static final class PlantDefinition
	{
		final ItemStack seed;
		final ItemStack[] outputs;
		final SoilMatcher soil;
		final Block renderBlock;
		final int maxMetadata;

		PlantDefinition(ItemStack seed, ItemStack[] outputs, SoilMatcher soil, Block renderBlock, int maxMetadata)
		{
			this.seed = seed;
			this.outputs = outputs;
			this.soil = soil;
			this.renderBlock = renderBlock;
			this.maxMetadata = maxMetadata;
		}
	}

	private interface SoilMatcher
	{
		boolean matches(ItemStack soil);
	}

	private static SoilMatcher dirtSoil()
	{
		return new SoilMatcher()
		{
			@Override
			public boolean matches(ItemStack soil)
			{
				return isDirtSoil(soil);
			}
		};
	}

	private static SoilMatcher exactSoil(final ItemStack required)
	{
		return new SoilMatcher()
		{
			@Override
			public boolean matches(ItemStack soil)
			{
				return soil!=null && OreDictionary.itemMatches(required, soil, false);
			}
		};
	}

	private static SoilMatcher oreSoil(final String oreName)
	{
		return new SoilMatcher()
		{
			@Override
			public boolean matches(ItemStack soil)
			{
				if(soil==null)
					return false;
				int oreId = OreDictionary.getOreID(oreName);
				for(int id : OreDictionary.getOreIDs(soil))
					if(id==oreId)
						return true;
				return false;
			}
		};
	}

	private static SoilMatcher eitherSoil(final ItemStack first, final ItemStack second)
	{
		return new SoilMatcher()
		{
			@Override
			public boolean matches(ItemStack soil)
			{
				return soil!=null && (OreDictionary.itemMatches(first, soil, false) || OreDictionary.itemMatches(second, soil, false));
			}
		};
	}

	private abstract static class RegisteredPlantHandler extends PlantHandlerBase
	{
		final Map<SeedKey, PlantDefinition> definitions = new LinkedHashMap<SeedKey, PlantDefinition>();

		void register(ItemStack seed, ItemStack[] outputs, SoilMatcher soil, Block renderBlock, int maxMetadata)
		{
			definitions.put(new SeedKey(seed), new PlantDefinition(seed.copy(), copyOutputs(outputs), soil, renderBlock, maxMetadata));
		}

		PlantDefinition get(ItemStack seed)
		{
			return seed==null?null:definitions.get(new SeedKey(seed));
		}

		@Override
		public boolean isValid(ItemStack seed)
		{
			return get(seed)!=null;
		}

		@Override
		public boolean isCorrectSoil(ItemStack seed, ItemStack soil)
		{
			PlantDefinition def = get(seed);
			return def!=null && def.soil.matches(soil);
		}

		@Override
		public ItemStack[] getOutput(ItemStack seed, ItemStack soil, TileEntity tile)
		{
			PlantDefinition def = get(seed);
			return def==null?new ItemStack[0]:copyOutputs(def.outputs);
		}
	}

	private static final class CompatCropHandler extends RegisteredPlantHandler
	{
		@Override
		public float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile, float fertilizer)
		{
			return .003125f*fertilizer;
		}

		@Override
		public PlantRenderInfo[] getRenderInfo(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			PlantDefinition def = get(seed);
			if(def==null)
				return new PlantRenderInfo[0];
			int meta = Math.min(def.maxMetadata, Math.max(0, Math.round(def.maxMetadata*growth)));
			return new PlantRenderInfo[]{new PlantRenderInfo(def.renderBlock, meta, .875f, 0f, 0f, 0f)};
		}
	}

	private static final class BasicCropHandler extends RegisteredPlantHandler
	{
		BasicCropHandler()
		{
			register(new ItemStack(Items.wheat_seeds), new ItemStack[]{new ItemStack(Items.wheat, 2), new ItemStack(Items.wheat_seeds)}, dirtSoil(), Blocks.wheat, 7);
			register(new ItemStack(Items.potato), new ItemStack[]{new ItemStack(Items.potato, 2)}, dirtSoil(), Blocks.potatoes, 7);
			register(new ItemStack(Items.carrot), new ItemStack[]{new ItemStack(Items.carrot, 2)}, dirtSoil(), Blocks.carrots, 7);
			register(new ItemStack(Items.nether_wart), new ItemStack[]{new ItemStack(Items.nether_wart, 2)}, exactSoil(new ItemStack(Blocks.soul_sand)), Blocks.nether_wart, 3);
			register(new ItemStack(Blocks.red_mushroom), new ItemStack[]{new ItemStack(Blocks.red_mushroom, 2)}, eitherSoil(new ItemStack(Blocks.mycelium), new ItemStack(Blocks.dirt, 1, 2)), Blocks.red_mushroom, 0);
			register(new ItemStack(Blocks.brown_mushroom), new ItemStack[]{new ItemStack(Blocks.brown_mushroom, 2)}, eitherSoil(new ItemStack(Blocks.mycelium), new ItemStack(Blocks.dirt, 1, 2)), Blocks.brown_mushroom, 0);
		}

		@Override
		public float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile, float fertilizer)
		{
			return .003125f*fertilizer;
		}

		@Override
		public PlantRenderInfo[] getRenderInfo(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			PlantDefinition def = get(seed);
			if(def==null)
				return new PlantRenderInfo[0];
			int meta = Math.min(def.maxMetadata, Math.max(0, Math.round(def.maxMetadata*growth)));
			return new PlantRenderInfo[]{new PlantRenderInfo(def.renderBlock, meta, .875f, 0f, 0f, 0f)};
		}
	}

	private static final class StemHandler extends RegisteredPlantHandler
	{
		private final Map<SeedKey, Block> fruits = new LinkedHashMap<SeedKey, Block>();

		StemHandler()
		{
			registerStem(new ItemStack(Items.pumpkin_seeds), new ItemStack[]{new ItemStack(Blocks.pumpkin)}, Blocks.pumpkin_stem, Blocks.pumpkin);
			registerStem(new ItemStack(Items.melon_seeds), new ItemStack[]{new ItemStack(Blocks.melon_block)}, Blocks.melon_stem, Blocks.melon_block);
		}

		private void registerStem(ItemStack seed, ItemStack[] outputs, Block stem, Block fruit)
		{
			register(seed, outputs, dirtSoil(), stem, 7);
			fruits.put(new SeedKey(seed), fruit);
		}

		@Override
		public float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile, float fertilizer)
		{
			return (growth<.5f?.00625f:.0015625f)*fertilizer;
		}

		@Override
		public float resetGrowth(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			return .5f;
		}

		@Override
		public PlantRenderInfo[] getRenderInfo(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			PlantDefinition def = get(seed);
			if(def==null)
				return new PlantRenderInfo[0];
			int stemMeta = growth>=.5f?7:Math.min(7, Math.round(growth*14f));
			if(growth<.5f)
				return new PlantRenderInfo[]{new PlantRenderInfo(def.renderBlock, stemMeta, .875f, 0f, 0f, 0f)};
			Block fruit = fruits.get(new SeedKey(seed));
			float fruitScale = Math.max(.05f, Math.min(.5f, (growth-.5f)));
			return new PlantRenderInfo[]{
					new PlantRenderInfo(def.renderBlock, stemMeta, .875f, 0f, 0f, 0f),
					new PlantRenderInfo(fruit, 0, fruitScale, 0f, .15f, .15f)};
		}
	}

	private static final class StackingHandler extends RegisteredPlantHandler
	{
		StackingHandler()
		{
			register(new ItemStack(Items.reeds), new ItemStack[]{new ItemStack(Items.reeds, 2)}, oreSoil("sand"), Blocks.reeds, 0);
			register(new ItemStack(Blocks.cactus), new ItemStack[]{new ItemStack(Blocks.cactus, 2)}, oreSoil("sand"), Blocks.cactus, 0);
		}

		@Override
		public float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile, float fertilizer)
		{
			return .003125f*fertilizer;
		}

		@Override
		public PlantRenderInfo[] getRenderInfo(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			PlantDefinition def = get(seed);
			if(def==null)
				return new PlantRenderInfo[0];
			float scale = .6875f;
			float slide = (growth-1f)*scale;
			return new PlantRenderInfo[]{
					new PlantRenderInfo(def.renderBlock, 0, scale, 0f, slide, 0f),
					new PlantRenderInfo(def.renderBlock, 0, scale, 0f, slide+scale, 0f)};
		}
	}

	private static ItemStack[] copyOutputs(ItemStack[] outputs)
	{
		if(outputs==null)
			return new ItemStack[0];
		ItemStack[] copy = new ItemStack[outputs.length];
		for(int i = 0; i < outputs.length; i++)
			copy[i] = outputs[i]==null?null:outputs[i].copy();
		return copy;
	}
}
