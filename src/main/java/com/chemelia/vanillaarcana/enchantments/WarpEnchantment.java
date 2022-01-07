package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

//////////
// WARP //
//////////
//I:   Teleport using XP!

public class WarpEnchantment extends SpellEnchantment {
    private final static int SPELL_COOLDOWN = 3;
    private final static int SPELL_COST = 15;
    private final static int MAX_LEVEL = 5;
    private final static int PROJECTILE_SPEED = 1;
    public static final String ID = VanillaArcana.MOD_ID + ":warp";

    public WarpEnchantment() {
        super(Rarity.RARE, SPELL_COOLDOWN, SPELL_COST);
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


            ThrownEnderpearl pearl = new ThrownEnderpearl(world, user);
            pearl.setPos(pos.x, pos.y, pos.z);
            pearl.setDeltaMovement(look.scale(PROJECTILE_SPEED*(spellLevel+1)));
            world.addFreshEntity(pearl);
            
            return true;
        } else return false;
    }   
    
}
