package com.chemelia.vanillaarcana.entity.projectile;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;

public class ThrowableBlock extends ThrowableItemProjectile implements IEntityAdditionalSpawnData {

    public ThrowableBlock(EntityType<? extends ThrowableItemProjectile> type, LivingEntity owner,
            Level world) {
        super(type, owner, world);
    }

    public ThrowableBlock(EntityType<? extends ThrowableItemProjectile> type, double posX, double posY,
            double posZ, Level world) {
        super(type, posX, posY, posZ, world);
    }


    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        
    }

    @Override
    protected Item getDefaultItem() {
        return null;
    }

}