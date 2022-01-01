package com.chemelia.vanillaarcana.entity.monster;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

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
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.OldUsersConverter;

public abstract class SummonedMonster extends Monster implements OwnableEntity {
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(SummonedMonster.class, EntityDataSerializers.BYTE);
    protected static final EntityDataAccessor<Optional<UUID>> DATA_OWNERUUID_ID = SynchedEntityData.defineId(SummonedMonster.class, EntityDataSerializers.OPTIONAL_UUID);
    
    protected SummonedMonster(EntityType<? extends SummonedMonster> type, Level world) {
        super(type, world);
    }
    
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
            } catch (Throwable throwable) {
                //oops!
            }
        }
    }

    public boolean canBeLeashed(Player pPlayer) {
        return !this.isLeashed();
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

    @Nullable
    public UUID getOwnerUUID() {
       return this.entityData.get(DATA_OWNERUUID_ID).orElse((UUID)null);
    }
 
    public void setOwnerUUID(@Nullable UUID uuid) {
       this.entityData.set(DATA_OWNERUUID_ID, Optional.ofNullable(uuid));
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
        LivingEntity summoner = this.getOwner();
        if (summoner != null) {
            return summoner.getTeam();
        }
        return super.getTeam();
     }
  
     /**
      * Returns whether this Entity is on the same team as the given Entity.
      */
     public boolean isAlliedTo(Entity pEntity) {
        LivingEntity summoner = this.getOwner();
        if (pEntity == summoner) {
            return true;
        }
        if (summoner != null) {
            return summoner.isAlliedTo(pEntity);
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
