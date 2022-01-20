package com.chemelia.vanillaarcana.block;

import java.util.Random;

import com.chemelia.vanillaarcana.enchantments.AegisEnchantment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
 
public class AegisBlock extends StainedGlassPaneBlock {

    public static final int DEFAULT_LIFETIME = 60;
    private int lifetime;

    public AegisBlock(BlockBehaviour.Properties props, int lifetime){
        super(DyeColor.CYAN, props);
        this.lifetime = lifetime;
    }

    public AegisBlock(BlockBehaviour.Properties props){
        this(props, DEFAULT_LIFETIME);
    }

    public void setLifetime(int time){
        this.lifetime = time;
    }

    // public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
    //     //super.onPlace(pState, pLevel, pPos, pOldState, pIsMoving);
    //     pLevel.scheduleTick(pPos, this, lifetime);
    //  }
     
    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
        pLevel.destroyBlock(pPos, false);
        if (pLevel.getBlockState(pPos.above()) == this.defaultBlockState()){
            pLevel.destroyBlock(pPos.above(), false);
        }
        if (pLevel.getBlockState(pPos.below()) == this.defaultBlockState()){
            pLevel.destroyBlock(pPos.below(), false);
        }
        if (pLevel.getBlockState(pPos.north()) == this.defaultBlockState()){
            pLevel.destroyBlock(pPos.north(), false);
        }
        if (pLevel.getBlockState(pPos.south()) == this.defaultBlockState()){
            pLevel.destroyBlock(pPos.south(), false);
        }
        if (pLevel.getBlockState(pPos.east()) == this.defaultBlockState()){
            pLevel.destroyBlock(pPos.east(), false);
        }
        if (pLevel.getBlockState(pPos.west()) == this.defaultBlockState()){
            pLevel.destroyBlock(pPos.west(), false);
        }
    }

    @Override
    public int getExpDrop(BlockState state, net.minecraft.world.level.LevelReader reader, BlockPos pos, int fortune,
            int silktouch) {
        return silktouch == 0 ? AegisEnchantment.spellCost : 0;
    }
}