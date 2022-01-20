package com.chemelia.vanillaarcana.entity.projectile;

import java.util.UUID;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.item.WandItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.core.particles.BlockParticleOption;

public class OldThrownBlock extends ThrowableItemProjectile implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Boolean> HELD = SynchedEntityData.defineId(OldThrownBlock.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(OldThrownBlock.class, EntityDataSerializers.BYTE);

    private BlockState block = Blocks.GRASS_BLOCK.defaultBlockState();
    private UUID ownerUUID;
    public LivingEntity owner; 
    

    public OldThrownBlock(EntityType<? extends ThrowableItemProjectile> type, Level world) {
        super(type, world);
    }

    public OldThrownBlock(EntityType<? extends OldThrownBlock> type, LivingEntity owner, Level world) {
        super(type, owner, world);
    }

    public OldThrownBlock(EntityType<? extends OldThrownBlock> type, double posX, double posY,
            double posZ, Level world) {
        super(type, posX, posY, posZ, world);
    }

    public OldThrownBlock(Level world, Entity owner, BlockState bState) {
        this(RegistryHandler.THROWN_BLOCK.get(), world);
        this.block = bState;
        this.setOwner(owner);
    }

    @Override
    protected void defineSynchedData(){
        super.defineSynchedData();
        this.entityData.define(HELD, false);
    }

    public BlockState getBlockState(){
        return this.block;
    }

    public LivingEntity getOwner() {

        return this.owner;
    }

    public void setOwner(LivingEntity owner){
        this.owner = owner;
        this.ownerUUID = owner.getUUID();
    }
    public boolean isHeld(){
        return this.entityData.get(HELD);
    }
    private void setHeld(Boolean bool){
        this.entityData.set(HELD, bool);
    }

    @Override
    protected float getGravity(){
        return 0.02F;
    }

    @Override
    public boolean canBeCollidedWith(){
        return this.isHeld() && this.isAlive();
    }

    @Override
    public void push(Entity entity){
        if (!entity.is(this.owner)){
            super.push(entity);
        }
    }

    @Override
    public void handleEntityEvent(byte id){
        if (id == 3) {
			for (int i = 0; i < 60; ++i) {
				double x = this.getX() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth();
				double y = this.getY() + 0.5D + (double)(this.random.nextFloat() * this.getBbHeight());
				double z = this.getZ() + (double)(this.random.nextFloat() * this.getBbWidth() * 2.0F) - (double)this.getBbWidth();
				double motx = (double)((this.random.nextFloat() - this.random.nextFloat()) * 3.0F);
				double moty = (double)(0.5F + this.random.nextFloat() * 2.0F);
				double motz = (double)((this.random.nextFloat() - this.random.nextFloat()) * 3.0F);
				this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, this.block), x, y, z, motx, moty, motz);
			}
		}
    }
    @Override
	public void tick() {
		if (this.isHeld()) {
			this.xOld = this.getX();
			this.yOld = this.getY();
			this.zOld = this.getZ();

			if (this.level.isClientSide()) {
				this.setSharedFlag(6, this.isCurrentlyGlowing());
			}

			this.baseTick();

			if (this.owner == null || !this.owner.isAlive() || this.owner.isSpectator()) {
				this.setHeld(false);
			} else {
				Vec3 vec = this.owner.getLookAngle();
				double x = this.owner.getX() + vec.x * 1.6D - this.getX();
				double y = this.owner.getY() + this.owner.getEyeHeight() + vec.y * 1.6D - this.getY();
				double z = this.owner.getZ() + vec.z * 1.6D - this.getZ();
				float offset = 0.6F;
				this.setDeltaMovement(x * offset, y * offset, z * offset);
				this.move(MoverType.SELF, this.getDeltaMovement());
			}
		} else {
			super.tick();
		}
	}
    
    @Override
    public InteractionResult interact(Player player, InteractionHand hand){
        if (player.isCrouching()){
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof WandItem){
            if (this.isHeld() && this.owner.is(player)){
                if (this.level.isClientSide()){
                    this.throwBlock(player);
                }
                return InteractionResult.SUCCESS;
            }
        } 
        return InteractionResult.PASS;
    }

    private void throwBlock(LivingEntity thrower){
        Vec3 look = thrower.getLookAngle();
        this.setRot(thrower.getXRot(), thrower.getYRot());
        this.setDeltaMovement(look);
        this.shoot(this.getDeltaMovement().x, this.getDeltaMovement().y, this.getDeltaMovement().z, 1.4F, 1.0F);
    }


    @Override
    protected void onHit(HitResult result){
        if (result.getType() == HitResult.Type.BLOCK){
            BlockPos pos = ((BlockHitResult) result).getBlockPos();
            if (this.level.getBlockState(pos).getCollisionShape(this.level, pos).isEmpty()){
                return;
            }
        }
        if (!this.level.isClientSide()){
            return;
        }
        Item item = this.block.getBlock().asItem();
        boolean ownerCanGrief = (this.owner == null || !(this.owner instanceof Mob) || net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this.owner));
        if (result.getType() == HitResult.Type.BLOCK){
            this.remove(RemovalReason.DISCARDED);
            if (ownerCanGrief){
                BlockHitResult blockResult = (BlockHitResult) result;
                Direction dir = blockResult.getDirection();
                DirectionalPlaceContext context = new DirectionalPlaceContext(this.level, blockResult.getBlockPos().relative(dir), dir, new ItemStack(item), dir.getOpposite());
                if (item instanceof BlockItem && ((BlockItem) item).place(context) == InteractionResult.SUCCESS){
                    return;
                }
            }
            this.level.addDestroyBlockEffect(this.blockPosition(), this.block);
            if (ownerCanGrief && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)){
                this.spawnAtLocation(item);
            }
        } else if (result.getType() == HitResult.Type.ENTITY){
            Entity hitEntity = ((EntityHitResult) result).getEntity();
            if (hitEntity.hurt(DamageSource.thrown(this, this.owner), 4F)){
                if (this.owner != null){
                    this.doEnchantDamageEffects(this.owner, hitEntity);
                }
            }
            this.level.addDestroyBlockEffect(this.blockPosition(), this.block);
            if (ownerCanGrief && this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)){
                this.spawnAtLocation(item);
            }
            this.remove(RemovalReason.DISCARDED);
        }

        for (Entity entity : this.level.getEntities(this, this.getBoundingBox().inflate(2.0D))){
            if (entity.canBeCollidedWith() && !entity.is(this.owner) && this.distanceToSqr(entity) <= 4.0D){
                entity.hurt(DamageSource.indirectMagic(this, this.owner), 7.0F);
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        if (tag.contains("BlockState", CompoundTag.TAG_COMPOUND)){
            this.block = NbtUtils.readBlockState(tag.getCompound("BlockState"));
        }

        this.setHeld(tag.getBoolean("Held"));
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(Block.getId(block));
        buffer.writeVarInt(this.owner == null ? 0 : this.owner.getId());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.block = Block.stateById(additionalData.readVarInt());
        Entity entity = this.level.getEntity(additionalData.readVarInt());
        this.owner = entity instanceof LivingEntity ? (LivingEntity)entity : null;
    }

    @Override
    protected Item getDefaultItem() {
        return this.block.getBlock().asItem();
    }

    @Override
    public Packet<?> getAddEntityPacket(){
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}