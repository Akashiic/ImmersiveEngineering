package blusunrize.immersiveengineering.common.fluids;

import java.util.List;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

/** Minimal legacy-native base for modern IE fluids on Forge 1.7.10. */
public class BlockIEFluidLegacy extends BlockFluidClassic
{
	public final String ieName;
	private PotionEffect[] potionEffects;

	public BlockIEFluidLegacy(String name, Fluid fluid, Material material)
	{
		super(fluid, material);
		this.ieName = name;
		setBlockName(ImmersiveEngineering.MODID+"."+name);
		GameRegistry.registerBlock(this, name);
		fluid.setBlock(this);
	}

	public BlockIEFluidLegacy setPotionEffects(PotionEffect... effects)
	{
		this.potionEffects = effects;
		return this;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		if(potionEffects!=null&&entity instanceof EntityLivingBase)
			for(PotionEffect effect : potionEffects)
				if(effect!=null)
					((EntityLivingBase)entity).addPotionEffect(new PotionEffect(effect));
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{
		// Fluid blocks are intentionally hidden as standalone inventory blocks.
	}
}
