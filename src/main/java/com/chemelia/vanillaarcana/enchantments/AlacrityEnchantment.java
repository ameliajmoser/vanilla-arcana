package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.RegistryHandler;
import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

public class AlacrityEnchantment extends Enchantment {
    public static final String ID = VanillaArcana.MOD_ID + ":alacrity";
    
    public AlacrityEnchantment() {
        super(Rarity.COMMON, RegistryHandler.WAND_CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
        
    }

    @Override
    public int getMaxLevel(){
        return 4;
    }
    

    //access all the other spell enchantments on this item and decrease their cooldownTime

}
