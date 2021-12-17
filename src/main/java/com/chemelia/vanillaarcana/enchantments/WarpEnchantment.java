package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

//////////
//SYPHON//
//////////
//I:   Spend XP to shoot a projectile. Gain some if you hit, lose more if you miss.

public class WarpEnchantment extends SpellEnchantment {
    public static int spellCooldown = 3;
    private final static int SPELL_COST = 5;
    private final static int PROJECTILE_SPEED = 1;

    public WarpEnchantment() {
        super(Rarity.UNCOMMON, spellCooldown, SPELL_COST);
    }

    public static final String ID = VanillaArcana.MOD_ID + ":syphon";

    @Override
    public int getMaxLevel(){
        return 5;
    }

    


    public boolean handleCast(Level world, Player player, ItemStack stack){
        if (world.isClientSide()){
            return false;
        }
        Vec3 look = player.getLookAngle();
        Vec3 pos = player.getEyePosition().add(look.scale(0.9));

        if (player.totalExperience < SPELL_COST && !player.isCreative()){
            player.getCooldowns().addCooldown(stack.getItem(), spellCooldown*10);
            world.playSound(null, player.blockPosition(), RegistryHandler.SPELL_FAIL.get(), SoundSource.PLAYERS, 1, 0.9F);
            if (world instanceof ServerLevel){
                ((ServerLevel) world).sendParticles(ParticleTypes.SMOKE, pos.x,pos.y,pos.z, 15, 0, 0, 0, 0.01);
            }
            return false;
        }

        int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
        if (!player.isCreative()){
            player.giveExperiencePoints(-SPELL_COST * spellLevel);
        }
        world.playSound(null, player.blockPosition(), RegistryHandler.SPELL_CAST.get(), SoundSource.PLAYERS, 1, 1.5F/spellLevel);
        
        // Snowball snowball = new Snowball(world, player);
        // snowball.setPos(pos.x, pos.y, pos.z);
        // snowball.setDeltaMovement(look.scale(PROJECTILE_SPEED));
        // world.addFreshEntity(snowball);


        ThrownEnderpearl pearl = new ThrownEnderpearl(world, player);
        pearl.setPos(pos.x, pos.y, pos.z);
        pearl.setDeltaMovement(look.scale(PROJECTILE_SPEED*(spellLevel+1)));
        world.addFreshEntity(pearl);

        //SpectralArrow arrow = new SpectralArrow(world, pos.x, pos.y, pos.z);
        // arrow.setDeltaMovement(look.scale(PROJECTILE_SPEED));
        // world.addFreshEntity(arrow);

        player.getCooldowns().addCooldown(stack.getItem(), spellCooldown*spellLevel);

        return true;
    }


    
    
    
}
