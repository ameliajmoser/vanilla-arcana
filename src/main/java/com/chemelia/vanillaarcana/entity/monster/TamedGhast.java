package com.chemelia.vanillaarcana.entity.monster;

import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Nullable;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.entity.goal.FollowSummonerGoal;
import com.chemelia.vanillaarcana.entity.goal.SummonerHurtByTargetGoal;
import com.chemelia.vanillaarcana.entity.goal.SummonerHurtTargetGoal;
import com.chemelia.vanillaarcana.interfaces.SummonedEntity;

import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
// import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.scores.Team;

public class TamedGhast extends Ghast implements SummonedEntity {

   public TamedGhast(Level world) {
      super(RegistryHandler.TAMED_GHAST.get(), world);
   }

   public TamedGhast(EntityType<? extends TamedGhast> type, Level world) {
      super(type, world);
   }

   public TamedGhast(EntityType<? extends TamedGhast> type, Level world, Player player) {
      this(type, world);
      tame(player);
   }

   public TamedGhast(Level world, Player player) {
      this(world);
      tame(player);
   }

   @Override
      protected void registerGoals() {
      this.goalSelector.addGoal(2, new FollowSummonerGoal(this, 1.0D, 12.0F, 1.0F, true));
      this.goalSelector.addGoal(5, new TamedGhast.RandomFloatAroundGoal(this));
      this.goalSelector.addGoal(7, new TamedGhast.GhastLookGoal(this));
      this.goalSelector.addGoal(7, new TamedGhast.GhastShootFireballGoal(this));
      this.targetSelector.addGoal(1, new SummonerHurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new SummonerHurtTargetGoal(this));
      //this.targetSelector.addGoal(3, ( new HurtByTargetGoal(this) ).setAlertOthers());
   }

   //////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////
   /////////////////  GHAST STUFF                   
   //////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////

   static class GhastLookGoal extends Goal {
      private final Ghast ghast;

      public GhastLookGoal(Ghast p_32762_) {
         this.ghast = p_32762_;
         this.setFlags(EnumSet.of(Goal.Flag.LOOK));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return true;
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         if (this.ghast.getTarget() == null) {
            Vec3 vec3 = this.ghast.getDeltaMovement();
            this.ghast.setYRot(-((float)Mth.atan2(vec3.x, vec3.z)) * (180F / (float)Math.PI));
            this.ghast.yBodyRot = this.ghast.getYRot();
         } else {
            LivingEntity livingentity = this.ghast.getTarget();
            double d0 = 64.0D;
            if (livingentity.distanceToSqr(this.ghast) < 4096.0D) {
               double d1 = livingentity.getX() - this.ghast.getX();
               double d2 = livingentity.getZ() - this.ghast.getZ();
               this.ghast.setYRot(-((float)Mth.atan2(d1, d2)) * (180F / (float)Math.PI));
               this.ghast.yBodyRot = this.ghast.getYRot();
            }
         }

      }
   }

   static class GhastMoveControl extends MoveControl {
      private final Ghast ghast;
      private int floatDuration;

      public GhastMoveControl(Ghast p_32768_) {
         super(p_32768_);
         this.ghast = p_32768_;
      }

      public void tick() {
         if (this.operation == MoveControl.Operation.MOVE_TO) {
            if (this.floatDuration-- <= 0) {
               this.floatDuration += this.ghast.getRandom().nextInt(5) + 2;
               Vec3 vec3 = new Vec3(this.wantedX - this.ghast.getX(), this.wantedY - this.ghast.getY(), this.wantedZ - this.ghast.getZ());
               double d0 = vec3.length();
               vec3 = vec3.normalize();
               if (this.canReach(vec3, Mth.ceil(d0))) {
                  this.ghast.setDeltaMovement(this.ghast.getDeltaMovement().add(vec3.scale(0.1D)));
               } else {
                  this.operation = MoveControl.Operation.WAIT;
               }
            }

         }
      }

      private boolean canReach(Vec3 p_32771_, int p_32772_) {
         AABB aabb = this.ghast.getBoundingBox();

         for(int i = 1; i < p_32772_; ++i) {
            aabb = aabb.move(p_32771_);
            if (!this.ghast.level.noCollision(this.ghast, aabb)) {
               return false;
            }
         }
         return true;
      }
   }

   static class GhastShootFireballGoal extends Goal {
      private final Ghast ghast;
      public int chargeTime;

      public GhastShootFireballGoal(Ghast p_32776_) {
         this.ghast = p_32776_;
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return this.ghast.getTarget() != null;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.chargeTime = 0;
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by another one
       */
      public void stop() {
         this.ghast.setCharging(false);
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         LivingEntity livingentity = this.ghast.getTarget();
         if (livingentity != null) {
            double d0 = 64.0D;
            if (livingentity.distanceToSqr(this.ghast) < 4096.0D && this.ghast.hasLineOfSight(livingentity)) {
               Level level = this.ghast.level;
               ++this.chargeTime;
               if (this.chargeTime == 10 && !this.ghast.isSilent()) {
                  level.levelEvent((Player)null, 1015, this.ghast.blockPosition(), 0);
               }

               if (this.chargeTime == 20) {
                  double d1 = 4.0D;
                  Vec3 vec3 = this.ghast.getViewVector(1.0F);
                  double d2 = livingentity.getX() - (this.ghast.getX() + vec3.x * 4.0D);
                  double d3 = livingentity.getY(0.5D) - (0.5D + this.ghast.getY(0.5D));
                  double d4 = livingentity.getZ() - (this.ghast.getZ() + vec3.z * 4.0D);
                  if (!this.ghast.isSilent()) {
                     level.levelEvent((Player)null, 1016, this.ghast.blockPosition(), 0);
                  }

                  LargeFireball largefireball = new LargeFireball(level, this.ghast, d2, d3, d4, this.ghast.getExplosionPower());
                  largefireball.setPos(this.ghast.getX() + vec3.x * 4.0D, this.ghast.getY(0.5D) + 0.5D, largefireball.getZ() + vec3.z * 4.0D);
                  level.addFreshEntity(largefireball);
                  this.chargeTime = -40;
               }
            } else if (this.chargeTime > 0) {
               --this.chargeTime;
            }

            this.ghast.setCharging(this.chargeTime > 10);
         }
      }
   }

   static class RandomFloatAroundGoal extends Goal {
      private final Ghast ghast;

      public RandomFloatAroundGoal(Ghast p_32783_) {
         this.ghast = p_32783_;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         MoveControl movecontrol = this.ghast.getMoveControl();
         if (!movecontrol.hasWanted()) {
            return true;
         } else {
            double d0 = movecontrol.getWantedX() - this.ghast.getX();
            double d1 = movecontrol.getWantedY() - this.ghast.getY();
            double d2 = movecontrol.getWantedZ() - this.ghast.getZ();
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            return d3 < 1.0D || d3 > 3600.0D;
         }
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return false;
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         Random random = this.ghast.getRandom();
         double d0 = this.ghast.getX() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
         double d1 = this.ghast.getY() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
         double d2 = this.ghast.getZ() + (double)((random.nextFloat() * 2.0F - 1.0F) * 16.0F);
         this.ghast.getMoveControl().setWantedPosition(d0, d1, d2, 1.0D);
      }
   }

   //////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////                                                   
   //////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////
   /// This should all be generic stuff for any tamed monster.///                                                     
   //////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////
   //////////////////////////////////////////////////////////////

   @Override
   protected boolean shouldDropLoot(){
      return false;
   }
   
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TamedGhast.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(TamedGhast.class, EntityDataSerializers.OPTIONAL_UUID);

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
     }
    
     public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (this.getOwnerUUID() != null) {
           pCompound.putUUID("Owner", this.getOwnerUUID());
        }
     }
   /**
    * (abstract) Protected helper method to read subclass entity data from NBT.
    */
   public void readAdditionalSaveData(CompoundTag pCompound) {
      super.readAdditionalSaveData(pCompound);
      UUID uuid;
      if (pCompound.hasUUID("Owner")) {
         uuid = pCompound.getUUID("Owner");
      } else {
         String s = pCompound.getString("Owner");
         uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.getServer(), s);
      }
      if (uuid != null) {
         try {
            this.setOwnerUUID(uuid);
            this.setTame(true);
         } catch (Throwable throwable) {
            this.setTame(false);
         }
      }
   }

   public boolean canBeLeashed(Player pPlayer) {
      return !this.isLeashed();
  }

  public boolean isTame() {
      return (this.entityData.get(DATA_FLAGS_ID) & 4) != 0;
   }

   public void setTame(boolean pTamed) {
      byte b0 = this.entityData.get(DATA_FLAGS_ID);
      if (pTamed) {
         this.entityData.set(DATA_FLAGS_ID, (byte)(b0 | 4));
      } else {
         this.entityData.set(DATA_FLAGS_ID, (byte)(b0 & -5));
      }

      this.reassessTameGoals();
   }

   protected void reassessTameGoals() {
   }

   
 @Nullable
 public UUID getOwnerUUID() {
    return this.entityData.get(DATA_OWNERUUID_ID).orElse((UUID)null);
 }

 public void setOwnerUUID(@Nullable UUID p_21817_) {
    this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(p_21817_));
 }

 public void tame(Player pPlayer) {
    this.setTame(true);
    this.setOwnerUUID(pPlayer.getUUID());
    if (pPlayer instanceof ServerPlayer) {
       CriteriaTriggers.SUMMONED_ENTITY.trigger((ServerPlayer)pPlayer, this);
    }
 }

 @Nullable
 public LivingEntity getSummoner() {
    try {
       UUID uuid = this.getOwnerUUID();
       return uuid == null ? null : this.level.getPlayerByUUID(uuid);
    } catch (IllegalArgumentException illegalargumentexception) {
       return null;
    }
 }

 public boolean canAttack(LivingEntity pTarget) {
    return this.isOwnedBy(pTarget) ? false : super.canAttack(pTarget);
 }

 public boolean isOwnedBy(LivingEntity pEntity) {
    return pEntity == this.getSummoner();
 }

 public boolean wantsToAttack(LivingEntity pTarget, LivingEntity pOwner) {
    return true;
 }

 public Team getTeam() {
    if (this.isTame()) {
       LivingEntity livingentity = this.getSummoner();
       if (livingentity != null) {
          return livingentity.getTeam();
       }
    }

    return super.getTeam();
 }

 /**
  * Returns whether this Entity is on the same team as the given Entity.
  */
 public boolean isAlliedTo(Entity pEntity) {
    if (this.isTame()) {
       LivingEntity livingentity = this.getSummoner();
       if (pEntity == livingentity) {
          return true;
       }

       if (livingentity != null) {
          return livingentity.isAlliedTo(pEntity);
       }
    }

    return super.isAlliedTo(pEntity);
 }

 /**
  * Called when the mob's health reaches 0.
  */
 public void die(DamageSource pCause) {
    // FORGE: Super moved to top so that death message would be cancelled properly
    net.minecraft.network.chat.Component deathMessage = this.getCombatTracker().getDeathMessage();
    super.die(pCause);

    if (this.dead)
    if (!this.level.isClientSide && this.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getSummoner() instanceof ServerPlayer) {
       this.getSummoner().sendMessage(deathMessage, Util.NIL_UUID);
    }
 }
 
   //END generic tamed monster stuff  
}
