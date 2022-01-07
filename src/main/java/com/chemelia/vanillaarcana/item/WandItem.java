package com.chemelia.vanillaarcana.item;

import com.chemelia.vanillaarcana.enchantments.SpellEnchantment;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

public class WandItem extends Item {   
    public final int enchantability; 

    public WandItem(Properties pProperties) {
        this(pProperties, 15);
    }
    public WandItem(Properties pProperties, int enchantability) {
        super(pProperties);
        this.enchantability = enchantability;
    }

    @Override
    public boolean isEnchantable(ItemStack stack){
        return true;
    }
    @Override
    public int getItemEnchantability(ItemStack stack){
        return this.enchantability;
    }

    
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand usedHand){
        ItemStack stack = player.getItemInHand(usedHand);
        if (!(stack.getItem() instanceof WandItem)){
            return InteractionResultHolder.fail(player.getItemInHand(usedHand));
        }

        for (Enchantment enchant : EnchantmentHelper.getEnchantments(stack).keySet()){
            if (enchant instanceof SpellEnchantment && ((SpellEnchantment) enchant).handleCast(world,player,stack)){
                return InteractionResultHolder.success(stack);
            }
        }
        
        return InteractionResultHolder.fail(player.getItemInHand(usedHand));
    }

    
}
