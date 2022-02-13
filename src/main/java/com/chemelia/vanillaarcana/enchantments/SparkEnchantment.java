package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.VanillaArcana;
import com.chemelia.vanillaarcana.entity.projectile.LightningProjectile;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
/////////////
//LIGHTNING//
/////////////
//Shoot a projectile that creates a lightning strike, or create a lightning strike at the block you're looking at?
import net.minecraft.world.phys.Vec3;

public class SparkEnchantment extends SpellEnchantment {
    private final static int SPELL_COOLDOWN = 20;
    private final static int SPELL_COST = 15;
    private final static int MAX_LEVEL = 3;
    public static final String ID = VanillaArcana.MOD_ID + ":spark";

    public SparkEnchantment() {
        super(Rarity.UNCOMMON, SPELL_COOLDOWN, SPELL_COST);
    }

    @Override
    public int getMaxLevel(){
        return MAX_LEVEL;
    }

    @Override
    public boolean handleCast(Level world, LivingEntity user, ItemStack stack){
        if (super.handleCast(world, user, stack)){
            int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
            Vec3 look = user.getLookAngle();
            Vec3 pos = user.getEyePosition().add(look.scale(0.9));
            Vec3 velocity = look.scale(1.5);
            LightningProjectile bolt = new LightningProjectile(world, user, spellLevel);
            bolt.setPos(pos.x, pos.y, pos.z);
            bolt.shoot(look.x, look.y, look.z, 1F, 0.1F);
            world.addFreshEntity(bolt);
            return true;
        } else return false;
    }
   
}
