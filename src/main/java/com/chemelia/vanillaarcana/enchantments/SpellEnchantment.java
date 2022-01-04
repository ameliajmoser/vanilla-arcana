package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.RegistryHandler;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class SpellEnchantment extends Enchantment {
    public static int spellCooldown;
    public static int spellCost;

    public SpellEnchantment(Rarity rarity, int spellCooldown, int spellCost) {
        super(rarity, RegistryHandler.WAND_CATEGORY, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
        SpellEnchantment.spellCooldown = spellCooldown;
        SpellEnchantment.spellCost = spellCost;
    }

    // SpellEnchantments are *not* compatible with each other - one spell per wand
    @Override
    protected boolean checkCompatibility(Enchantment other) {
        if (other instanceof SpellEnchantment) {
            return false;
        } else {
            return super.checkCompatibility(other);
        }
    }

    public boolean handleClientCast(Level world, LivingEntity user, ItemStack stack) {
        return false;
    }

    public boolean handleCast(Level world, LivingEntity user, ItemStack stack) {
        if (world.isClientSide()) {
            return this.handleClientCast(world, user, stack);
        }
        int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
        Vec3 look = user.getLookAngle();
        Vec3 pos = user.getEyePosition().add(look.scale(0.9));

        if (user instanceof Player) {
            Player player = (Player) user;
            if (player.totalExperience < spellCost * spellLevel && !player.isCreative()) {
                player.getCooldowns().addCooldown(stack.getItem(), spellCooldown * 10);
                playFailureSound(world, user);
                if (world instanceof ServerLevel) {spawnFailureParticle(world, pos);}
                return false;
            } else if (!player.isCreative()) {
                player.giveExperiencePoints(-spellCost * spellLevel);
            }
            playSuccessSound(world, user, spellLevel);
            if (world instanceof ServerLevel) {spawnSuccessParticle(world, pos);}
        }
        return true;
    }
    
    protected void spawnSuccessParticle(Level world, Vec3 pos){
        ((ServerLevel) world).sendParticles(ParticleTypes.GLOW, pos.x, pos.y, pos.z, 15, 0, 0, 0, 0.5);
    }
    protected void spawnFailureParticle(Level world, Vec3 pos) {
        ((ServerLevel) world).sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 15, 0, 0, 0, 0.1);
    }
    protected void playSuccessSound(Level world, LivingEntity user, int spellLevel){
        world.playSound(null, user.blockPosition(), RegistryHandler.SPELL_CAST.get(), SoundSource.PLAYERS, 1, 1.5F / spellLevel);
    }
    protected void playFailureSound(Level world, LivingEntity user){
        world.playSound(null, user.blockPosition(), RegistryHandler.SPELL_FAIL.get(), SoundSource.PLAYERS, 1, 0.9F);
    }

}
