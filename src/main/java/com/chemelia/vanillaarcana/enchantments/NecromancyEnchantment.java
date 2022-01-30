package com.chemelia.vanillaarcana.enchantments;

import java.util.Random;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.VanillaArcana;
import com.chemelia.vanillaarcana.entity.monster.TamedZombie;
import com.chemelia.vanillaarcana.interfaces.SummonedEntity;
import com.chemelia.vanillaarcana.entity.monster.TamedSkeleton;
import com.chemelia.vanillaarcana.entity.monster.TamedWitherSkeleton;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.WitherSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class NecromancyEnchantment extends SpellEnchantment {
    private final static int SPELL_COOLDOWN = 30;
    public static final int SPELL_COST = 50;
    private final static int MAX_LEVEL = 3;
    private static final int RANGE = 5;
    protected final Random random = new Random();
    public static final String ID = VanillaArcana.MOD_ID + ":necromancy";

    public NecromancyEnchantment() {
        super(Rarity.RARE, SPELL_COOLDOWN, SPELL_COST);
    }

    @Override
    public int getMaxLevel() {
        return MAX_LEVEL;
    }

    @Override
    public void doPostAttack(LivingEntity attacker, Entity target, int spellLevel) {
        Boolean success = false;
        if (!attacker.level.isClientSide()) {
            if (attacker instanceof Player) {
                Player player = (Player) attacker;
                if (player.totalExperience > SPELL_COST * spellLevel || player.isCreative()) {
                    Mob mob = (Mob) target;
                    switch (spellLevel) {
                        case 3:
                            // i should probably make a method for this huh
                            if (mob instanceof WitherSkeleton) {
                                if (!(mob instanceof SummonedEntity)) {
                                    mob.convertTo(RegistryHandler.TAMED_WITHER_SKELETON.get(), true);
                                } else if (((OwnableEntity) mob).getOwnerUUID() == player.getUUID()) {
                                    break;
                                }
                                spawnTameParticles(attacker.level, target.getEyePosition());
                                ((TamedWitherSkeleton) mob).tame((Player) attacker);
                                mob.lookAt(attacker, 180, 180);
                                success = true;
                            }
                        case 2:
                            if (mob instanceof Skeleton) {
                                if (!(mob instanceof SummonedEntity)) {
                                    mob.convertTo(RegistryHandler.TAMED_SKELETON.get(), true);
                                } else if (((OwnableEntity) mob).getOwnerUUID() == player.getUUID()) {
                                    break;
                                }
                                spawnTameParticles(attacker.level, target.getEyePosition());
                                ((TamedSkeleton) mob).tame((Player) attacker);
                                mob.lookAt(attacker, 180, 180);
                                success = true;
                            }
                        case 1:
                            if (mob instanceof Zombie) {
                                if (!(mob instanceof SummonedEntity)) {
                                    mob.convertTo(RegistryHandler.TAMED_ZOMBIE.get(), true);
                                } else if (((OwnableEntity) mob).getOwnerUUID() == player.getUUID()) {
                                    break;
                                }
                                spawnTameParticles(attacker.level, target.getEyePosition());
                                ((TamedZombie) mob).tame((Player) attacker);
                                mob.lookAt(attacker, 180, 180);
                                success = true;
                            }
                        default:
                            break;
                    }
                    if (success) {
                        doSpellSuccess(attacker, attacker.level, attacker.position(), attacker.getItemInHand(InteractionHand.MAIN_HAND), spellCost, spellLevel);
                    }
                }
            }
        }
    }

    @Override
    public boolean handleCast(Level world, LivingEntity user, ItemStack stack) {
        if (world.isClientSide()) {
            return this.handleClientCast(world, user, stack);
        }
        int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
        Vec3 look = user.getLookAngle();
        Vec3 pos = user.getEyePosition(1.0F).add(look.scale(0.4));
        Vec3 destination = pos.add(look.scale((double) RANGE));
        BlockHitResult cast = user.level
                .clip(new ClipContext(pos, destination, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, user));

        if (user instanceof Player) {
            Player player = (Player) user;
            if (cast.getType() != Type.MISS
                    && (player.totalExperience > spellCost * spellLevel || player.isCreative())) {
                Mob monster = null;
                switch (spellLevel) {
                    case 1:
                        monster = new TamedZombie(RegistryHandler.TAMED_ZOMBIE.get(), world, player);
                        break;
                    case 2:
                        monster = new TamedSkeleton(RegistryHandler.TAMED_SKELETON.get(), world, player);
                        break;
                    case 3:
                        monster = new TamedWitherSkeleton(RegistryHandler.TAMED_WITHER_SKELETON.get(), world, player);
                        break;
                }
                monster.setPos(cast.getLocation().add(0.0, -2.0, 0.0));
                monster.setDeltaMovement(0.0, 0.7, 0.0);
                world.addFreshEntity(monster);

                if (!player.isCreative()) {
                    player.giveExperiencePoints(-spellCost * spellLevel);
                }
                player.getCooldowns().addCooldown(stack.getItem(), SPELL_COOLDOWN * spellLevel * spellLevel);
                playSuccessSound(world, user, spellLevel);
                if (world instanceof ServerLevel) {
                    spawnSuccessParticle(world, pos);
                }
                return true;
            } else {
                playFailureSound(world, user);
                if (world instanceof ServerLevel) {
                    spawnFailureParticle(world, pos);
                }
                player.getCooldowns().addCooldown(stack.getItem(), SPELL_COOLDOWN * spellLevel * spellLevel);
                return false;
            }
        } else
            return false;
    }

    private void spawnTameParticles(Level level, Vec3 pos) {
        ((ServerLevel) level).sendParticles(ParticleTypes.HEART, pos.x, pos.y, pos.z, 1, 0, 0.5, 0, 0.3);
    }

}
