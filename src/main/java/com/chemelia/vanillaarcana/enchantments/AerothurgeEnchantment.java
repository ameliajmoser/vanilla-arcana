package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
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
    private final static int SPELL_COST = 1;
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
        if (!attacker.level.isClientSide()){
            ServerLevel world = (ServerLevel) attacker.level;
        }
        Vec3 velocity = attacker.getLookAngle().scale(3);
        target.setDeltaMovement(velocity);
        //target.push(velocity.x, velocity.y, velocity.z);
    }

    @Override
    public boolean handleCast(Level world, LivingEntity user, ItemStack stack){
        if (super.handleCast(world, user, stack)){
            int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
            Vec3 look = user.getLookAngle();
            //Vec3 pos = user.getEyePosition().add(look.scale(0.9));
            Vec3 velocity = look.scale(5);

            user.setDeltaMovement(velocity);

            switch (spellLevel){
                case 0:
                    return false;
                case 1:
                    
                    break;
                case 2:

                    break;
                case 3:

                    break;
                default:
                    break;  
                }
            return true;
        } else return false;
    }

    @Override
    public boolean handleClientCast(Level world, LivingEntity user, ItemStack stack) {
        Vec3 look = user.getLookAngle();
        Vec3 velocity = look.scale(5);
        user.setDeltaMovement(velocity);

        return super.handleClientCast(world, user, stack);
    }
}