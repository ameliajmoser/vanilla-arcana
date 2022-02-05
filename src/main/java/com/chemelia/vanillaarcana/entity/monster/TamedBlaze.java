package com.chemelia.vanillaarcana.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.level.Level;

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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.scores.Team;

public class TamedBlaze extends Blaze implements SummonedEntity {

   public TamedBlaze(EntityType<? extends Blaze> type, Level world) {
      super(type, world);
   }

   public TamedBlaze(EntityType<? extends Blaze> type, Level world, Player player) {
      this(type, world);
      tame(player);
   }

   public TamedBlaze(Level world, Player player) {
      this(RegistryHandler.TAMED_BLAZE.get(), world);
      tame(player);
   }

   @Override
   protected void registerGoals() {
      this.goalSelector.addGoal(1, new BlazeAttackGoal(this));
      this.goalSelector.addGoal(1, new BlazeAttackGoal(this));
      this.goalSelector.addGoal(2, new FollowSummonerGoal(this, 1.0D, 12.0F, 1.0F, true));
      this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
      this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D, 0.0F));
      this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
      this.targetSelector.addGoal(1, new SummonerHurtByTargetGoal(this));
      this.targetSelector.addGoal(2, new SummonerHurtTargetGoal(this));
      this.targetSelector.addGoal(3, (new HurtByTargetGoal(this)).setAlertOthers());
   }

   /// This makes the necromancy mobs wither in sunlight
   @Override
   public void aiStep() {
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

   ////////////////////////////////////////
   ////////////////////////////////////////
   /////// BLAZE NONSENSE ////////////////
   ////////////////////////////////////////
   ////////////////////////////////////////

   public boolean isOnFire() {
      return this.isCharged();
   }

   private boolean isCharged() {
      return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
   }

   protected void setChargedTame(boolean pOnFire) {
      byte b0 = this.entityData.get(DATA_FLAGS_ID);
      if (pOnFire) {
         b0 = (byte) (b0 | 1);
      } else {
         b0 = (byte) (b0 & -2);
      }

      this.entityData.set(DATA_FLAGS_ID, b0);
   }

   class BlazeAttackGoal extends Goal {
      private final TamedBlaze blaze;
      private int attackStep;
      private int attackTime;
      private int lastSeen;

      public BlazeAttackGoal(TamedBlaze p_32247_) {
         this.blaze = p_32247_;
         this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
      }

      /**
       * Returns whether execution should begin. You can also read and cache any state
       * necessary for execution in this
       * method as well.
       */
      public boolean canUse() {
         LivingEntity livingentity = this.blaze.getTarget();
         return livingentity != null && livingentity.isAlive() && this.blaze.canAttack(livingentity);
      }

      /**
       * Execute a one shot task or start executing a continuous task
       */
      public void start() {
         this.attackStep = 0;
      }

      /**
       * Reset the task's internal state. Called when this task is interrupted by
       * another one
       */
      public void stop() {
         this.blaze.setChargedTame(false);
         this.lastSeen = 0;
      }

      public boolean requiresUpdateEveryTick() {
         return true;
      }

      /**
       * Keep ticking a continuous task that has already been started
       */
      public void tick() {
         --this.attackTime;
         LivingEntity livingentity = this.blaze.getTarget();
         if (livingentity != null) {
            boolean flag = this.blaze.getSensing().hasLineOfSight(livingentity);
            if (flag) {
               this.lastSeen = 0;
            } else {
               ++this.lastSeen;
            }

            double d0 = this.blaze.distanceToSqr(livingentity);
            if (d0 < 4.0D) {
               if (!flag) {
                  return;
               }

               if (this.attackTime <= 0) {
                  this.attackTime = 20;
                  this.blaze.doHurtTarget(livingentity);
               }

               this.blaze.getMoveControl().setWantedPosition(livingentity.getX(), livingentity.getY(),
                     livingentity.getZ(), 1.0D);
            } else if (d0 < this.getFollowDistance() * this.getFollowDistance() && flag) {
               double d1 = livingentity.getX() - this.blaze.getX();
               double d2 = livingentity.getY(0.5D) - this.blaze.getY(0.5D);
               double d3 = livingentity.getZ() - this.blaze.getZ();
               if (this.attackTime <= 0) {
                  ++this.attackStep;
                  if (this.attackStep == 1) {
                     this.attackTime = 60;
                     this.blaze.setChargedTame(true);
                  } else if (this.attackStep <= 4) {
                     this.attackTime = 6;
                  } else {
                     this.attackTime = 100;
                     this.attackStep = 0;
                     this.blaze.setChargedTame(false);
                  }

                  if (this.attackStep > 1) {
                     // inaccuracy?
                     // TODO: check this
                     double d4 = Math.sqrt(Math.sqrt(d0)) * 0.2D;
                     if (!this.blaze.isSilent()) {
                        this.blaze.level.levelEvent((Player) null, 1018, this.blaze.blockPosition(), 0);
                     }

                     for (int i = 0; i < 1; ++i) {
                        SmallFireball smallfireball = new SmallFireball(this.blaze.level, this.blaze,
                              d1 + this.blaze.getRandom().nextGaussian() * d4, d2,
                              d3 + this.blaze.getRandom().nextGaussian() * d4);
                        smallfireball.setPos(smallfireball.getX(), this.blaze.getY(0.5D) + 0.5D, smallfireball.getZ());
                        this.blaze.level.addFreshEntity(smallfireball);
                     }
                  }
               }
               this.blaze.getLookControl().setLookAt(livingentity, 10.0F, 10.0F);
            } else if (this.lastSeen < 5) {
               this.blaze.getMoveControl().setWantedPosition(livingentity.getX(), livingentity.getY(),
                     livingentity.getZ(), 1.0D);
            }
            super.tick();
         }
      }

      private double getFollowDistance() {
         return this.blaze.getAttributeValue(Attributes.FOLLOW_RANGE);
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
      this.entityData.define(DATA_FLAGS_ID, (byte) 0);
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
         this.entityData.set(DATA_FLAGS_ID, (byte) (b0 | 4));
      } else {
         this.entityData.set(DATA_FLAGS_ID, (byte) (b0 & -5));
      }

      this.reassessTameGoals();
   }

   protected void reassessTameGoals() {
   }

   @Nullable
   public UUID getOwnerUUID() {
      return this.entityData.get(DATA_OWNERUUID_ID).orElse((UUID) null);
   }

   public void setOwnerUUID(@Nullable UUID p_21817_) {
      this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(p_21817_));
   }

   public void tame(Player pPlayer) {
      this.setTame(true);
      this.setOwnerUUID(pPlayer.getUUID());
      if (pPlayer instanceof ServerPlayer) {
         CriteriaTriggers.SUMMONED_ENTITY.trigger((ServerPlayer) pPlayer, this);
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
         if (!this.level.isClientSide && this.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)
               && this.getSummoner() instanceof ServerPlayer) {
            this.getSummoner().sendMessage(deathMessage, Util.NIL_UUID);
         }
   }

   // END generic tamed monster stuff
}
