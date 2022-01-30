package com.chemelia.vanillaarcana.entity.goal;

import java.util.EnumSet;

import com.chemelia.vanillaarcana.interfaces.SummonedEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

public class FollowSummonerGoal extends Goal {
   public static final int TELEPORT_WHEN_DISTANCE_IS = 20;
   // private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
   // private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
   // private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
   private final Mob monster;
   private final SummonedEntity ownableMonster;
   private final LevelReader level;
   private LivingEntity owner;
   private final double speedModifier;
   private final PathNavigation navigation;
   private int timeToRecalcPath;
   private final float stopDistance;
   private final float startDistance;
   private float oldWaterCost;
   private final boolean canFly;
   
   public FollowSummonerGoal(Mob monster, double speedModifier, float startDistance, float stopDistance, boolean canFly){
       this.monster = monster;
       this.ownableMonster = ((SummonedEntity) monster);
       this.level = monster.getLevel();
       this.speedModifier = speedModifier;
       this.navigation = monster.getNavigation();
       this.startDistance = startDistance;
       this.stopDistance = stopDistance;
       this.canFly = canFly;
       

       this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      if (!(monster.getNavigation() instanceof GroundPathNavigation) && !(monster.getNavigation() instanceof FlyingPathNavigation)) {
         throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
      }

   }
    /**
    * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
    * method as well.
    */
    public boolean canUse() {
      LivingEntity owner = (LivingEntity) this.ownableMonster.getSummoner();
        if (owner == null) {
           return false;
        } else if (owner.isSpectator()) {
           return false;
        } else if (this.monster.distanceToSqr(owner) < (double)(this.startDistance * this.startDistance)) {
           return false;
        } else {
           this.owner = owner;
           return true;
        }
    }

    /**
    * Returns whether an in-progress EntityAIBase should continue executing
    */
   public boolean canContinueToUse() {
    if (this.navigation.isDone()) {
       return false;
    } else {
       return !(this.monster.distanceToSqr(this.owner) <= (double)(this.stopDistance * this.stopDistance));
    }
 }

   /**
    * Execute a one shot task or start executing a continuous task
    */
    public void start() {
        this.timeToRecalcPath = 0;
        this.oldWaterCost = this.monster.getPathfindingMalus(BlockPathTypes.WATER);
        this.monster.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
     }
  
     /**
      * Reset the task's internal state. Called when this task is interrupted by another one
      */
     public void stop() {
        this.owner = null;
        this.navigation.stop();
        this.monster.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
     }
  
     /**
      * Keep ticking a continuous task that has already been started
      */
     public void tick() {
        this.monster.getLookControl().setLookAt(this.owner, 10.0F, (float)this.monster.getMaxHeadXRot());
        if (--this.timeToRecalcPath <= 0) {
           this.timeToRecalcPath = this.adjustedTickDelay(10);
           if (!this.monster.isLeashed() && !this.monster.isPassenger()) {
              if (this.monster.distanceToSqr(this.owner) >= TELEPORT_WHEN_DISTANCE_IS*TELEPORT_WHEN_DISTANCE_IS) {
                 this.teleportToOwner();
              } else {
                 this.navigation.moveTo(this.owner, this.speedModifier);
              }
  
           }
        }
     }
  
     private void teleportToOwner() {
        BlockPos blockpos = this.owner.blockPosition();
  
        for(int i = 0; i < 10; ++i) {
           int j = this.randomIntInclusive(-3, 3);
           int k = this.randomIntInclusive(-1, 1);
           int l = this.randomIntInclusive(-3, 3);
           boolean flag = this.maybeTeleportTo(blockpos.getX() + j, blockpos.getY() + k, blockpos.getZ() + l);
           if (flag) {
              return;
           }
        }
  
     }
  
     private boolean maybeTeleportTo(int pX, int pY, int pZ) {
        if (Math.abs((double)pX - this.owner.getX()) < 2.0D && Math.abs((double)pZ - this.owner.getZ()) < 2.0D) {
           return false;
        } else if (!this.canTeleportTo(new BlockPos(pX, pY, pZ))) {
           return false;
        } else {
           this.monster.moveTo((double)pX + 0.5D, (double)pY, (double)pZ + 0.5D, this.monster.getYRot(), this.monster.getXRot());
           this.navigation.stop();
           return true;
        }
     }
  
     private boolean canTeleportTo(BlockPos pPos) {
        BlockPathTypes blockpathtypes = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, pPos.mutable());
        if (blockpathtypes != BlockPathTypes.WALKABLE) {
           return false;
        } else {
           BlockState blockstate = this.level.getBlockState(pPos.below());
           if (!this.canFly && blockstate.getBlock() instanceof LeavesBlock) {
              return false;
           } else {
              BlockPos blockpos = pPos.subtract(this.monster.blockPosition());
              return this.level.noCollision(this.monster, this.monster.getBoundingBox().move(blockpos));
           }
        }
     }
  
     private int randomIntInclusive(int pMin, int pMax) {
        return this.monster.getRandom().nextInt(pMax - pMin + 1) + pMin;
     }
    
}
