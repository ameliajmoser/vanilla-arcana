package com.chemelia.vanillaarcana.item.custom;


import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.enchantments.AlacrityEnchantment;
import com.chemelia.vanillaarcana.enchantments.PyrokinesisEnchantment;
import com.chemelia.vanillaarcana.enchantments.SyphonEnchantment;
import com.chemelia.vanillaarcana.enchantments.WarpEnchantment;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class WandItem extends Item {

    public WandItem(Properties pProperties) {
        super(pProperties);
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack){
        return true;
    }
    @Override
    public int getItemEnchantability(ItemStack stack){
        return 30;
    }

    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand usedHand){
        ItemStack stack = player.getItemInHand(usedHand);
        if (!(stack.getItem() instanceof WandItem)){
            return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        }

        AlacrityEnchantment alacrity = (AlacrityEnchantment) RegistryHandler.ALACRITY.get();
        int alacrityLevel = EnchantmentHelper.getItemEnchantmentLevel(alacrity, stack);
        //make an array of enchantments and reduce their cooldown?

    
        PyrokinesisEnchantment pyrokinesis = (PyrokinesisEnchantment) RegistryHandler.PYROKINESIS.get();
        int pyroLevel = EnchantmentHelper.getItemEnchantmentLevel(pyrokinesis, stack);
        if (pyroLevel > 0 && pyrokinesis.handleCast(world,player,stack)){
            return InteractionResultHolder.success(stack);
        }

        SyphonEnchantment syphon = (SyphonEnchantment) RegistryHandler.SYPHON.get();
        int syphonLevel = EnchantmentHelper.getItemEnchantmentLevel(syphon, stack);
        if (syphonLevel > 0 && syphon.handleCast(world,player,stack)){
            return InteractionResultHolder.success(stack);
        }

        WarpEnchantment warp = (WarpEnchantment) RegistryHandler.WARP.get();
        int warpLevel = EnchantmentHelper.getItemEnchantmentLevel(warp, stack);
        if (warpLevel > 0 && warp.handleCast(world,player,stack)){
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.fail(player.getItemInHand(usedHand));
    }

    
}
