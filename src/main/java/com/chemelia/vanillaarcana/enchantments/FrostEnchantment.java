package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.VanillaArcana;
import com.chemelia.vanillaarcana.entity.projectile.FrostSnowball;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

///////////
// FROST //
///////////
//I:   Snowball projectile that applies powdered snow frost effect.

public class FrostEnchantment extends SpellEnchantment {
    private final static int SPELL_COOLDOWN = 10;
    private final static int SPELL_COST = 10;
    private final static int MAX_LEVEL = 5;
    private final static int PROJECTILE_SPEED = 1;
    public static final String ID = VanillaArcana.MOD_ID + ":syphon";

    public FrostEnchantment() {
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

            FrostSnowball frostBall = new FrostSnowball(world, user, spellLevel);
            frostBall.setPos(pos.x, pos.y, pos.z);
            frostBall.setDeltaMovement(look.scale(PROJECTILE_SPEED));
            world.addFreshEntity(frostBall);
        
            if (user instanceof Player) {
                ((Player) user).getCooldowns().addCooldown(stack.getItem(), SPELL_COOLDOWN * spellLevel * spellLevel);
            }
            
            return true;
        } else return false;
    }

    @Override
    protected void spawnSuccessParticle(Level world, Vec3 pos) {
        ((ServerLevel) world).sendParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y, pos.z, 10, 0, 0, 0, 0.1);
    }

}
