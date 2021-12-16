package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
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

//PSEUDOCODE
/*
getSpellXPCost(enchantmentLevel){
    return enchanmentLevel * 3;
}

@Override
public void onSwingItem(){
    if (player.xp > getSpellXPCost){
        player.xp.decrease(getSpellXPCost);
        switch (enchantmentLevel){
            case 1:
                EntityType.SMALL_FIREBALL.spawn(location, velocity);
                break;
            case 2:
                spawnGhastFireball(location, velocity);
                break;
            case 3:
                placeBlock(LAVA);
                break;
            case 4: 
                spawnFriendlyBlaze();
            case 5:

            default:
                break;
        }
    } else {
        spawnSmokeParticles();
    }
}
*/


public class PyrokinesisEnchantment extends Enchantment {
    public static final String ID = VanillaArcana.MOD_ID + ":pyrokinesis";
    public static final int spellCost = 14;

    @Override
    public int getMaxLevel(){
        return 4;
    }

    public boolean handleCast(Level world, Player player, ItemStack stack){
        if (world.isClientSide()){
            return false;
        }
        if (player.totalExperience < PyrokinesisEnchantment.spellCost){
            return false;
        }

        int level = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
        player.giveExperiencePoints(-PyrokinesisEnchantment.spellCost * level);
        
        Vec3 look = player.getLookAngle();
        Vec3 pos = player.getEyePosition().add(look.scale(0.9));
        Vec3 velocity = look.scale(0.3);

        switch (level){
            case 0:
                return false;
            case 1:
                SmallFireball blazeFireball = new SmallFireball(world, player, 0,0,0);
                blazeFireball.setPos(pos.x, pos.y, pos.z);
                blazeFireball.xPower = velocity.x;
                blazeFireball.yPower = velocity.y;
                blazeFireball.zPower = velocity.z;
                world.addFreshEntity(blazeFireball);
                player.getCooldowns().addCooldown(stack.getItem(), 20);
                break;
            case 2:
                velocity = look.scale(0.2);
                //last parameter is explosionpower
                LargeFireball ghastFireball = new LargeFireball(world, player, 0,0,0, 2);
                ghastFireball.setPos(pos.x, pos.y, pos.z);
                ghastFireball.xPower = velocity.x;
                ghastFireball.yPower = velocity.y;
                ghastFireball.zPower = velocity.z;
                world.addFreshEntity(ghastFireball);
                player.getCooldowns().addCooldown(stack.getItem(), 40);
                break;
            case 3:
                velocity = look.scale(0.2);
                //last parameter is explosionpower
                LargeFireball bigFireball = new LargeFireball(world, player, 0,0,0, 6);
                bigFireball.setPos(pos.x, pos.y, pos.z);
                bigFireball.xPower = velocity.x;
                bigFireball.yPower = velocity.y;
                bigFireball.zPower = velocity.z;
                world.addFreshEntity(bigFireball);
                player.getCooldowns().addCooldown(stack.getItem(), 40);
                break;
            default:
                break;
        }
        return true;
    }


    
    public PyrokinesisEnchantment() {
        super(Rarity.UNCOMMON, RegistryHandler.WAND_CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
        
    }

    
    // private static boolean canEnchantItem(Item item){
        
    // }
    
}
