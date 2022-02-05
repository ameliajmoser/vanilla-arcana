package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

//////////////////
//  WARD/AEGIS  //
//////////////////
//I:   Spend XP to put up a shield made of fragile blocks (ie tinted glass) that decays after a short time.

public class AegisEnchantment extends SpellEnchantment {
    private final static int SPELL_COOLDOWN = 3;
    private final static int SPELL_COST = 5;
    private final static int MAX_LEVEL = 3;
    //private final static int PROJECTILE_SPEED = 1;
    private final static int RANGE = 5;
    private final Block BLOCK = RegistryHandler.AEGIS_BLOCK.get();
    private final BlockState B_STATE = BLOCK.defaultBlockState();
    public static final String ID = VanillaArcana.MOD_ID + ":aegis";

    public AegisEnchantment() {
        super(Rarity.COMMON, SPELL_COOLDOWN, SPELL_COST);
    }

    @Override
    public int getMaxLevel(){
        return MAX_LEVEL;
    }

    @Override
    public boolean handleCast(Level world, LivingEntity user, ItemStack stack){
        if (super.handleCast(world, user, stack)){
            BlockPos userBlockpos = user.blockPosition().above();
            Vec3 pos = user.getEyePosition();
            Vec3 look = user.getLookAngle();
            
            int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);

            Vec3 destination = pos.add(look.scale((double) RANGE));
            BlockHitResult cast = user.level.clip(new ClipContext(pos, destination, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, user));
            Direction dir = user.getDirection();

            BlockPos center = userBlockpos;
            if (cast.getType() != HitResult.Type.MISS){
                center = cast.getBlockPos().above(2);
            } else {
                
                int distance = 3;
                if (user.isCrouching()){
                    distance -=2;
                }
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
            }
            if (dir == Direction.EAST || dir == Direction.WEST){  
                placeShieldPane(center.north(), world, spellLevel);
                placeShieldPane(center.below().north(), world, spellLevel);
                placeShieldPane(center.south(), world, spellLevel);
                placeShieldPane(center.below().south(), world, spellLevel);
                if (spellLevel > 1){
                    placeShieldPane(center.above().north(), world, spellLevel);
                    placeShieldPane(center.above().south(), world, spellLevel);
                }
            } else if (dir == Direction.NORTH || dir == Direction.SOUTH){
                placeShieldPane(center.west(), world, spellLevel);
                placeShieldPane(center.below().west(), world, spellLevel);
                placeShieldPane(center.east(), world, spellLevel);
                placeShieldPane(center.below().east(), world, spellLevel);
                if (spellLevel > 1){
                    placeShieldPane(center.above().west(), world, spellLevel);
                    placeShieldPane(center.above().east(), world, spellLevel);
                }                             
            }
            placeShieldPane(center, world, spellLevel);
            placeShieldPane(center.below(), world, spellLevel);
            if (spellLevel > 1){
                placeShieldPane(center.above(), world, spellLevel);
            }
            
            return true;
        } else return false;
    }

    private boolean placeShieldPane(BlockPos pos, Level world, int spellLevel){
        if (world.getBlockState(pos).getCollisionShape(world, pos).isEmpty()){
            world.destroyBlock(pos, true);
            world.setBlockAndUpdate(pos, B_STATE);
            world.scheduleTick(pos, BLOCK, spellLevel*40);
            return true;
        } else return false;
    }
}
