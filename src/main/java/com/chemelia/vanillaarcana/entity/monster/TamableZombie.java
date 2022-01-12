package com.chemelia.vanillaarcana.entity.monster;

import java.util.UUID;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.enchantments.NecromancyEnchantment;
import com.chemelia.vanillaarcana.entity.goal.FollowSummonerGoal;
import com.chemelia.vanillaarcana.entity.goal.SummonerHurtByTargetGoal;
import com.chemelia.vanillaarcana.entity.goal.SummonerHurtTargetGoal;
import com.mojang.blaze3d.vertex.PoseStack.Pose;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TamableZombie extends TamableMonster {

    public TamableZombie(EntityType<? extends TamableZombie> type, Level world, Player owner) {
        super(type, world);
        this.tame(owner);
    }

    public TamableZombie(EntityType<? extends TamableZombie> type, Level world) {
        super(type, world);
    }

    public TamableZombie(Level world) {
        super(RegistryHandler.TAMABLE_ZOMBIE.get(), world);
    }

    public TamableZombie(Player owner) {
        super(RegistryHandler.TAMABLE_ZOMBIE.get(), owner.getLevel());
        this.tame(owner);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.addBehaviourGoals();
    }

    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(4, new FollowSummonerGoal(this, 1.0D, 5.0F, 2.0F, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.targetSelector.addGoal(1, new SummonerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new SummonerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    // @Override
    // protected void registerGoals() {
    // //this.goalSelector.addGoal(1, new EatBlockGoal(this));
    // this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
    // this.goalSelector.addGoal(3, new ZombieAttackGoal(this, 1.0D, false));

    // this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
    // this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
    // this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    // this.targetSelector.addGoal(1, new SummonerHurtByTargetGoal(this));
    // this.targetSelector.addGoal(2, new SummonerHurtTargetGoal(this));
    // this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    // //this.goalSelector.addGoal(4, new
    // TamedZombie.ZombieAttackTurtleEggGoal(this, 1.0D, 3));
    // }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.MOVEMENT_SPEED, (double) 0.23F).add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.ARMOR, 2.0D).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
    }

    protected int getExperienceReward(Player player) {
        return NecromancyEnchantment.spellCost;
    }

    /**
     * Called frequently so the entity can update its state every tick as required.
     * For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void aiStep() {
        if (this.isAlive()) {
            boolean flag = this.isSunSensitive() && this.isSunBurnTick();
            if (flag) {
                ItemStack itemstack = this.getItemBySlot(EquipmentSlot.HEAD);
                if (!itemstack.isEmpty()) {
                    if (itemstack.isDamageableItem()) {
                        itemstack.setDamageValue(itemstack.getDamageValue() + this.random.nextInt(2));
                        if (itemstack.getDamageValue() >= itemstack.getMaxDamage()) {
                            this.broadcastBreakEvent(EquipmentSlot.HEAD);
                            this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        }
                    }

                    flag = false;
                }

                if (flag) {
                    this.setSecondsOnFire(8);
                }
            }
        }

        super.aiStep();
    }

    protected boolean isSunSensitive() {
        return true;
    }

    public boolean doHurtTarget(Entity pEntity) {
        boolean flag = super.doHurtTarget(pEntity);
        if (flag) {
            float f = this.level.getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            if (this.getMainHandItem().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3F) {
                pEntity.setSecondsOnFire(2 * (int) f);
            }
        }

        return flag;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.ZOMBIE_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.ZOMBIE_STEP;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }

    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    /**
     * Gives armor or weapon for entity based on given DifficultyInstance
     */
    protected void populateDefaultEquipmentSlots(DifficultyInstance pDifficulty) {
        super.populateDefaultEquipmentSlots(pDifficulty);
        if (this.random.nextFloat() < (this.level.getDifficulty() == Difficulty.HARD ? 0.05F : 0.01F)) {
            int i = this.random.nextInt(3);
            if (i == 0) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
            }
        }

    }

    protected float getStandingEyeHeight(Pose pose, EntityDimensions size) {
        return 1.74F;
    }

    public boolean canHoldItem(ItemStack pStack) {
        return pStack.is(Items.EGG) && this.isPassenger() ? false : super.canHoldItem(pStack);
    }

    public boolean wantsToPickUp(ItemStack p_182400_) {
        return p_182400_.is(Items.GLOW_INK_SAC) ? false : super.wantsToPickUp(p_182400_);
    }

    /**
     * Returns the Y Offset of this entity.
     */
    public double getMyRidingOffset() {
        return -0.45D;
    }

    protected ItemStack getSkull() {
        return new ItemStack(Items.ZOMBIE_HEAD);
    }

}
