package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.VanillaArcana;
import com.chemelia.vanillaarcana.entity.monster.TamedZombie;

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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class NecromancyEnchantment extends SpellEnchantment {
    private final static int SPELL_COOLDOWN = 60;
    private final static int SPELL_COST = 20;
    private final static int MAX_LEVEL = 3;
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
        if (!attacker.level.isClientSide()) {
            if (attacker instanceof Player){
                Player player = (Player) attacker;
                if (player.totalExperience > SPELL_COST * spellLevel || !player.isCreative()) {
                    Mob mob = (Mob) target;
                    switch (spellLevel) {
                        case 3:
                            if (target instanceof WitherSkeleton) {
                                mob.convertTo(RegistryHandler.TAMED_ZOMBIE.get(), true);
                            }
                            break;
                        case 2:
                            if (target instanceof Skeleton) {
                                mob.convertTo(RegistryHandler.TAMED_ZOMBIE.get(), true);
                            }
                            break;
                        case 1:
                            if (target instanceof Zombie) {
                                mob.convertTo(RegistryHandler.TAMED_ZOMBIE.get(), true);
                            }
                            break;
                        default:
                            break;
                    }
                    //subtract xp
                    player.giveExperiencePoints(-SPELL_COST * spellLevel);
                }
            }
        }
    }

    @Override
    public boolean handleCast(Level world, LivingEntity user, ItemStack stack) {
        if (super.handleCast(world, user, stack)) {
            int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
            Vec3 look = user.getLookAngle();
            Vec3 pos = user.getEyePosition().add(look.scale(3));

            // BlockHitResult blockHitResult = getPlayerCastOnBlock(world, player,
            // ClipContext.Fluid.ANY);

            switch (spellLevel){
                case 1:
                //TODO: Find out how to create an attribute supplier for this TAMED_ZOMBIE type
                    TamedZombie zombie = new TamedZombie(RegistryHandler.TAMED_ZOMBIE.get(), world, user);
                    zombie.setPos(pos);
                    world.addFreshEntity(zombie);
                    zombie.addEffect(new MobEffectInstance(MobEffects.WITHER, 9999, 0));
                    break;
                case 2:
                    break;
                case 3:
                    break;
            }
            return true;
        } else
            return false;
    }

}
