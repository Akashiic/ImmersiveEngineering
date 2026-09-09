package blusunrize.immersiveengineering.common.util.compat.magicalcrops;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;

/**
 * Soft compatibility for Magical Crops 4.x on Minecraft 1.7.10.
 *
 * The discovery path intentionally mirrors the installed AgriCraft 1.5.2
 * integration: scan MSeeds and ModCompat for ItemSeeds, resolve the crop block
 * through ItemSeeds, and ask BlockMagicalCrops.func_149865_P for its real crop
 * product. This keeps outputs sourced from the installed Magical Crops binary
 * rather than from a hard-coded material table.
 */
public class MagicalCropsHelper extends IECompatModule
{
	private final Set<Item> registeredSeeds = new HashSet<Item>();
	private Class<?> magicalCropClass;
	private Method getCropDrop;

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
			magicalCropClass = Class.forName("com.mark719.magicalcrops.blocks.BlockMagicalCrops");
			getCropDrop = magicalCropClass.getDeclaredMethod("func_149865_P");
			getCropDrop.setAccessible(true);

			int count = 0;
			count += registerSeedsFrom("com.mark719.magicalcrops.handlers.MSeeds");
			count += registerSeedsFrom("com.mark719.magicalcrops.handlers.ModCompat");
			IELogger.info("Garden Cloche Magical Crops compat: registered "+count+" seed -> crop product mappings from the loaded mod.");
		}
		catch(Exception e)
		{
			IELogger.error("Garden Cloche Magical Crops compat could not resolve the V4 crop API; no Magical Crops seeds were registered.");
			e.printStackTrace();
		}
	}

	private int registerSeedsFrom(String className)
	{
		int count = 0;
		try
		{
			Class<?> holder = Class.forName(className);
			for(Field field : holder.getDeclaredFields())
			{
				if(!Modifier.isStatic(field.getModifiers()))
					continue;
				field.setAccessible(true);
				Object value = field.get(null);
				if(!(value instanceof ItemSeeds))
					continue;

				ItemSeeds seed = (ItemSeeds)value;
				if(!registeredSeeds.add(seed))
					continue;
				Block crop = seed.getPlant(null, 0, 0, 0);
				if(crop==null || !magicalCropClass.isInstance(crop))
					continue;
				Object dropValue = getCropDrop.invoke(crop);
				if(!(dropValue instanceof Item))
					continue;

				Item drop = (Item)dropValue;
				BelljarHandler.registerCompatCrop(new ItemStack(seed), new ItemStack[]{new ItemStack(drop)}, crop, 7);
				count++;
			}
		}
		catch(ClassNotFoundException e)
		{
			IELogger.warn("Garden Cloche Magical Crops compat: optional holder not present: "+className);
		}
		catch(Exception e)
		{
			IELogger.error("Garden Cloche Magical Crops compat failed while scanning "+className);
			e.printStackTrace();
		}
		return count;
	}
}
