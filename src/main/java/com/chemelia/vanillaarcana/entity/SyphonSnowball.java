package com.chemelia.vanillaarcana.entity;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.enchantments.SyphonEnchantment;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class SyphonSnowball extends Snowball {
    private LivingEntity owner;
    private Level world;
    private int spellLevel;

    public SyphonSnowball(EntityType<? extends Snowball> type, Level world){
        super(type, world);
    }

    public SyphonSnowball(Level world, LivingEntity owner, int spellLevel) {
        super(world, owner);
        this.owner = owner;
        this.world = world;
        this.spellLevel = spellLevel;
    }


    public static final int SPELL_COST = SyphonEnchantment.SPELL_COST;


    public void onHitEntity(EntityHitResult result){
        super.onHitEntity(result);
        Entity hitEntity = result.getEntity();
        hitEntity.hurt(DamageSource.thrown(this, this.getOwner()), (float)spellLevel);
        if (hitEntity instanceof Player){
            ((Player) hitEntity).giveExperiencePoints(-SPELL_COST * spellLevel);
            ((Player) owner).giveExperiencePoints(SPELL_COST * spellLevel);
        } else if (!world.isClientSide()){
            owner.level.playSound(null, owner.blockPosition(), RegistryHandler.SPELL_SAP.get(), SoundSource.PLAYERS, 1, 0.5F);
            for (int i = 0; i< SPELL_COST + spellLevel; ++i){
                ExperienceOrb orb = new ExperienceOrb(world, hitEntity.position().x, hitEntity.position().y + 1, hitEntity.position().z, 1);
                world.addFreshEntity(orb);
            }
        }
    }
}
