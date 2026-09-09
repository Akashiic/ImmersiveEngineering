package blusunrize.immersiveengineering.common.util.compat.agricraft;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.IPlantHandler;
import blusunrize.immersiveengineering.api.tool.BelljarHandler.PlantRenderInfo;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

/** Soft integration for the installed AgriCraft 1.5.2-evernife-v1 runtime. */
public class AgriCraftHelper extends IECompatModule
{
	private static final float CLOCHE_BASE_GROWTH_STEP = .003125f;
	private final Random fallbackRandom = new Random();

	private Object apiV2;
	private Method apiIsHandled;
	private Method apiGetSeedStats;
	private Method statsGetGrowth;
	private Method statsGetGain;
	private Method statsGetStrength;
	private Method isValidSeed;
	private Method getPlantFromStack;
	private Method plantGetGrowthRate;
	private Method plantGetFruitsOnHarvest;
	private Method plantGetBlock;
	private Method plantGetGrowthRequirement;
	private Method requirementIsValidSoil;
	private Constructor<?> blockWithMetaConstructor;
	private java.lang.reflect.Field growthMultiplierField;

	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
	}

	@Override
	public void postInit()
	{
		try
		{
			resolveReflection();
			BelljarHandler.registerHandlerFirst(new AgriPlantHandler());
			IELogger.info("Garden Cloche AgriCraft compat: genetic seed handler registered.");
		}
		catch(Exception e)
		{
			IELogger.error("Garden Cloche AgriCraft compat could not resolve API v2/runtime crop methods: "+e.getMessage());
		}
	}

	private void resolveReflection() throws Exception
	{
		Class<?> apiEntry = Class.forName("com.InfinityRaider.AgriCraft.api.API");
		apiV2 = apiEntry.getMethod("getAPI", int.class).invoke(null, 2);
		apiIsHandled = apiV2.getClass().getMethod("isHandledByAgricraft", ItemStack.class);
		apiGetSeedStats = apiV2.getClass().getMethod("getSeedStats", ItemStack.class);

		Class<?> seedStats = Class.forName("com.InfinityRaider.AgriCraft.api.v2.ISeedStats");
		statsGetGrowth = seedStats.getMethod("getGrowth");
		statsGetGain = seedStats.getMethod("getGain");
		statsGetStrength = seedStats.getMethod("getStrength");

		Class<?> cropPlantHandler = Class.forName("com.InfinityRaider.AgriCraft.farming.CropPlantHandler");
		Class<?> cropPlant = Class.forName("com.InfinityRaider.AgriCraft.farming.cropplant.CropPlant");
		isValidSeed = cropPlantHandler.getMethod("isValidSeed", ItemStack.class);
		getPlantFromStack = cropPlantHandler.getMethod("getPlantFromStack", ItemStack.class);
		plantGetGrowthRate = cropPlant.getMethod("getGrowthRate");
		plantGetFruitsOnHarvest = cropPlant.getMethod("getFruitsOnHarvest", int.class, Random.class);
		plantGetBlock = cropPlant.getMethod("getBlock");
		plantGetGrowthRequirement = cropPlant.getMethod("getGrowthRequirement");

		Class<?> blockWithMeta = Class.forName("com.InfinityRaider.AgriCraft.api.v1.BlockWithMeta");
		Class<?> growthRequirement = Class.forName("com.InfinityRaider.AgriCraft.api.v1.IGrowthRequirement");
		requirementIsValidSoil = growthRequirement.getMethod("isValidSoil", blockWithMeta);
		blockWithMetaConstructor = blockWithMeta.getConstructor(Block.class, int.class);

		Class<?> configuration = Class.forName("com.InfinityRaider.AgriCraft.handler.ConfigurationHandler");
		growthMultiplierField = configuration.getField("growthMultiplier");
	}

	private boolean hasGeneticStats(ItemStack seed)
	{
		if(seed==null || !seed.hasTagCompound())
			return false;
		return seed.getTagCompound().hasKey("growth")
				&& seed.getTagCompound().hasKey("gain")
				&& seed.getTagCompound().hasKey("strength");
	}

	private boolean isRecognizedGeneticSeed(ItemStack seed)
	{
		if(!hasGeneticStats(seed))
			return false;
		try
		{
			return Boolean.TRUE.equals(isValidSeed.invoke(null, seed))
					&& Boolean.TRUE.equals(apiIsHandled.invoke(apiV2, seed))
					&& apiGetSeedStats.invoke(apiV2, seed)!=null;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	private Object getPlant(ItemStack seed)
	{
		try
		{
			return getPlantFromStack.invoke(null, seed);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	private Object getStats(ItemStack seed)
	{
		try
		{
			return apiGetSeedStats.invoke(apiV2, seed);
		}
		catch(Exception e)
		{
			return null;
		}
	}

	private int getStat(ItemStack seed, Method getter)
	{
		Object stats = getStats(seed);
		if(stats==null)
			return 1;
		try
		{
			return Math.max(1, ((Number)getter.invoke(stats)).intValue());
		}
		catch(Exception e)
		{
			return 1;
		}
	}

	private float getGlobalGrowthMultiplier()
	{
		try
		{
			return Math.max(0f, growthMultiplierField.getFloat(null));
		}
		catch(Exception e)
		{
			return 1f;
		}
	}

	private static Block itemBlock(ItemStack stack)
	{
		return stack!=null && stack.getItem()!=null?Block.getBlockFromItem(stack.getItem()):null;
	}

	private static int itemBlockMetadata(ItemStack stack)
	{
		if(stack==null || stack.getItem()==null)
			return 0;
		int metadata = stack.getItemDamage();
		if(stack.getItem() instanceof ItemBlock)
			metadata = ((ItemBlock)stack.getItem()).getMetadata(metadata);
		return metadata;
	}

	private boolean testSoil(Object requirement, Block block, int metadata)
	{
		if(requirement==null || block==null || block==Blocks.air)
			return false;
		try
		{
			Object blockWithMeta = blockWithMetaConstructor.newInstance(block, metadata);
			return Boolean.TRUE.equals(requirementIsValidSoil.invoke(requirement, blockWithMeta));
		}
		catch(Exception e)
		{
			return false;
		}
	}

	private boolean isCorrectSoil(Object plant, ItemStack soil)
	{
		if(plant==null || soil==null)
			return false;
		try
		{
			Object requirement = plantGetGrowthRequirement.invoke(plant);
			if(requirement==null)
				return BelljarHandler.isDirtSoil(soil);

			Block actualBlock = itemBlock(soil);
			int actualMeta = itemBlockMetadata(soil);
			if(testSoil(requirement, actualBlock, actualMeta))
				return true;

			return BelljarHandler.isDirtSoil(soil)
					&& testSoil(requirement, Blocks.farmland, 0);
		}
		catch(Exception e)
		{
			return false;
		}
	}

	private int getRenderMetadata(Object plant, Block block, int growthStage)
	{
		try
		{
			Method transform = plant.getClass().getMethod("transformMeta", int.class);
			return ((Number)transform.invoke(plant, growthStage)).intValue();
		}
		catch(NoSuchMethodException ignored)
		{
			if(block==Blocks.nether_wart)
				return growthStage>=7?3:growthStage<4?0:1;
			return growthStage;
		}
		catch(Exception e)
		{
			return growthStage;
		}
	}

	private PlantRenderInfo[] getRenderInfo(Object plant, float growth)
	{
		if(plant==null)
			return new PlantRenderInfo[0];
		try
		{
			Block block = (Block)plantGetBlock.invoke(plant);
			if(block==null || block==Blocks.air)
				return new PlantRenderInfo[0];
			int stage = Math.max(0, Math.min(7, Math.round(7f*growth)));
			int meta = getRenderMetadata(plant, block, stage);

			try
			{
				Method maxBottomMethod = plant.getClass().getMethod("maxMetaBottomBlock");
				int maxBottom = ((Number)maxBottomMethod.invoke(plant)).intValue();
				if(stage>maxBottom)
				{
					int bottomMeta = getRenderMetadata(plant, block, maxBottom);
					return new PlantRenderInfo[]{
							new PlantRenderInfo(block, bottomMeta, .875f, 0f, 0f, 0f),
							new PlantRenderInfo(block, meta, .875f, 0f, .8125f, 0f)};
				}
			}
			catch(NoSuchMethodException ignored)
			{
			}
			catch(Exception ignored)
			{
			}

			return new PlantRenderInfo[]{new PlantRenderInfo(block, meta, .875f, 0f, 0f, 0f)};
		}
		catch(Exception e)
		{
			return new PlantRenderInfo[0];
		}
	}

	private final class AgriPlantHandler implements IPlantHandler
	{
		@Override
		public boolean isValid(ItemStack seed)
		{
			return isRecognizedGeneticSeed(seed);
		}

		@Override
		public boolean isCorrectSoil(ItemStack seed, ItemStack soil)
		{
			return AgriCraftHelper.this.isCorrectSoil(getPlant(seed), soil);
		}

		@Override
		public float getGrowthStep(ItemStack seed, ItemStack soil, float growth, TileEntity tile, float fertilizer)
		{
			Object plant = getPlant(seed);
			if(plant==null)
				return 0f;
			try
			{
				float growthRate = ((Number)plantGetGrowthRate.invoke(plant)).floatValue();
				float growthBonus = 1f+getStat(seed, statsGetGrowth)/10f;
				float probability = growthRate*growthBonus*getGlobalGrowthMultiplier()/100f;
				return CLOCHE_BASE_GROWTH_STEP*Math.max(0f, probability)*fertilizer;
			}
			catch(Exception e)
			{
				return 0f;
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public ItemStack[] getOutput(ItemStack seed, ItemStack soil, TileEntity tile)
		{
			Object plant = getPlant(seed);
			if(plant==null)
				return new ItemStack[0];
			try
			{
				Random random = tile!=null && tile.getWorldObj()!=null?tile.getWorldObj().rand:fallbackRandom;
				List<ItemStack> harvested = (List<ItemStack>)plantGetFruitsOnHarvest.invoke(
						plant, getStat(seed, statsGetGain), random);
				ArrayList<ItemStack> outputs = new ArrayList<ItemStack>();
				if(harvested!=null)
					for(ItemStack stack : harvested)
						if(stack!=null && stack.getItem()!=null && stack.stackSize>0)
							outputs.add(stack.copy());
				return outputs.toArray(new ItemStack[outputs.size()]);
			}
			catch(Exception e)
			{
				return new ItemStack[0];
			}
		}

		@Override
		public float resetGrowth(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			return 0f;
		}

		@Override
		public PlantRenderInfo[] getRenderInfo(ItemStack seed, ItemStack soil, float growth, TileEntity tile)
		{
			return AgriCraftHelper.this.getRenderInfo(getPlant(seed), growth);
		}

		@Override
		public ResourceLocation getSoilTexture(ItemStack seed, ItemStack soil, TileEntity tile)
		{
			return null;
		}
	}
}
