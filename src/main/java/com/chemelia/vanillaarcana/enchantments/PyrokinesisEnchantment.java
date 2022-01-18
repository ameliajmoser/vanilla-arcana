package com.chemelia.vanillaarcana.enchantments;


import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Fireball;
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
    private final static int SPELL_COOLDOWN = 7;
    private final static int SPELL_COST = 14;
    private final static int MAX_LEVEL = 4;
    public static final String ID = VanillaArcana.MOD_ID + ":pyrokinesis";

    public PyrokinesisEnchantment() {
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
            Vec3 velocity = look.scale(0.3);
            Fireball fireball = new SmallFireball(world, user, 0,0,0);

            switch (spellLevel){
                case 0:
                    return false;
                case 1:
                    break;
                case 2:
                    velocity = look.scale(0.2);
                    //last parameter is explosionpower
                    fireball = new LargeFireball(world, user, 0,0,0, 1);
                    break;
                case 3:
                    velocity = look.scale(0.1);
                    //last parameter is explosionpower
                    fireball = new LargeFireball(world, user, 0,0,0, 3);
                    break;
                default:
                    break;  
                }
                fireball.setPos(pos.x, pos.y, pos.z);
                fireball.xPower = velocity.x;
                fireball.yPower = velocity.y;
                fireball.zPower = velocity.z; 
                world.addFreshEntity(fireball);

                if (user instanceof Player){
                    ((Player) user).getCooldowns().addCooldown(stack.getItem(), SPELL_COOLDOWN*spellLevel*spellLevel);
                }
            return true;
        } else return false;
    }

    //Increases the speed of any fireballs reflected - like lethal league for wizards!
    @Override
    public void doPostAttack(LivingEntity attacker, Entity target, int spellLevel){
        if (target instanceof Fireball){
            target.setDeltaMovement(target.getDeltaMovement().scale(1.2));
        }
    }
}
