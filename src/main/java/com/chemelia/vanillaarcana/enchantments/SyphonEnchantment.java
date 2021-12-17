package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

//////////
//SYPHON//
//////////
//I:   Spend XP to shoot a projectile. Gain some if you hit, lose more if you miss.

public class SyphonEnchantment extends SpellEnchantment {
    private final static int SPELL_COST = 5;
    private final static int PROJECTILE_SPEED = 2;

    public SyphonEnchantment() {
        super(Rarity.UNCOMMON, 3, SPELL_COST);
    }

    public static final String ID = VanillaArcana.MOD_ID + ":syphon";

    @Override
    public int getMaxLevel(){
        return 5;
    }

    

    
    //Suck XP out of creatures when they're hit (left click)
    @Override
    public void doPostAttack(LivingEntity attacker, Entity target, int spellLevel){
        if (!attacker.level.isClientSide()){
            ServerLevel world = (ServerLevel) attacker.level;
            
            attacker.level.playSound(null, attacker.blockPosition(), RegistryHandler.SPELL_SAP.get(), SoundSource.PLAYERS, 1, 2.0F);

            if (target instanceof Player){
                ((Player) target).giveExperiencePoints(-(spellLevel + 1));
            }
            for (int i = 0; i< spellLevel; ++i){
                ExperienceOrb orb = new ExperienceOrb(world, target.position().x, target.position().y, target.position().z, SPELL_COST + 2);
                world.addFreshEntity(orb);
            }
            
            
        }
        
        
    }


    public boolean handleCast(Level world, Player player, ItemStack stack){
        if (world.isClientSide()){
            return false;
        }
        Vec3 look = player.getLookAngle();
        Vec3 pos = player.getEyePosition().add(look.scale(0.9));

        if (player.totalExperience < this.spellCost && !player.isCreative()){
            player.getCooldowns().addCooldown(stack.getItem(), spellCooldown*10);
            world.playSound(null, player.blockPosition(), RegistryHandler.SPELL_FAIL.get(), SoundSource.PLAYERS, 1, 0.9F);
            if (world instanceof ServerLevel){
                ((ServerLevel) world).sendParticles(ParticleTypes.SMOKE, pos.x,pos.y,pos.z, 15, 0, 0, 0, 0.1);
            }
            return false;
        }

        int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
        if (!player.isCreative()){
            player.giveExperiencePoints(-this.spellCost * spellLevel);
        }
        world.playSound(null, player.blockPosition(), RegistryHandler.SPELL_CAST.get(), SoundSource.PLAYERS, 1, 1.5F/spellLevel);
        
        Snowball snowball = new Snowball(world, player);
        snowball.setPos(pos.x, pos.y, pos.z);
        snowball.setDeltaMovement(look.scale(PROJECTILE_SPEED));
        world.addFreshEntity(snowball);


        // ThrownEnderpearl pearl = new ThrownEnderpearl(world, player);
        // pearl.setPos(pos.x, pos.y, pos.z);
        // pearl.setDeltaMovement(look.scale(PROJECTILE_SPEED));
        // world.addFreshEntity(pearl);

        //SpectralArrow arrow = new SpectralArrow(world, pos.x, pos.y, pos.z);
        // arrow.setDeltaMovement(look.scale(PROJECTILE_SPEED));
        // world.addFreshEntity(arrow);

        player.getCooldowns().addCooldown(stack.getItem(), spellCooldown*spellLevel);

        return true;
    }


    
    
    
}
