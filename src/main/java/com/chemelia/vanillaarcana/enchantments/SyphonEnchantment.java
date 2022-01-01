package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.VanillaArcana;
import com.chemelia.vanillaarcana.entity.projectile.SyphonSnowball;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

//////////
//SYPHON//
//////////
//I:   Spend XP to shoot a projectile. Gain some if you hit, lose more if you miss.

public class SyphonEnchantment extends SpellEnchantment {
    private final static int SPELL_COOLDOWN = 3;
    private final static int SPELL_COST = 5;
    private final static int MAX_LEVEL = 5;
    private final static int PROJECTILE_SPEED = 1;
    public static final String ID = VanillaArcana.MOD_ID + ":syphon";

    public SyphonEnchantment() {
        super(Rarity.UNCOMMON, SPELL_COOLDOWN, SPELL_COST);
    }

    @Override
    public int getMaxLevel(){
        return MAX_LEVEL;
    }

    //Suck XP out of creatures when they're hit (left click)
    @Override
    public void doPostAttack(LivingEntity attacker, Entity target, int spellLevel){
        if (!attacker.level.isClientSide()){
            ServerLevel world = (ServerLevel) attacker.level;
            attacker.level.playSound(null, attacker.blockPosition(), RegistryHandler.SPELL_SAP.get(), SoundSource.PLAYERS, 1, 0.5F);
            if (target instanceof Player){
                ((Player) target).giveExperiencePoints(-(spellLevel + 1));
            }
            for (int i = 0; i< spellLevel; ++i){
                ExperienceOrb orb = new ExperienceOrb(world, target.position().x, target.position().y + 1, target.position().z, 1);
                world.addFreshEntity(orb);
            }
        }
    }

    @Override
    public boolean handleCast(Level world, LivingEntity user, ItemStack stack){
        if (super.handleCast(world, user, stack)){
            int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
            Vec3 look = user.getLookAngle();
            Vec3 pos = user.getEyePosition().add(look.scale(0.9));


            SyphonSnowball spellBall = new SyphonSnowball(world, user, spellLevel);
            spellBall.setPos(pos.x, pos.y, pos.z);
            spellBall.setDeltaMovement(look.scale(PROJECTILE_SPEED));
            world.addFreshEntity(spellBall);
            
            if (user instanceof Player) {
                ((Player) user).getCooldowns().addCooldown(stack.getItem(), SPELL_COOLDOWN * spellLevel * spellLevel);
            }
            return true;
        } else return false;
    }
}
