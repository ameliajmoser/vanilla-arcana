package com.chemelia.vanillaarcana.entity.monster;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;

public abstract class TamableMonster extends Monster implements OwnableEntity {
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(TamableMonster.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(TamableMonster.class, EntityDataSerializers.OPTIONAL_UUID);

    protected TamableMonster(EntityType<? extends Monster> p_33002_, Level p_33003_) {
        super(p_33002_, p_33003_);
        this.reassessTameGoals();
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte) 0);
        this.entityData.define(DATA_OWNERUUID_ID, Optional.empty());
    }

    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.getOwnerUUID() != null) {
            compoundTag.putUUID("Owner", this.getOwnerUUID());
        }
    }

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

    /**
     * Play the taming effect, will either be hearts or smoke depending on status
     */
    protected void spawnTamingParticles(boolean pPlay) {
        ParticleOptions particleoptions = ParticleTypes.HEART;
        if (!pPlay) {
            particleoptions = ParticleTypes.SMOKE;
        }

        for (int i = 0; i < 7; ++i) {
            double d0 = this.random.nextGaussian() * 0.02D;
            double d1 = this.random.nextGaussian() * 0.02D;
            double d2 = this.random.nextGaussian() * 0.02D;
            this.level.addParticle(particleoptions, this.getRandomX(1.0D), this.getRandomY() + 0.5D,
                    this.getRandomZ(1.0D), d0, d1, d2);
        }

    }

    /**
     * Handler for {@link World#setEntityState}
     */
    public void handleEntityEvent(byte pId) {
        if (pId == 7) {
            this.spawnTamingParticles(true);
        } else if (pId == 6) {
            this.spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(pId);
        }
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
   public LivingEntity getOwner() {
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
      return pEntity == this.getOwner();
   }

   public boolean wantsToAttack(LivingEntity pTarget, LivingEntity pOwner) {
      return true;
   }

   public Team getTeam() {
      if (this.isTame()) {
         LivingEntity livingentity = this.getOwner();
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
         LivingEntity livingentity = this.getOwner();
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
      if (!this.level.isClientSide && this.level.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES) && this.getOwner() instanceof ServerPlayer) {
         this.getOwner().sendMessage(deathMessage, Util.NIL_UUID);
      }
   }

}
