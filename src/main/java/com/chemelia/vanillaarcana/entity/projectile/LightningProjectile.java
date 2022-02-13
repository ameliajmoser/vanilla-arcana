package com.chemelia.vanillaarcana.entity.projectile;

import com.chemelia.vanillaarcana.RegistryHandler;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class LightningProjectile extends LlamaSpit {
    private final int spellLevel;


    public LightningProjectile(EntityType<? extends LightningProjectile> p_37224_, Level p_37225_, int spellLevel) {
        super(p_37224_, p_37225_);
        this.spellLevel = spellLevel;
        this.setNoGravity(true);
    }
    public LightningProjectile(EntityType<? extends LightningProjectile> type, Level world) {
        this(type, world, 0);
    }

    public LightningProjectile(Level world, LivingEntity user, int spellLevel) {
        this(RegistryHandler.LIGHTNING_PROJECTILE.get(), world, spellLevel);
        this.setOwner(user);
    }

    /**
     * Called when the arrow hits an entity
     */
    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity entity = this.getOwner();
        if (entity instanceof LivingEntity) {
            pResult.getEntity().hurt(DamageSource.LIGHTNING_BOLT.setProjectile().setMagic(), 2.0F + spellLevel);
        }
    }

}
