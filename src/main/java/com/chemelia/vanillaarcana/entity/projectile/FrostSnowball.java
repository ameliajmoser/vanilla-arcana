package com.chemelia.vanillaarcana.entity.projectile;

import com.chemelia.vanillaarcana.enchantments.FrostEnchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

///////////
// FROST //
///////////
//I:   Snowball projectile that applies powdered snow frost effect.
//II:  Freezes water.
//III: Freezes water and turns ice blue.
//VI:  Freezes entities.
//VI:  Freezes entities in blue ice.

public class FrostSnowball extends Snowball {
    public static final int SPELL_COST = FrostEnchantment.spellCost;
    public static final Block ICE = Blocks.ICE;
    public static final BlockState ICE_STATE = ICE.defaultBlockState();
    private LivingEntity owner;
    private Level world;
    private int spellLevel;

    public FrostSnowball(EntityType<? extends Snowball> type, Level world){
        super(type, world);
    }

    public FrostSnowball(Level world, LivingEntity owner, int spellLevel) {
        super(world, owner);
        this.owner = owner;
        this.world = world;
        this.spellLevel = spellLevel;
    }


    public void onHitEntity(EntityHitResult result){
        Entity hitEntity = result.getEntity();
        hitEntity.hurt(DamageSource.FREEZE, (float)spellLevel);
        if (hitEntity instanceof Skeleton){
            Skeleton skelly = (Skeleton) hitEntity;
            skelly.setFreezeConverting(true);
        } else if (hitEntity instanceof Blaze){
            hitEntity.hurt(DamageSource.FREEZE, 2*spellLevel);
        }      
        hitEntity.setTicksFrozen(140 * spellLevel);
        owner.level.playSound(null, owner.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1, 1.0F);
        if (spellLevel > 2){
            BlockPos pos = hitEntity.blockPosition();
            world.setBlockAndUpdate(pos, ICE_STATE);
            if (hitEntity.getEyeHeight() > 1.2){
                world.setBlockAndUpdate(pos.above(), ICE_STATE);
            }
            hitEntity.moveTo(pos, 0, 0);
        }
    }

    public void tick(){
        super.tick();
        if (Math.random() > 0.7){
            emitSnowflake(1, 0.01);
        }
        if (world.getBlockState(this.blockPosition()).is(Blocks.WATER)){
            world.setBlockAndUpdate(this.blockPosition(), ICE_STATE);
            world.playSound(null, this.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1, 1.0F);
            emitSnowflake(15, 0.5);
            this.discard();
            switch (spellLevel){
                case 5:
                case 4:
                case 3:
                waterToIce(this.blockPosition().north(2));
                waterToIce(this.blockPosition().south(2));
                waterToIce(this.blockPosition().east(2));
                waterToIce(this.blockPosition().west(2));
                waterToIce(this.blockPosition().north().east());
                waterToIce(this.blockPosition().north().west());
                waterToIce(this.blockPosition().south().west());
                waterToIce(this.blockPosition().south().east());
                case 2:
                waterToIce(this.blockPosition().above());
                waterToIce(this.blockPosition().below());
                waterToIce(this.blockPosition().north());
                waterToIce(this.blockPosition().south());
                waterToIce(this.blockPosition().east());
                waterToIce(this.blockPosition().west());
                break;
            }
        }
    }

    public void onHitBlock(BlockHitResult result){
        if (world.getBlockState(result.getBlockPos()) == ICE_STATE && this.spellLevel > 2){
            world.setBlockAndUpdate(result.getBlockPos(), Blocks.BLUE_ICE.defaultBlockState());
            world.playSound(null, this.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1, 1.2F);
            emitSnowflake(5, 0.5);
        }
    }

    private void emitSnowflake(int num, double velocity){
        Vec3 pos = this.position();
        if (world instanceof ServerLevel) {
            ((ServerLevel) world).sendParticles(ParticleTypes.SNOWFLAKE, pos.x, pos.y, pos.z, num, 0, 0, 0, 0.05);
        }
    }

    private void waterToIce(BlockPos bPos){
        if (world.getBlockState(bPos).is(Blocks.WATER)){
            world.setBlockAndUpdate(bPos, ICE_STATE);
        }
    }
}
