package com.chemelia.vanillaarcana.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class FocusCrystal extends Item {
    private static final int THRESHOLD = 3;
    private static final int XP_PER_DURABILITY = 1;

    public FocusCrystal(Properties pProperties) {
        super(pProperties);
    }


    /**
    * Called as the item is being used by an entity.
    */
    // public void onUseTick(Level pLevel, LivingEntity user, ItemStack stack, int pRemainingUseDuration) {
    //     System.out.println("using? timer: " + this.timer);
    //     if (this.timer > THRESHOLD){
    //         if (user.isCrouching() && user instanceof Player){
    //             inputExperience((Player) user);
    //         } else {
    //             produceOrb(user);
    //         }
    //         resetTimer();
    //     } else {
    //         ++this.timer;
    //     }
    // }

    /**
     * How long it takes to use or consume an item
     */
    public int getUseDuration(ItemStack pStack) {
        return THRESHOLD;
    }

    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user){
        if (user.isCrouching() && user instanceof Player){
            return inputExperience((Player) user);
        } else {
            produceOrb(user);
        }
        return stack;
    }


    private void crystalExplosion(LivingEntity user){
        user.level.explode(null, user.getX(), user.getY(), user.getZ(), 7.0F, Explosion.BlockInteraction.DESTROY);
    }

    private void produceOrb(LivingEntity user){
        ItemStack stack = user.getItemInHand(user.getUsedItemHand());
        stack.hurtAndBreak(1, user, (p) -> {
            p.broadcastBreakEvent(user.getUsedItemHand());
            this.crystalExplosion(user);
        });
        playDrawSound(user, stack);
        Vec3 pos = user.getEyePosition().add(user.getLookAngle().scale(2.0));
        Level world = user.getLevel();
        ExperienceOrb orb = new ExperienceOrb(world, pos.x, pos.y, pos.z, XP_PER_DURABILITY);
        //orb.setDeltaMovement(0, 1.0, 0);
        world.addFreshEntity(orb);
    }

    private ItemStack inputExperience(Player player){
        ItemStack stack = player.getItemInHand(player.getUsedItemHand());
        if (player.totalExperience > XP_PER_DURABILITY || player.isCreative()){
            player.giveExperiencePoints(-XP_PER_DURABILITY);
            playLoadSound(player, stack);
            if (stack.getDamageValue()-1 < 0){
                stack.hurtAndBreak(999, player, (p) -> {
                    p.broadcastBreakEvent(player.getUsedItemHand());
                    this.crystalExplosion(player);
                });
            } else {
                stack.setDamageValue(stack.getDamageValue() - 1);
            }
        } else {
            this.crystalExplosion(player);
        }
        return stack;
    }

    @Override
    public boolean isFoil(ItemStack stack){
        return true;
    }

    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BLOCK;
    }

    /**
     * Called to trigger the item's "innate" right click behavior. To handle when
     * this item is used on a Block, see
     * {@link #onItemUse}.
     */
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.pass(itemstack);
    }

    private void playLoadSound(LivingEntity user, ItemStack stack){
        float pitch = 1 -stack.getDamageValue()/stack.getMaxDamage() + 0.9F;
        if (user instanceof Player){
            user.level.playSound((Player) user, user.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.6F, pitch);
        } else {
            user.level.playSound(null, user.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.6F, pitch);
        }
    }
    private void playDrawSound(LivingEntity user, ItemStack stack){
        float pitch = 1- (stack.getDamageValue())/stack.getMaxDamage() + 0.4F;
        if (user instanceof Player){
            user.level.playSound((Player) user, user.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.6F, pitch);
        } else {
            user.level.playSound(null, user.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.6F, pitch);
        }
    }

}
