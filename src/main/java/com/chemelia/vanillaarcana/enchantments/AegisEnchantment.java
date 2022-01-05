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
            switch (dir){
                case DOWN:
                    break;
                case UP:
                    break;
                case NORTH:
                    center = userBlockpos.north(distance);
                    break;
                case SOUTH:
                    center = userBlockpos.south(distance);
                    break;
                case WEST:
                    center = userBlockpos.west(distance);
                    break;
                case EAST:
                    center = userBlockpos.east(distance);
                    break;
            }

            if (dir == Direction.EAST || dir == Direction.WEST){
                placeShieldPane(center.above().north(1), world);
                placeShieldPane(center.north(1), world);
                placeShieldPane(center.below().north(1), world);
                placeShieldPane(center.above().south(1), world);
                placeShieldPane(center.south(1), world);
                placeShieldPane(center.below().south(1), world);
            } else if (dir == Direction.NORTH || dir == Direction.SOUTH){
                placeShieldPane(center.above().west(1), world);
                placeShieldPane(center.west(1), world);
                placeShieldPane(center.below().west(1), world);
                placeShieldPane(center.above().east(1), world);
                placeShieldPane(center.east(1), world);
                placeShieldPane(center.below().east(1), world);
            }
            placeShieldPane(center, world);
            placeShieldPane(center.above(), world);
            placeShieldPane(center.below(), world);

            if (user instanceof Player) {
                ((Player) user).getCooldowns().addCooldown(stack.getItem(), SPELL_COOLDOWN * spellLevel * spellLevel);
            }
            return true;
        } else return false;
    }


    private boolean placeShieldPane(BlockPos pos, Level world){
        if (world.isEmptyBlock(pos)){
            world.setBlockAndUpdate(pos, RegistryHandler.AEGIS_BLOCK.get().defaultBlockState());
            //world.setBlockAndUpdate(pos, Blocks.CYAN_STAINED_GLASS_PANE.defaultBlockState());

            return true;
        } else return false;
    }
}
