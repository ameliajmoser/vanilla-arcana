package com.chemelia.vanillaarcana.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.entity.goal.FollowSummonerGoal;
import com.chemelia.vanillaarcana.entity.goal.SummonerHurtByTargetGoal;
import com.chemelia.vanillaarcana.entity.goal.SummonerHurtTargetGoal;
import com.chemelia.vanillaarcana.interfaces.SummonedEntity;

import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.scores.Team;

public class TamedVex extends Vex implements SummonedEntity {

   public TamedVex(EntityType<? extends TamedVex> type, Level world) {
      super(type, world);
   }
   public TamedVex(Level world) {
      this(RegistryHandler.TAMED_VEX.get(), world);
   }
   public TamedVex(Level world, Player player) {
      this(world);
      tame(player);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new FloatGoal(this));
      this.goalSelector.addGoal(2, new TamedVex.VexChargeAttackGoal());
      this.goalSelector.addGoal(3, new FollowSummonerGoal(this, 1.0D, 12.0F, 1.0F, true));
      this.goalSelector.addGoal(4, new TamedVex.VexRandomMoveGoal());
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
      this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
      this.targetSelector.addGoal(1, new SummonerHurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new SummonerHurtTargetGoal(this));
      this.targetSelector.addGoal(3, (new HurtByTargetGoal(this)).setAlertOthers());
   }

        ///This makes the necromancy mobs wither in sunlight
        @Override
        public void aiStep(){
           boolean flag = this.isSunBurnTick();
           if (flag) {
              net.minecraft.world.item.ItemStack itemstack = this.getItemBySlot(EquipmentSlot.HEAD);
              if (!itemstack.isEmpty()) {
                 if (itemstack.isDamageableItem()) {
                    itemstack.setDamageValue(itemstack.getDamageValue() + this.random.nextInt(2));
                    if (itemstack.getDamageValue() >= itemstack.getMaxDamage()) {
                       this.broadcastBreakEvent(EquipmentSlot.HEAD);
                       this.setItemSlot(EquipmentSlot.HEAD, net.minecraft.world.item.ItemStack.EMPTY);
                    }
                 }
                 flag = false;
              }
              if (flag) {
                 this.addEffect(new MobEffectInstance(MobEffects.WITHER, 20, 1, true, false));
              }
           }
           super.aiStep();
        }
   

   class VexChargeAttackGoal extends Goal {
      public VexChargeAttackGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state
       * necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         if (TamedVex.this.getTarget() != null && !TamedVex.this.getMoveControl().hasWanted()
               && TamedVex.this.random.nextInt(reducedTickDelay(7)) == 0) {
            return TamedVex.this.distanceToSqr(TamedVex.this.getTarget()) > 4.0D;
         } else {
            return false;
         }
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return TamedVex.this.getMoveControl().hasWanted() && TamedVex.this.isCharging()
               && TamedVex.this.getTarget() != null && TamedVex.this.getTarget().isAlive();
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         LivingEntity livingentity = TamedVex.this.getTarget();
         if (livingentity != null) {
            Vec3 vec3 = livingentity.getEyePosition();
            TamedVex.this.moveControl.setWantedPosition(vec3.x, vec3.y, vec3.z, 1.0D);
         }

         TamedVex.this.setIsCharging(true);
         TamedVex.this.playSound(SoundEvents.VEX_CHARGE, 1.0F, 1.0F);
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by
       * another one
       */
      public void stop() {
         TamedVex.this.setIsCharging(false);
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         LivingEntity livingentity = TamedVex.this.getTarget();
         if (livingentity != null) {
            if (TamedVex.this.getBoundingBox().intersects(livingentity.getBoundingBox())) {
               TamedVex.this.doHurtTarget(livingentity);
               TamedVex.this.setIsCharging(false);
            } else {
               double d0 = TamedVex.this.distanceToSqr(livingentity);
               if (d0 < 9.0D) {
                  Vec3 vec3 = livingentity.getEyePosition();
                  TamedVex.this.moveControl.setWantedPosition(vec3.x, vec3.y, vec3.z, 1.0D);
               }
            }

         }
      }
   }

   class VexMoveControl extends MoveControl {
      public VexMoveControl(Vex p_34062_) {
         super(p_34062_);
      }

      public void tick() {
         if (this.operation == MoveControl.Operation.MOVE_TO) {
            Vec3 vec3 = new Vec3(this.wantedX - TamedVex.this.getX(), this.wantedY - TamedVex.this.getY(),
                  this.wantedZ - TamedVex.this.getZ());
            double d0 = vec3.length();
            if (d0 < TamedVex.this.getBoundingBox().getSize()) {
               this.operation = MoveControl.Operation.WAIT;
               TamedVex.this.setDeltaMovement(TamedVex.this.getDeltaMovement().scale(0.5D));
            } else {
               TamedVex.this.setDeltaMovement(
                     TamedVex.this.getDeltaMovement().add(vec3.scale(this.speedModifier * 0.05D / d0)));
               if (TamedVex.this.getTarget() == null) {
                  Vec3 vec31 = TamedVex.this.getDeltaMovement();
                  TamedVex.this.setYRot(-((float) Mth.atan2(vec31.x, vec31.z)) * (180F / (float) Math.PI));
                  TamedVex.this.yBodyRot = TamedVex.this.getYRot();
               } else {
                  double d2 = TamedVex.this.getTarget().getX() - TamedVex.this.getX();
                  double d1 = TamedVex.this.getTarget().getZ() - TamedVex.this.getZ();
                  TamedVex.this.setYRot(-((float) Mth.atan2(d2, d1)) * (180F / (float) Math.PI));
                  TamedVex.this.yBodyRot = TamedVex.this.getYRot();
               }
            }

         }
      }
   }

   class VexRandomMoveGoal extends Goal {
      public VexRandomMoveGoal() {
         this.setFlags(EnumSet.of(Goal.Flag.MOVE));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state
       * necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         return !TamedVex.this.getMoveControl().hasWanted() && TamedVex.this.random.nextInt(reducedTickDelay(7)) == 0;
      }

      /**
       * Returns whether an in-progress EntityAIBase should continue executing
       */
      public boolean canContinueToUse() {
         return false;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         BlockPos blockpos = TamedVex.this.getBoundOrigin();
         if (blockpos == null) {
            blockpos = TamedVex.this.blockPosition();
         }

         for (int i = 0; i < 3; ++i) {
            BlockPos blockpos1 = blockpos.offset(TamedVex.this.random.nextInt(15) - 7,
                  TamedVex.this.random.nextInt(11) - 5, TamedVex.this.random.nextInt(15) - 7);
            if (TamedVex.this.level.isEmptyBlock(blockpos1)) {
               TamedVex.this.moveControl.setWantedPosition((double) blockpos1.getX() + 0.5D,
                     (double) blockpos1.getY() + 0.5D, (double) blockpos1.getZ() + 0.5D, 0.25D);
               if (TamedVex.this.getTarget() == null) {
                  TamedVex.this.getLookControl().setLookAt((double) blockpos1.getX() + 0.5D,
                        (double) blockpos1.getY() + 0.5D, (double) blockpos1.getZ() + 0.5D, 180.0F, 20.0F);
               }
               break;
            }
         }
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
   protected boolean shouldDropLoot() {
      return false;
   }

   protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TamedBlaze.class,
         EntityDataSerializers.BYTE);
   protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData
         .defineId(TamedBlaze.class, EntityDataSerializers.OPTIONAL_UUID);

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
