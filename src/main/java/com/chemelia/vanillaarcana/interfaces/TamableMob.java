package com.chemelia.vanillaarcana.interfaces;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public interface TamedMob {
    public String getMobOwner();
    public int getMaxHealth();    
    public void setOwner(Player player);
    public boolean hasOwner();
    public void spawnEffects();
    public EntityType<? extends LivingEntity> getMobType();
}
