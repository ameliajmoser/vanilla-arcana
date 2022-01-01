package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

////////////
//  WARD  //
////////////
//I:   Spend XP to put up a shield made of fragile blocks (ie tinted glass) that decays after a short time.

public class AegisEnchantment extends SpellEnchantment {
    private final static int SPELL_COOLDOWN = 3;
    private final static int SPELL_COST = 5;
    private final static int MAX_LEVEL = 5;
    //private final static int PROJECTILE_SPEED = 1;
    public static final String ID = VanillaArcana.MOD_ID + ":aegis";

    public AegisEnchantment() {
        super(Rarity.UNCOMMON, SPELL_COOLDOWN, SPELL_COST);
    }

    @Override
    public int getMaxLevel(){
        return MAX_LEVEL;
    }

    @Override
    public boolean handleCast(Level world, LivingEntity user, ItemStack stack){
        if (super.handleCast(world, user, stack)){
            BlockPos userBlockpos = user.blockPosition();
           int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
            Vec3 look = user.getLookAngle();
           // Vec3 pos = user.getEyePosition().add(look.scale(0.9));

           //world.setBlock(pPos, pState, pFlags, pRecursionLeft)

            //create shield blockentities
        
            if (user instanceof Player) {
                ((Player) user).getCooldowns().addCooldown(stack.getItem(), SPELL_COOLDOWN * spellLevel * spellLevel);
            }
            return true;
        } else return false;
    }
}
