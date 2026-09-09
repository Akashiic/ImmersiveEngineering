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
import net.minecraft.item.ItemBlock;
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
				/*
				 * Forge 1.7.10 BlockCrops gives a sustaining non-fertile center soil a
				 * contribution of 1 and an isFertile() soil a contribution of 3. With
				 * the base growth factor of 1 this is 2 versus 4, exactly a 2x ratio.
				 */
				return 2f;
			}
		});
		IELogger.info("Garden Cloche MFR compat: registered Fertile Soil as a 2.0x dirt substitute.");
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
