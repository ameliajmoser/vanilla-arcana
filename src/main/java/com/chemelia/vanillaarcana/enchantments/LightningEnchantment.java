package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
/////////////
//LIGHTNING//
/////////////
//Shoot a projectile that creates a lightning strike, or create a lightning strike at the block you're looking at?
import net.minecraft.world.phys.Vec3;

public class LightningEnchantment extends SpellEnchantment {
    private final static int SPELL_COOLDOWN = 3;
    private final static int SPELL_COST = 5;
    private final static int MAX_LEVEL = 5;
    public static final String ID = VanillaArcana.MOD_ID + ":lightning";

    public LightningEnchantment() {
        super(Rarity.COMMON, SPELL_COOLDOWN, SPELL_COST);
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
            LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
            bolt.setPos(pos);
            bolt.setXRot(90);
            world.addFreshEntity(bolt);
            return true;
        } else return false;
    }
}
