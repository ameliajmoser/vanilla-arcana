package com.chemelia.vanillaarcana.enchantments;

import com.chemelia.vanillaarcana.RegistryHandler;


import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

public class SpellEnchantment extends Enchantment {
    public int spellCooldown;
    public int spellCost;

    public SpellEnchantment(Rarity rarity, int spellCooldown, int spellCost) {
        super(rarity, RegistryHandler.WAND_CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
        this.spellCooldown = spellCooldown;
        this.spellCost = spellCost;
    }



    @Override
    protected boolean checkCompatibility(Enchantment other) {
        if (other instanceof SpellEnchantment){
            return false;
        } else {
            return super.checkCompatibility(other);
        }      
    }
}
