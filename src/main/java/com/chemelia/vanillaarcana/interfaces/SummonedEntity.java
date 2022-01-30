package com.chemelia.vanillaarcana.interfaces;

import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;

public interface SummonedEntity {

   @Nullable
   UUID getOwnerUUID();

   @Nullable
   LivingEntity getSummoner();
}
