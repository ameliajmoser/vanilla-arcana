package com.chemelia.vanillaarcana.enchantments;

import javax.annotation.Nullable;

import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
/////////////
//LIGHTNING//
/////////////
//Shoot a projectile that creates a lightning strike, or create a lightning strike at the block you're looking at?
import net.minecraft.world.phys.Vec3;

public class LightningEnchantment extends SpellEnchantment {
    private final static int SPELL_COOLDOWN = 20;
    private final static int SPELL_COST = 15;
    private final static int MAX_LEVEL = 3;
    private final static int RANGE = 15;
    public static final String ID = VanillaArcana.MOD_ID + ":lightning";

    public LightningEnchantment() {
        super(Rarity.UNCOMMON, SPELL_COOLDOWN, SPELL_COST);
    }

    @Override
    public int getMaxLevel(){
        return MAX_LEVEL;
    }

    @Override
    public boolean handleCast(Level world, LivingEntity user, ItemStack stack){
        if (world.isClientSide()) {
            return this.handleClientCast(world, user, stack);
        }
        int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
        Vec3 look = user.getLookAngle();
        Vec3 pos = user.getEyePosition().add(look.scale(0.4));

        if (user instanceof Player) {
            Player player = (Player) user;
            if (player.totalExperience < spellCost*spellLevel && !player.isCreative()) {
                doSpellFailure(user, world, pos, stack, spellCost, spellLevel);
                return false;
            } else {
                Vec3 destination = pos.add(look.scale((double) RANGE*spellLevel*spellLevel));
                BlockHitResult cast = user.level.clip(new ClipContext(pos, destination, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user));

                if (cast.getType() != HitResult.Type.MISS){
                    doSpellSuccess(user, world, pos, stack, spellCost, spellLevel);
                    LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, world);
                    bolt.setPos(cast.getLocation());
                    bolt.setDamage(3.0F + 2*spellLevel);
                    if (spellLevel > 1){
                        world.explode(user, cast.getLocation().x(), cast.getLocation().y(), cast.getLocation().z(), spellLevel, Explosion.BlockInteraction.BREAK);
                    }
                    world.addFreshEntity(bolt);
                    return true;
                } else {
                    doSpellFailure(user, world, pos, stack, spellCost, spellLevel);
                    return false;
                }
            }
        }
        return true;
    }
}
