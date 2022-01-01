package com.chemelia.vanillaarcana.block;

import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;


public class AegisBlock extends CrossCollisionBlock {

    public int lifetime = 100;

    public AegisBlock(BlockBehaviour.Properties properties) {
        super(1.0F, 1.0F, 16.0F, 16.0F, 16.0F, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, Boolean.valueOf(false)).setValue(EAST, Boolean.valueOf(false)).setValue(SOUTH, Boolean.valueOf(false)).setValue(WEST, Boolean.valueOf(false)).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    public void tick(){
        //every tick, subtract from the lifetime. when it reaches zero, break this block
    }

    




    

}