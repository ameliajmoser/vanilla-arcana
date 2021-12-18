package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.KnockbackEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class AerothurgeEnchantment extends SpellEnchantment {
    private final static int SPELL_COOLDOWN = 7;
    private final static int SPELL_COST = 14;
    private final static int MAX_LEVEL = 4;
    public static final String ID = VanillaArcana.MOD_ID + ":aerothurge";

    
    public AerothurgeEnchantment() {
        super(Rarity.RARE, SPELL_COOLDOWN, SPELL_COST);
    }

    @Override
    public int getMaxLevel(){
        return MAX_LEVEL;
    }

    @Override
    public void doPostAttack(LivingEntity attacker, Entity target, int spellLevel){

        Vec3 velocity = attacker.getLookAngle().scale(3);
        target.setDeltaMovement(velocity);
        //target.push(velocity.x, velocity.y, velocity.z);
    }

    @Override
    public boolean handleClientCast(Level world, LivingEntity user, ItemStack stack){
        int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
        Vec3 look = user.getLookAngle();
        if (user instanceof Player){
            Player playerUser = (Player) user;
            if (playerUser.totalExperience < spellCost*spellLevel && !playerUser.isCreative()){
                return false;
            } 
        }
        user.setDeltaMovement(look.scale(1.1 * spellLevel));
        return true;  
    }


    @Override
    public boolean handleCast(Level world, LivingEntity user, ItemStack stack){
        if (super.handleCast(world, user, stack)){
            int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);            
            
            if (user instanceof Player){
                ((Player) user).getCooldowns().addCooldown(stack.getItem(), SPELL_COOLDOWN*spellLevel*spellLevel);
            }
            return true;
        } else return false;
    }

}