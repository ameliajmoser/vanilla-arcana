package com.chemelia.vanillaarcana.entity.projectile;

import com.chemelia.vanillaarcana.RegistryHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

public class ThrownBlock extends ThrowableItemProjectile implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(ThrownBlock.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> HELD = SynchedEntityData.defineId(ThrownBlock.class, EntityDataSerializers.BOOLEAN);

    public int groundTime = 0;
    public boolean touchedGround = false;
    private float gravity = 0.03F;

    //only client
    private float xRotInc;
    private float yRotInc;
    public Lazy<Integer> light = Lazy.of(() -> {
        Item item = this.getItem().getItem();
        if (item instanceof BlockItem) {
            Block b = ((BlockItem) item).getBlock();
            return b.getLightEmission(b.defaultBlockState(), this.level, this.blockPosition());
        }
        return 0;
    });

    public ThrownBlock(Level world, LivingEntity thrower, ItemStack item) {
        super(RegistryHandler.THROWN_BLOCK.get(), thrower, world);
        this.setItem(item);

        this.yRotInc = (this.random.nextBoolean() ? 1 : -1) * (float) (4 * this.random.nextGaussian() + 7);
        this.xRotInc = (this.random.nextBoolean() ? 1 : -1) * (float) (4 * this.random.nextGaussian() + 7);
        this.setXRot(this.random.nextFloat() * 360);
        this.setYRot(this.random.nextFloat() * 360);
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }

    public ThrownBlock(PlayMessages.SpawnEntity spawnEntity, Level world) {
        this(RegistryHandler.THROWN_BLOCK.get(), world);
    }

    //client factory
    public ThrownBlock(EntityType<ThrownBlock> type, Level world) {
        super(type, world);
    }

    public ThrownBlock(Level world, LivingEntity thrower, BlockPos bPos){
        this(world, thrower, new ItemStack(world.getBlockState(bPos).getBlock().asItem()));
    }

    public boolean isNoPhysics(){
        if (!this.level.isClientSide()){
            return this.noPhysics;
        } else {
            return true;
        }
    }

    @Override
    protected float getGravity(){
        return this.gravity;
    }
    public void setGravity(float grav){
        if (grav == 0){
            grav = 0.01F;
        }
        this.gravity = grav;
    }

    @Override
    public boolean canBeCollidedWith(){
        return !this.isHeld() && this.isAlive();
    }

    @Override
    public void push(Entity entity){
        if (entity != this.getOwner()){
            super.push(entity);
        }
    }


    public float getBlockWeight(){
        float resist = this.getBlock().getExplosionResistance();
        if (resist > 0){
            return resist;
        } else return 1F;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HELD, false);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setHeld(tag.getBoolean("Held"));
    }

    public void setHeld(Boolean bool){
        this.entityData.set(HELD, bool);
    }
    public boolean isHeld(){
        return this.entityData.get(HELD);
    }

    protected BlockState getBlockState(){
        Item item = this.getItem().getItem();
        if (this.getItem().getItem() instanceof BlockItem){
            return ((BlockItem) item).getBlock().defaultBlockState();
        } else {
            return this.getDefaultBlockState();
        }
    } 

    private Block getBlock(){
        return this.getBlockState().getBlock();
    }


    @Override
    protected Item getDefaultItem() {
        return Items.STONE;
    }

    protected BlockState getDefaultBlockState(){
        BlockItem item = (BlockItem) this.getDefaultItem();
        return item.getBlock().defaultBlockState();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.isHeld()){
            return;
        }
        //super.onHitEntity(result);
        Entity hitEntity = result.getEntity();
        if (hitEntity == this.getOwner()){
            return;
        }
        hitEntity.hurt(DamageSource.thrown(this, this.getOwner()), 2.0F + this.getBlockWeight()/2);
        this.level.addDestroyBlockEffect(new BlockPos(result.getLocation()), this.getBlockState());
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS) && !this.level.isClientSide()){
            this.spawnAtLocation(this.getItem());
        }
        this.remove(RemovalReason.DISCARDED);
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        //can only place when first hits
        //if (this.touchedGround) return;
        Entity owner = this.getOwner();
        boolean success = false;
        if (owner instanceof Player player && player.getAbilities().mayBuild) {
            ItemStack stack = this.getItem();
            Item item = stack.getItem();
            //block override. mimic forge event
            PlayerInteractEvent.RightClickBlock blockPlaceEvent = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, hit.getBlockPos(), hit);
            //ItemsOverrideHandler.tryPerformClickedBlockOverride(blockPlaceEvent, stack, true);
            if (blockPlaceEvent.isCanceled() && blockPlaceEvent.getCancellationResult().consumesAction()) {
                success = true;
            }
            if (!success && item instanceof BlockItem blockItem) {
                BlockPlaceContext ctx = new BlockPlaceContext(this.level, player, InteractionHand.MAIN_HAND, this.getItem(), hit);
                success = blockItem.place(ctx).consumesAction();
            }
            if (success){
                this.remove(RemovalReason.DISCARDED);
            }

        }
    }

    @Override
    public void tick() {
        if (this.getOwner() != null && this.isHeld()){
            Entity owner = this.getOwner();
            if (owner instanceof Player){
                if (EnchantmentHelper.getItemEnchantmentLevel(RegistryHandler.TELEKINESIS.get(), ((Player) owner).getItemInHand(InteractionHand.MAIN_HAND)) < 1){
                    this.setHeld(false);
                }
            }
            //no rotation...?
            //this.setRot(owner.getRotationVector().x, owner.getRotationVector().y);
            this.xOld = this.getX();
			this.yOld = this.getY();
			this.zOld = this.getZ();
            if (this.level.isClientSide()){
                this.setSharedFlag(6, this.isCurrentlyGlowing());
            }

            Vec3 holdPosition = owner.getEyePosition().add(owner.getLookAngle());
            
            if (owner.isAlive() && this.distanceToSqr(holdPosition) > 0.1){
                Vec3 delta = this.position().vectorTo(holdPosition);
                this.setDeltaMovement(delta.scale(0.1));
            } else {
                //this.setRot(owner.getXRot(), owner.getYRot());
                this.setDeltaMovement(0,0,0);
            }

            this.baseTick();
        }
        if (this.isNoPhysics()) {
            int i = 0;
            Entity owner = this.getOwner();
            if (i > 0 && this.isAcceptableReturnOwner(owner)) {
                Vec3 vector3d = new Vec3(owner.getX() - this.getX(), owner.getEyeY() - this.getY(), owner.getZ() - this.getZ());
                this.setPosRaw(this.getX(), this.getY() + vector3d.y * 0.015D * (double) i, this.getZ());
                if (this.level.isClientSide) {
                    this.yOld = this.getY();
                }

                double d0 = 0.05D * (double) i;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(vector3d.normalize().scale(d0)));
            }
        }
        super.tick();
    }

    private boolean isAcceptableReturnOwner(Entity owner) {
        if (owner != null && owner.isAlive()) {
            return !(owner instanceof ServerPlayer) || !owner.isSpectator();
        } else {
            return false;
        }
    }

    /*
    //this is for players to catch 
    @Override
    public void playerTouch(Player playerEntity) {
        if (false || this.touchedGround) {

            boolean success = playerEntity.getAbilities().instabuild || playerEntity.getInventory().add(this.getItem());

            if (!this.level.isClientSide) {
                if (!success) {
                    this.spawnAtLocation(this.getItem(), 0.1f);
                }
            } else {
                this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, (this.random.nextFloat() - this.random.nextFloat()) * 1.4F + 2.0F, false);
            }
            this.remove(RemovalReason.DISCARDED);
        }
    }
    */
    
    public boolean hasReachedEndOfLife() {
        if (this.isNoGravity() && this.getDeltaMovement().lengthSqr() < 0.005) return true;
        return !this.isNoPhysics();
    }

    public void reachedEndOfLife() {
        if (this.isAcceptableReturnOwner(this.getOwner())) {
            // this.setNoPhysics(true);
            this.groundTime = 0;
        } else {
            this.spawnAtLocation(this.getItem(), 0.1f);
            this.remove(RemovalReason.DISCARDED);
        }
    }

    public void setNoPhysics(boolean noPhysics) {
        this.noPhysics = noPhysics;
        this.setFlag(2, noPhysics);
    }

    private void setFlag(int id, boolean value) {
        byte b0 = this.entityData.get(ID_FLAGS);
        if (value) {
            this.entityData.set(ID_FLAGS, (byte) (b0 | id));
        } else {
            this.entityData.set(ID_FLAGS, (byte) (b0 & ~id));
        }
    }


    @Override
    protected void updateRotation() {
        // if (!this.isNoPhysics()) {
        //     this.xRotO = this.getXRot();
        //     this.yRotO = this.getYRot();
        //     this.setXRot(this.getXRot() + xRotInc);
        //     this.setYRot(this.getYRot() + yRotInc);
        // } else {
            super.updateRotation();
        // }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        Entity entity = this.getOwner();
        int id = -1;
        if (entity != null) {
            id = entity.getId();
        }
        buffer.writeInt(id);
        buffer.writeFloat(this.xRotInc);
        buffer.writeFloat(this.yRotInc);
        buffer.writeFloat(this.getXRot());
        buffer.writeFloat(this.getYRot());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf buffer) {
        int id = buffer.readInt();
        if (id != -1) {
            this.setOwner(this.level.getEntity(id));
        }
        this.xRotInc = buffer.readFloat();
        this.yRotInc = buffer.readFloat();
        this.setXRot(buffer.readFloat());
        this.setYRot(buffer.readFloat());
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }
}
