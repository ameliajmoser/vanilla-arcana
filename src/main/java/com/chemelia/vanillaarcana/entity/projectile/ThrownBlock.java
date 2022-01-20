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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
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
import net.minecraft.world.level.block.Blocks;
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
    private static final EntityDataAccessor<Byte> ID_LOYALTY = SynchedEntityData.defineId(ThrownBlock.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> ID_FLAGS = SynchedEntityData.defineId(ThrownBlock.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> HELD = SynchedEntityData.defineId(ThrownBlock.class, EntityDataSerializers.BOOLEAN);

    public int groundTime = 0;
    public boolean touchedGround = false;

    //only client
    public int clientSideReturnTridentTickCount;
    private float xRotInc;
    private float yRotInc;
    private float particleCooldown = 0;
    public Lazy<Integer> light = Lazy.of(() -> {
        Item item = this.getItem().getItem();
        if (item instanceof BlockItem) {
            Block b = ((BlockItem) item).getBlock();
            return b.getLightEmission(b.defaultBlockState(), this.level, this.blockPosition());
        }
        return 0;
    });

    public ThrownBlock(LivingEntity thrower, Level world, ItemStack item, ItemStack wand) {
        super(RegistryHandler.THROWN_BLOCK.get(), thrower, world);
        this.setItem(item);
        this.entityData.set(ID_LOYALTY, (byte) EnchantmentHelper.getLoyalty(wand));


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

    public boolean isNoPhysics(){
        if (!this.level.isClientSide()){
            return this.noPhysics;
        } else {
            return true;
        }
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
        if (!entity.is(this.getOwner())){
            super.push(entity);
        }
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_LOYALTY, (byte) 0);
        this.entityData.define(HELD, false);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(ID_LOYALTY, tag.getByte("Loyalty"));
        this.entityData.set(ID_LOYALTY, tag.getByte("Loyalty"));
        this.setHeld(tag.getBoolean("Held"));
    }
    private void setHeld(Boolean bool){
        this.entityData.set(HELD, bool);
    }
    public boolean isHeld(){
        return this.entityData.get(HELD);
    }

    protected BlockState getBlock(){
        Item item = this.getItem().getItem();
        if (this.getItem().getItem() instanceof BlockItem){
            return ((BlockItem) item).getBlock().defaultBlockState();
        } else {
            return this.getDefaultBlockState();
        }
    } 

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("Loyalty", this.entityData.get(ID_LOYALTY));
    }

    public void setLoyalty(ItemStack stack) {
        this.entityData.set(ID_LOYALTY, (byte) EnchantmentHelper.getLoyalty(stack));
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
        //super.onHitEntity(result);
        Entity hitEntity = result.getEntity();
        //TODO: tune block damage
        hitEntity.hurt(DamageSource.thrown(this, this.getOwner()), 4F);
        this.level.addDestroyBlockEffect(new BlockPos(result.getLocation()), this.getBlock());
        if (this.level.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)){
            this.spawnAtLocation(this.getItem());
        }
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
        if (this.isHeld()){
            //no rotation...?
            this.xOld = this.getX();
			this.yOld = this.getY();
			this.zOld = this.getZ();
            if (this.level.isClientSide()){
                this.setSharedFlag(6, this.isCurrentlyGlowing());
            }

            this.baseTick();
        }
        if (this.isNoPhysics()) {
            int i = this.entityData.get(ID_LOYALTY);
            Entity owner = this.getOwner();
            if (i > 0 && this.isAcceptableReturnOwner(owner)) {
                Vec3 vector3d = new Vec3(owner.getX() - this.getX(), owner.getEyeY() - this.getY(), owner.getZ() - this.getZ());
                this.setPosRaw(this.getX(), this.getY() + vector3d.y * 0.015D * (double) i, this.getZ());
                if (this.level.isClientSide) {
                    this.yOld = this.getY();
                }

                double d0 = 0.05D * (double) i;
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95D).add(vector3d.normalize().scale(d0)));

                ++this.clientSideReturnTridentTickCount;
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
    
    public boolean hasReachedEndOfLife() {
        if (this.isNoGravity() && this.getDeltaMovement().lengthSqr() < 0.005) return true;
        return !this.isNoPhysics();
    }

    public void reachedEndOfLife() {
        if (this.entityData.get(ID_LOYALTY) != 0 && this.isAcceptableReturnOwner(this.getOwner())) {
            this.setNoPhysics(true);
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

        if (!this.isNoPhysics()) {
            this.xRotO = this.getXRot();
            this.yRotO = this.getYRot();
            this.setXRot(this.getXRot() + xRotInc);
            this.setYRot(this.getYRot() + yRotInc);
            this.particleCooldown++;
        } else {
            super.updateRotation();
        }
    }

    // @Override
    // public void spawnTrailParticles(Vec3 currentPos, Vec3 newPos) {
    //     if (!this.isNoPhysics()) {
    //         double d = this.getDeltaMovement().length();
    //         if (this.tickCount > 1 && d * this.tickCount > 1.5) {
    //             if (this.isNoGravity()) {

    //                 Vec3 rot = new Vec3(0.325, 0, 0).yRot(this.tickCount * 0.32f);

    //                 Vec3 movement = this.getDeltaMovement();
    //                 Vec3 offset = changeBasis(movement, rot);

    //                 double px = newPos.x + offset.x;
    //                 double py = newPos.y + offset.y; //+ this.getBbHeight() / 2d;
    //                 double pz = newPos.z + offset.z;

    //                 movement = movement.scale(0.25);
    //                 this.level.addParticle(ModRegistry.STASIS_PARTICLE.get(), px, py, pz, movement.x, movement.y, movement.z);
    //             } else {
    //                 double interval = 4 / (d * 0.95 + 0.05);
    //                 if (this.particleCooldown > interval) {
    //                     this.particleCooldown -= interval;
    //                     double x = currentPos.x;
    //                     double y = currentPos.y;//+ this.getBbHeight() / 2d;
    //                     double z = currentPos.z;
    //                     this.level.addParticle(ModRegistry.SLINGSHOT_PARTICLE.get(), x, y, z, 0, 0.01, 0);
    //                 }
    //             }
    //         }
    //     }
    // }

    private Vec3 changeBasis(Vec3 dir, Vec3 rot) {
        Vec3 y = dir.normalize();
        Vec3 x = new Vec3(y.y, y.z, y.x).normalize();
        Vec3 z = y.cross(x).normalize();
        return x.scale(rot.x).add(y.scale(rot.y)).add(z.scale(rot.z));
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
