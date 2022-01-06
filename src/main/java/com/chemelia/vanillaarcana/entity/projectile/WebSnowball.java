package com.chemelia.vanillaarcana.entity.projectile;

import com.chemelia.vanillaarcana.enchantments.WebEnchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class WebSnowball extends Snowball {
    public static final int SPELL_COST = WebEnchantment.spellCost;
    private LivingEntity owner;
    private Level world;
    private int spellLevel;

    public WebSnowball(EntityType<? extends Snowball> type, Level world){
        super(type, world);
    }

    public WebSnowball(Level world, LivingEntity owner, int spellLevel) {
        super(world, owner);
        this.owner = owner;
        this.world = world;
        this.spellLevel = spellLevel;
    }

    public void onHitEntity(EntityHitResult result){
        BlockPos pos = new BlockPos(result.getLocation());
        generateWebs(Direction.UP, pos);
        super.onHitEntity(result);
    }

    public void onHitBlock(BlockHitResult result){
        BlockPos pos = new BlockPos(result.getLocation());
        generateWebs(result.getDirection(), pos);
        super.onHitBlock(result);
    }

    private void generateWebs(Direction direction, BlockPos center){
        if (!world.isEmptyBlock(center)){
            center.relative(direction, -1);
        }
        switch (spellLevel) {
            case 3:
            if(direction.getAxis().isVertical()){
                placeWeb(world, center.south().west());
                placeWeb(world, center.south().east());
                placeWeb(world, center.north().west());
                placeWeb(world, center.north().east());
            } else if (direction == Direction.NORTH || direction == Direction.SOUTH){
                placeWeb(world, center.above().west());
                placeWeb(world, center.above().east());
                placeWeb(world, center.below().west());
                placeWeb(world, center.below().east());
            } else if (direction == Direction.EAST || direction == Direction.WEST) {
                placeWeb(world, center.above().north());
                placeWeb(world, center.above().south());
                placeWeb(world, center.below().north());
                placeWeb(world, center.below().south());
            }
            case 2:
            if(direction.getAxis().isVertical()){
                placeWeb(world, center.north());
                placeWeb(world, center.south());
                placeWeb(world, center.east());
                placeWeb(world, center.west());
            } else if (direction == Direction.NORTH || direction == Direction.SOUTH){
                placeWeb(world, center.above());
                placeWeb(world, center.below());
                placeWeb(world, center.east());
                placeWeb(world, center.west());
            } else if (direction == Direction.EAST || direction == Direction.WEST){
                placeWeb(world, center.above());
                placeWeb(world, center.below());
                placeWeb(world, center.north());
                placeWeb(world, center.south());
            }
            case 1:
                placeWeb(world, center);
                break;
            default:
                break;
        }
    }
    private boolean placeWeb(Level world, BlockPos pos){
        if (world.isEmptyBlock(pos)){
            world.setBlockAndUpdate(pos, Blocks.COBWEB.defaultBlockState());
            return true;
        } else return false;
    }
}
