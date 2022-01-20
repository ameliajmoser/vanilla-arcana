package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.VanillaArcana;
import com.chemelia.vanillaarcana.entity.projectile.FrostSnowball;
import com.chemelia.vanillaarcana.entity.projectile.OldThrownBlock;
import com.chemelia.vanillaarcana.entity.projectile.ThrownBlock;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class TelekinesisEnchantment extends SpellEnchantment {
    private final static int SPELL_COOLDOWN = 10;
    private final static int SPELL_COST = 10;
    private final static int MAX_LEVEL = 5;
    private final static int PROJECTILE_SPEED = 1;
    public static final String ID = VanillaArcana.MOD_ID + ":telekinesis";

    public TelekinesisEnchantment(){
        super(Rarity.RARE, SPELL_COOLDOWN, SPELL_COST);
    }

    @Override
    public int getMaxLevel(){
        return MAX_LEVEL;
    }

    @Override
    public boolean handleCast(Level world, LivingEntity user, ItemStack stack){
        if (super.handleCast(world, user, stack)){
            int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
            Vec3 look = user.getLookAngle();
            Vec3 pos = user.getEyePosition().add(look.scale(0.9));

            ThrownBlock thrownBlock = new ThrownBlock(user, world, new ItemStack(Blocks.GRASS_BLOCK.defaultBlockState().getBlock().asItem()), stack);
            thrownBlock.setPos(pos.x, pos.y, pos.z);
            thrownBlock.setDeltaMovement(look.scale(PROJECTILE_SPEED));
            world.addFreshEntity(thrownBlock);
        
            if (user instanceof Player) {
                ((Player) user).getCooldowns().addCooldown(stack.getItem(), SPELL_COOLDOWN * spellLevel * spellLevel);
            }
            
            return true;
        } else return false;
    }
}
