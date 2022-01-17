package com.chemelia.vanillaarcana.enchantments;

import java.util.Random;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.VanillaArcana;
import com.chemelia.vanillaarcana.entity.monster.TamedZombie;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
    private final static int SPELL_COOLDOWN = 60;
    public static final int SPELL_COST = 20;
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
            if (attacker instanceof Player){
                Player player = (Player) attacker;
                if (player.totalExperience > SPELL_COST * spellLevel || player.isCreative()) {
                    Mob mob = (Mob) target;
                    switch (spellLevel) {
                        case 3:
                            if (target instanceof WitherSkeleton && !(target instanceof TamedZombie)) {
                                mob.convertTo(RegistryHandler.TAMED_ZOMBIE.get(), true);
                                success = true;
                            }
                            break;
                        case 2:
                            if (target instanceof Skeleton && !(target instanceof TamedZombie)) {
                                mob.convertTo(RegistryHandler.TAMED_ZOMBIE.get(), true);
                                success = true;
                            }
                            break;
                        case 1:
                            if (target instanceof Zombie && !(target instanceof TamedZombie)) {
                                mob.convertTo(RegistryHandler.TAMED_ZOMBIE.get(), true);
                                success = true;
                            }
                            break;
                        default:
                            break;
                    }
                    //subtract xp
                    if (success){
                        player.giveExperiencePoints(-SPELL_COST * spellLevel);
                        spawnTameParticles(attacker.getLevel(), target.getEyePosition());
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
        BlockHitResult cast = user.level.clip(new ClipContext(pos, destination, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, user));

        if (user instanceof Player) {
            Player player = (Player) user;
            if (cast.getType() != Type.MISS && (player.totalExperience > spellCost * spellLevel || player.isCreative())){
                switch (spellLevel){
                    case 1:
                        TamedZombie zombie = new TamedZombie(RegistryHandler.TAMED_ZOMBIE.get(), world, player);
                        zombie.setPos(cast.getLocation().add(0.0, -2.0, 0.0));
                        zombie.setDeltaMovement(0.0, 0.7, 0.0);;
                        world.addFreshEntity(zombie);
                        zombie.addEffect(new MobEffectInstance(MobEffects.WITHER, 9999, 0, true, false));
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                if (!player.isCreative()){
                    player.giveExperiencePoints(-spellCost * spellLevel);
                }
                player.getCooldowns().addCooldown(stack.getItem(), SPELL_COOLDOWN * spellLevel * spellLevel);
                playSuccessSound(world, user, spellLevel);
                if (world instanceof ServerLevel) {spawnSuccessParticle(world, pos);}
                return true;     
            } else {
                playFailureSound(world, user);
                if (world instanceof ServerLevel) {spawnFailureParticle(world, pos);}
                player.getCooldowns().addCooldown(stack.getItem(), SPELL_COOLDOWN * spellLevel * spellLevel);
                return false;
            }
        } else return false;
    }

    private void spawnTameParticles(Level level, Vec3 pos){
        ((ServerLevel) level).sendParticles(ParticleTypes.HEART, pos.x, pos.y, pos.z, 1, 0, 0.5, 0, 0.3);
    }

}
