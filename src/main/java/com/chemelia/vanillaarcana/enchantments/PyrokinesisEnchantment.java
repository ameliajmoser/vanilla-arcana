package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.ForgeConfig.Server;

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

    @Override
    public int getMaxLevel(){
        return 5;
    }

    @Override
    public void doPostAttack(LivingEntity attacker, Entity target, int enchantmentLevel){
        //if on a server:
        if (!attacker.level.isClientSide()){
            ServerLevel world = (ServerLevel) attacker.level;
            ServerPlayer player = ((ServerPlayer) attacker);
            BlockPos position = target.blockPosition();



            switch(enchantmentLevel){
                case 1:
                    EntityType.SMALL_FIREBALL.spawn(world, null, player, position, 
                        MobSpawnType.TRIGGERED, true, true);
                    break;
                case 2:
                    EntityType.FIREBALL.spawn(world, null, player, position,
                        MobSpawnType.TRIGGERED, true, true);
                    break;
                default:
                    break;
            }
        }
    }

    
    public PyrokinesisEnchantment() {
        super(Rarity.UNCOMMON, RegistryHandler.WAND_CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
        
    }

    
    // private static boolean canEnchantItem(Item item){
        
    // }
    
}
