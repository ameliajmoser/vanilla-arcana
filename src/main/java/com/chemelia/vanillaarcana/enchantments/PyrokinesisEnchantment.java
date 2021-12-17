package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

///////////////
//PYROKINESIS//
///////////////
//I:    Blaze fireball
//II:   Ghast fireball
//III:  Conjure lava? (infinite lava bucket)
//IV:   Summon blaze
//V:    Dragon fireball? Fire everywhere

public class PyrokinesisEnchantment extends SpellEnchantment {
    public PyrokinesisEnchantment() {
        super(Rarity.UNCOMMON, 5, 14);
    }

    public static final String ID = VanillaArcana.MOD_ID + ":pyrokinesis";

    @Override
    public int getMaxLevel(){
        return 4;
    }

    public boolean handleCast(Level world, Player player, ItemStack stack){
        if (world.isClientSide()){
            return false;
        }
        Vec3 look = player.getLookAngle();
        Vec3 pos = player.getEyePosition().add(look.scale(0.9));
        Vec3 velocity = look.scale(0.3);

        if (player.totalExperience < SPELL_COST && !player.isCreative()){
            player.getCooldowns().addCooldown(stack.getItem(), spellCooldown*10);
            world.playSound(null, player.blockPosition(), RegistryHandler.SPELL_FAIL.get(), SoundSource.PLAYERS, 1, 0.9F);
            if (world instanceof ServerLevel){
                ((ServerLevel) world).sendParticles(ParticleTypes.SMOKE, player.getEyePosition().x,player.getEyePosition().y,player.getEyePosition().z, 15, 0, 0, 0, 0.1);
            }
            return false;
        }

        int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
        if (!player.isCreative()){
            player.giveExperiencePoints(-SPELL_COST * spellLevel);
        }
        world.playSound(null, player.blockPosition(), RegistryHandler.SPELL_CAST.get(), SoundSource.PLAYERS, 1, 1.5F/spellLevel);
        
        

        switch (spellLevel){
            case 0:
                return false;
            case 1:
                SmallFireball blazeFireball = new SmallFireball(world, player, 0,0,0);
                blazeFireball.setPos(pos.x, pos.y, pos.z);
                blazeFireball.xPower = velocity.x;
                blazeFireball.yPower = velocity.y;
                blazeFireball.zPower = velocity.z;
                world.addFreshEntity(blazeFireball);
                player.getCooldowns().addCooldown(stack.getItem(), spellCooldown);
                break;
            case 2:
                velocity = look.scale(0.2);
                //last parameter is explosionpower
                LargeFireball ghastFireball = new LargeFireball(world, player, 0,0,0, 1);
                ghastFireball.setPos(pos.x, pos.y, pos.z);
                ghastFireball.xPower = velocity.x;
                ghastFireball.yPower = velocity.y;
                ghastFireball.zPower = velocity.z;
                world.addFreshEntity(ghastFireball);
                player.getCooldowns().addCooldown(stack.getItem(), spellCooldown*spellLevel*spellLevel);
                break;
            case 3:
                velocity = look.scale(0.1);
                //last parameter is explosionpower
                LargeFireball bigFireball = new LargeFireball(world, player, 0,0,0, 3);
                bigFireball.setPos(pos.x, pos.y, pos.z);
                bigFireball.xPower = velocity.x;
                bigFireball.yPower = velocity.y;
                bigFireball.zPower = velocity.z;
                world.addFreshEntity(bigFireball);
                player.getCooldowns().addCooldown(stack.getItem(), spellCooldown*spellLevel*spellLevel);
                break;
            default:
                break;
        }
        return true;
    }


    
    
    
}
