package blusunrize.immersiveengineering.common.util.compat.mfr;

import blusunrize.immersiveengineering.api.tool.BelljarHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler;
import blusunrize.immersiveengineering.api.tool.ChemthrowerHandler.ChemthrowerEffect_Potion;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.IEPotions;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockStem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import powercrystals.minefactoryreloaded.api.FactoryRegistry;

public class MFRHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void postInit()
	{
		final Block fertileSoil = GameRegistry.findBlock("MineFactoryReloaded", "farmland");
		if(fertileSoil==null)
		{
			IELogger.warn("Garden Cloche MFR compat: MineFactoryReloaded:farmland was not found; fertile-soil support disabled.");
			return;
		}

		BelljarHandler.registerSoilHandler(new BelljarHandler.SoilHandler()
		{
			private boolean matchesFertileSoil(ItemStack soil)
			{
				if(soil==null || soil.getItem()==null || Block.getBlockFromItem(soil.getItem())!=fertileSoil)
					return false;
				int effectiveMetadata = soil.getItemDamage();
				if(soil.getItem() instanceof ItemBlock)
					effectiveMetadata = ((ItemBlock)soil.getItem()).getMetadata(soil.getItemDamage());
				return effectiveMetadata>0;
			}

			private boolean hasAgriStats(ItemStack seed)
			{
				return seed!=null && seed.hasTagCompound()
						&& seed.getTagCompound().hasKey("growth")
						&& seed.getTagCompound().hasKey("gain")
						&& seed.getTagCompound().hasKey("strength");
			}

			private Block getSeedPlant(ItemStack seed)
			{
				if(seed==null || !(seed.getItem() instanceof ItemSeeds))
					return null;
				return ((ItemSeeds)seed.getItem()).getPlant(null, 0, 0, 0);
			}

			private boolean isMagicalCrop(Block plant)
			{
				Class<?> type = plant==null?null:plant.getClass();
				while(type!=null)
				{
					if("com.mark719.magicalcrops.blocks.BlockMagicalCrops".equals(type.getName()))
						return true;
					type = type.getSuperclass();
				}
				return false;
			}

			@Override
			public boolean isValid(ItemStack soil)
			{
				return matchesFertileSoil(soil);
			}

			@Override
			public boolean isDirtReplacement(ItemStack soil)
			{
				return matchesFertileSoil(soil);
			}

			@Override
			public float getGrowthMultiplier(ItemStack seed, ItemStack soil, TileEntity tile)
			{
				if(hasAgriStats(seed))
					return 1f;

				Block plant = getSeedPlant(seed);
				if(isMagicalCrop(plant))
					return 31f/11f;
				if(seed!=null && seed.getItem()==IEContent.itemSeeds)
					return 1.5f;
				if(plant instanceof BlockCrops || plant instanceof BlockStem
						|| seed!=null && (seed.getItem()==Items.carrot || seed.getItem()==Items.potato))
					return 2f;
				return 1f;
			}
		});
		IELogger.info("Garden Cloche MFR compat: registered Fertile Soil with crop-aware growth multipliers.");
	}

	@Override
	public void init()
	{
		FactoryRegistry.sendMessage("registerPlantable",new IEPlantable(IEContent.itemSeeds, IEContent.blockCrop));
		FactoryRegistry.sendMessage("registerHarvestable", new IEHarvestable());

		ChemthrowerHandler.registerEffect("sewage", new ChemthrowerEffect_Potion(null,0, new PotionEffect(Potion.poison.id,60,0)));
		ChemthrowerHandler.registerEffect("sludge", new ChemthrowerEffect_Potion(null,0, new PotionEffect(Potion.wither.id,20,0),new PotionEffect(Potion.confusion.id,40,0)));
		ChemthrowerHandler.registerEffect("pinkslime", new ChemthrowerEffect_Potion(null,0, IEPotions.sticky,100,1));
	}
}
