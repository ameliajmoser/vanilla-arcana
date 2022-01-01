package com.chemelia.vanillaarcana.entity.projectile;

import com.chemelia.vanillaarcana.enchantments.FrostEnchantment;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;


public class FrostSnowball extends Snowball {
    public static final int SPELL_COST = FrostEnchantment.spellCost;
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
        super.onHitEntity(result);
        Entity hitEntity = result.getEntity();
        hitEntity.hurt(DamageSource.FREEZE, (float)spellLevel);
        if (hitEntity instanceof Skeleton){
            Skeleton skelly = (Skeleton) hitEntity;
            skelly.setFreezeConverting(true);
        }         
        hitEntity.setTicksFrozen(140 * spellLevel);
        owner.level.playSound(null, owner.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1, 1.0F);
    }

    public void tick(){
        super.tick();
        if (Math.random() > 0.7){
            emitSnowflake(1, 0.01);
        }
        if (world.getBlockState(this.blockPosition()).is(Blocks.WATER) && world.getBlockState(this.blockPosition().above()) == Blocks.AIR.defaultBlockState()){
            world.setBlockAndUpdate(this.blockPosition(), Blocks.ICE.defaultBlockState());
            world.playSound(null, this.blockPosition(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1, 1.0F);
            emitSnowflake(15, 0.5);
            this.discard();
        }
    }

    public void onHitBlock(BlockHitResult result){
        if (world.getBlockState(result.getBlockPos()) == Blocks.ICE.defaultBlockState()){
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
}
