package com.chemelia.vanillaarcana.item;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class ChorusSproutItem extends Item {

    public ChorusSproutItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack,Player attacker,LivingEntity target,InteractionHand hand) {
        Level world = target.level;
        if (world.isClientSide()) return InteractionResult.PASS;
        InteractionResult result = InteractionResult.FAIL;
        double targetX = target.getX();
        double targetY = target.getY();
        double targetZ = target.getZ();
        //boolean succeeded = false;
        for (int i = 0; i < 16; ++i) {
            double tryTargetX = targetX + (target.getRandom().nextDouble() - 0.5D) * 16.0D;
            double tryTargetY = Mth.clamp(targetY + (double) (target.getRandom().nextInt(16) - 8), (double) world.getMinBuildHeight(), (double) (world.getMinBuildHeight() + ((ServerLevel) world).getLogicalHeight() - 1));
            double tryTargetZ = targetX + (target.getRandom().nextDouble() - 0.5D) * 16.0D;
        if (target.isPassenger()) {
            attacker.stopRiding();
        }
        net.minecraftforge.event.entity.EntityTeleportEvent.ChorusFruit event = net.minecraftforge.event.ForgeEventFactory.onChorusFruitTeleport(target, tryTargetX, tryTargetY, tryTargetZ);
        //if (event.isCanceled()) return InteractionResult.FAIL;
        if (target.randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) {
           SoundEvent soundevent = target instanceof Fox ? SoundEvents.FOX_TELEPORT : SoundEvents.CHORUS_FRUIT_TELEPORT;
           world.playSound((Player)null, targetX, targetY, targetZ, soundevent, SoundSource.PLAYERS, 1.0F, 1.0F);
           target.playSound(soundevent, 1.0F, 1.0F);
           result = InteractionResult.SUCCESS;
           break;
        }
     }
     if (attacker instanceof Player) {
        ((Player)attacker).getCooldowns().addCooldown(this, 200);
     }
     return result;
    }
}
