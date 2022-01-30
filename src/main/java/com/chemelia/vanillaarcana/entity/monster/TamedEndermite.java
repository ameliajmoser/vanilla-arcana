package com.chemelia.vanillaarcana.entity.monster;

import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.scores.Team;

public class TamedEndermite extends Endermite implements SummonedEntity {

    public TamedEndermite(Level world) {
        super(RegistryHandler.TAMED_ENDERMITE.get(), world);
    }

    public TamedEndermite(EntityType<? extends TamedEndermite> type, Level world) {
        super(type, world);
    }

    public TamedEndermite(EntityType<? extends TamedEndermite> type, Level world, Player player) {
        this(type, world);
        tame(player);
    }

    public TamedEndermite(Level world, Player player) {
        this(RegistryHandler.TAMED_ENDERMITE.get(), world);
        tame(player);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        // this.goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.5D, false));
        this.goalSelector.addGoal(3, new FollowSummonerGoal(this, 1.0D, 12.0F, 1.0F, true));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new SummonerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new SummonerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, (new HurtByTargetGoal(this)).setAlertOthers());
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
   
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TamedEndermite.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(TamedEndermite.class, EntityDataSerializers.OPTIONAL_UUID);

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
