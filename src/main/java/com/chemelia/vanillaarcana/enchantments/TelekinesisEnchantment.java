package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.VanillaArcana;
import com.chemelia.vanillaarcana.entity.projectile.ThrownBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class TelekinesisEnchantment extends SpellEnchantment {
    private final static int SPELL_COOLDOWN = 30;
    private final static int SPELL_COST = 10;
    private final static int MAX_LEVEL = 5;
    //private final static int PROJECTILE_SPEED = 1;
    private final static int RANGE = 10;
    public static final String ID = VanillaArcana.MOD_ID + ":telekinesis";

    private ThrownBlock heldBlock;
    
    public TelekinesisEnchantment(){
        super(Rarity.RARE, SPELL_COOLDOWN, SPELL_COST);
    }

    @Override
    public int getMaxLevel(){
        return MAX_LEVEL;
    }

    private boolean setHeldBlock(ThrownBlock block){
        if (this.holdingBlock()){
            return false;
        } 
        heldBlock = block;
        block.setHeld(true);
        return true;
    }
    private void clearHeldBlock(){
        if (holdingBlock()){
            this.heldBlock.setHeld(false);
            this.heldBlock = null;
        }
    }
    private ThrownBlock getHeldBlock(){
        return this.heldBlock;
    }
    private boolean holdingBlock(){
        return this.heldBlock != null;
    }


    @Override
    public boolean handleCast(Level world, LivingEntity user, ItemStack stack){
        if (world.isClientSide()){
            return handleClientCast(world, user, stack);
        }
        int spellLevel = EnchantmentHelper.getItemEnchantmentLevel(this, stack);
        if (user instanceof Player) {
            Player player = (Player) user;
            if(player.isCrouching()){
                this.clearHeldBlock();
                return false;
            }

            if (player.totalExperience < spellCost * spellLevel && !player.isCreative()) {
                //insufficient XP
                return doSpellFailure(user, world, null, stack, spellCost, spellLevel);
            } else if (holdingBlock()){
                //sufficient XP, holding block
                throwBlock(user, spellLevel);
                return doSpellSuccess(user, world, null, stack, spellCost, spellLevel);
            } else if (grabNewBlock(world, user, spellLevel)){
                //sufficient XP, not holding block, new grab successful
                return doSpellSuccess(user, world, null, stack, spellCost, spellLevel);
            } else {
                //sufficient XP, not holding block, new grab unsuccessful
                return doSpellFailure(user, world, null, stack, spellCost, spellLevel);
            }
        }  
        return true;
    }

    private float getBlockWeight(Level world, BlockPos bPos){
        return this.getBlockWeight(world.getBlockState(bPos));
        //DIRT: 0.5
        //WOOD: 2.0
        /// ^LEVEL 1
        //STONE: 3.0F
        //
        /// ^LEVEL 2
        //IRON: 6.0F
        //END STONE: 9.0F
        //OBSIDIAN: 1200
    }

    private float getBlockWeight(BlockState block){
        float resist = block.getBlock().getExplosionResistance();
        if (resist > 0){
            return resist;
        } else return 1F;
    }

    private boolean grabNewBlock(Level world, LivingEntity user, int spellLevel){
        Vec3 look = user.getLookAngle();
        Vec3 pos = user.getEyePosition();
        Vec3 destination = pos.add(look.scale(RANGE*spellLevel));
        BlockHitResult cast = user.level.clip(new ClipContext(pos, destination, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, user));
        if (cast.getType() == Type.BLOCK){
            if (getBlockWeight(world, cast.getBlockPos()) > 3.0 * spellLevel){
                return false;
            }
            ThrownBlock block = new ThrownBlock(world, user, cast.getBlockPos());
            world.destroyBlock(cast.getBlockPos(), false);
            block.setPos(cast.getLocation());
            block.setHeld(true);
            this.setHeldBlock(block);
            world.addFreshEntity(block);
            return true;
        } else return false;
    }

    private void throwBlock(LivingEntity user, int spellLevel){
        this.getHeldBlock().setDeltaMovement(user.getLookAngle().scale(0.8 + spellLevel/5));
        //this.getHeldBlock().setGravity(getHeldBlock().getBlockWeight()/100F);
        this.clearHeldBlock();
    } 

    @Override
    protected void playSuccessSound(Level world, LivingEntity user, int spellLevel){
        if (holdingBlock()){
            world.playSound(null, user.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 0.2F);

        } else {
            super.playSuccessSound(world, user, spellLevel);
            world.playSound(null, user.blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1, 0.2F);
        }
        
    }

    @Override
    protected void spawnSuccessParticle(Level world, Vec3 pos){
        if (holdingBlock()){

        } else {
            super.spawnSuccessParticle(world, pos);
        }    
    }

    @Override
    protected void cooldownSuccess(Player player, ItemStack stack, int spellLevel){
        player.getCooldowns().addCooldown(stack.getItem(), spellCooldown/spellLevel);
    }
}
