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
        Vec3 pos = user.getEyePosition().add(look.scale(0.4));

        if (user instanceof Player) {
            Player player = (Player) user;
            if (player.totalExperience < spellCost * spellLevel && !player.isCreative()) {
                doSpellFailure(user, world, pos, stack, spellCost, spellLevel);
                return false;
            } else {
                doSpellSuccess(user, world, pos, stack, spellCost, spellLevel);
                return true;
            }
        }
        return true;
    }

    protected void doSpellSuccess(LivingEntity user, Level world, Vec3 pos, ItemStack stack, int spellCost, int spellLevel){
        if (user instanceof Player){
            Player player = (Player) user;
            subtractSpellXP(player, spellCost*spellLevel);
            cooldownSuccess(player, stack, spellLevel);
        }
        playSuccessSound(world, user, spellLevel);
        spawnSuccessParticle(world, pos);
    }

    protected void doSpellFailure(LivingEntity user, Level world, Vec3 pos, ItemStack stack, int spellCost, int spellLevel){
        if (user instanceof Player){
            cooldownFail((Player) user, stack);
        }
        playFailureSound(world, user);
        spawnFailureParticle(world, pos);
    }

    protected void subtractSpellXP(Player player, int cost){
        if (!player.isCreative()){
            if (player.totalExperience < cost){
                System.out.println("ERROR - should have checked xp beforehand");
            }
            player.giveExperiencePoints(-cost);
        }
    }

    protected void cooldownFail(Player player, ItemStack stack){
        player.getCooldowns().addCooldown(stack.getItem(), spellCooldown);
    }
    protected void cooldownSuccess(Player player, ItemStack stack, int spellLevel){
        player.getCooldowns().addCooldown(stack.getItem(), spellCooldown*spellLevel*spellLevel);
    }


    protected void spawnSuccessParticle(Level world, Vec3 pos){
        ((ServerLevel) world).sendParticles(ParticleTypes.GLOW, pos.x, pos.y, pos.z, 15, 0, 0, 0, 0.2);
    }
    protected void spawnFailureParticle(Level world, Vec3 pos) {
        ((ServerLevel) world).sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 5, 0, 0, 0, 0.05);
    }
    protected void playSuccessSound(Level world, LivingEntity user, int spellLevel){
        world.playSound(null, user.blockPosition(), RegistryHandler.SPELL_CAST.get(), SoundSource.PLAYERS, 1, 1.5F / spellLevel);
    }
    protected void playFailureSound(Level world, LivingEntity user){
        world.playSound(null, user.blockPosition(), RegistryHandler.SPELL_FAIL.get(), SoundSource.PLAYERS, 1, 0.9F);
    }
}
