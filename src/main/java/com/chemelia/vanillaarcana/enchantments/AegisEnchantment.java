package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

//////////////////
//  WARD/AEGIS  //
//////////////////
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
            BlockPos userBlockpos = user.blockPosition().above();
            int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
            Direction dir = user.getDirection();
            int distance = 3;
            if (user.isCrouching()){
                distance -=2;
            }
            BlockPos center = userBlockpos;
            if (user.getXRot() < -8){
                center = center.above();
            }
            if (user.getXRot() < -20){
                center = center.above();
                distance -=1;
            }
            if (user.getXRot() < -40){
                distance = 1;
            }
            if (user.getXRot() < -80){
                distance = 0;
            }
            switch (dir){
                case DOWN:
                    break;
                case UP:
                    break;
                case NORTH:
                    center = center.north(distance);
                    break;
                case SOUTH:
                    center = center.south(distance);
                    break;
                case WEST:
                    center = center.west(distance);
                    break;
                case EAST:
                    center = center.east(distance);
                    break;
            }

            if (dir == Direction.EAST || dir == Direction.WEST){  
                placeShieldPane(center.north(), world);
                placeShieldPane(center.below().north(), world);
                placeShieldPane(center.south(), world);
                placeShieldPane(center.below().south(), world);
                if (spellLevel > 1){
                    placeShieldPane(center.above().north(), world);
                    placeShieldPane(center.above().south(), world);
                }
            } else if (dir == Direction.NORTH || dir == Direction.SOUTH){
                placeShieldPane(center.west(), world);
                placeShieldPane(center.below().west(), world);
                placeShieldPane(center.east(), world);
                placeShieldPane(center.below().east(), world);
                if (spellLevel > 1){
                    placeShieldPane(center.above().west(), world);
                    placeShieldPane(center.above().east(), world);
                }                             
            }
            placeShieldPane(center, world);
            placeShieldPane(center.below(), world);
            if (spellLevel > 1){
                placeShieldPane(center.above(), world);
            }
            
            if (user instanceof Player) {
                ((Player) user).getCooldowns().addCooldown(stack.getItem(), SPELL_COOLDOWN * spellLevel * spellLevel);
            }
            return true;
        } else return false;
    }

    private boolean placeShieldPane(BlockPos pos, Level world){
        if (world.isEmptyBlock(pos)){
            world.setBlockAndUpdate(pos, RegistryHandler.AEGIS_BLOCK.get().defaultBlockState());
            return true;
        } else return false;
    }
}
