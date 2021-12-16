package com.chemelia.vanillaarcana.item.custom;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.enchantments.PyrokinesisEnchantment;


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
        return 20;
    }
    

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand usedHand){
        ItemStack stack = player.getItemInHand(usedHand);
        if (!(stack.getItem() instanceof WandItem)){
            return InteractionResultHolder.pass(player.getItemInHand(usedHand));
        }

        PyrokinesisEnchantment pyrokinesis = (PyrokinesisEnchantment) RegistryHandler.PYROKINESIS.get();
        int pyroLevel = EnchantmentHelper.getItemEnchantmentLevel(pyrokinesis, stack);
        if (pyroLevel > 0 && pyrokinesis.handleCast(world,player,stack)){
            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(player.getItemInHand(usedHand));
    }

    
}
