package com.chemelia.vanillaarcana.enchantments;

import java.util.Random;

import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

//////////
// WARP //
//////////
//I:   Teleport using XP!

public class WarpEnchantment extends SpellEnchantment {
    private final static int SPELL_COOLDOWN = 30;
    private final static int SPELL_COST = 15;
    private final static int MAX_LEVEL = 3;
    private final static int RANGE = 20;
    protected final Random random = new Random();
    public static final String ID = VanillaArcana.MOD_ID + ":warp";

    public WarpEnchantment() {
        super(Rarity.RARE, SPELL_COOLDOWN, SPELL_COST);
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
            if (player.totalExperience < spellCost * spellLevel && !player.isCreative()) {
                doSpellFailure(user, world, pos, stack, spellCost, spellLevel);
                return false;
            } else {
                Vec3 destination = pos.add(look.scale((double) RANGE*spellLevel*spellLevel));
                if (spellLevel > 4){
                    destination = pos.add(look.scale(99999.0D));
                }
                BlockHitResult cast = user.level.clip(new ClipContext(pos, destination, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, user));

                if (cast.getType() != HitResult.Type.MISS){
                    doSpellSuccess(user, world, pos, stack, spellCost, spellLevel);
                    user.teleportTo(cast.getLocation().x, cast.getLocation().y, cast.getLocation().z);
                    user.resetFallDistance();
                    playSuccessSoundLocation(world, cast.getLocation(), spellLevel);
                    spawnSuccessParticle(world, cast.getLocation());
                    return true;
                } else {
                    doSpellFailure(user, world, pos, stack, spellCost, spellLevel);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void spawnSuccessParticle(Level world, Vec3 pos){
        if (world instanceof ServerLevel){
            for(int i = 0; i < 32; ++i) {
               ((ServerLevel) world).sendParticles(ParticleTypes.PORTAL, pos.x, pos.y + this.random.nextDouble()*2.0D, pos.z, 1, 0.0, 0.0, 0.0, this.random.nextGaussian()*5.0D);
            }
         }
    }

    protected void playSuccessSoundLocation(Level world, Vec3 pos, int spellLevel){
        world.playSound(null, new BlockPos(pos), SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1, 1.0F);
    }

    @Override
    protected void playSuccessSound(Level world, LivingEntity user, int spellLevel){
        world.playSound(null, user.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.PLAYERS, 1, 1.0F);
    }
    

    
    
}
