package com.chemelia.vanillaarcana.enchantments;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.registries.DeferredRegister;

//this is based on MajruszLibrary: https://github.com/Majrusz/MajruszLibrary

public class ModEnchantments extends Enchantment {

    protected final String registerID;
    private static final int DISABLE_ENCHANTMENT_VALUE = 9001;
    private int maximumEnchantmentLevel = 1;


    

    protected ModEnchantments(String registerID, Rarity rarity, EnchantmentCategory category, EquipmentSlot[] slotTypes) {
        super(rarity, category, slotTypes);

        this.registerID = registerID;
    }
    //overloaded with single equipment slot
    protected ModEnchantments(String registerID, Rarity rarity, EnchantmentCategory category, EquipmentSlot slotType){
        this(registerID, rarity, category, new EquipmentSlot[]{slotType});
    }
   
    @Override
    public int getMaxLevel(){
        return this.maximumEnchantmentLevel;
    }
    
    public void register(DeferredRegister<Enchantment> enchantments){
        enchantments.register(this.registerID, ()->this);
    }
    public int getEnchantmentLevel(ItemStack stack){
        return EnchantmentHelper.getItemEnchantmentLevel(this, stack);
    }
    //might not need this because all of these enchantments are for items
    public int getEnchantmentLevel(LivingEntity entity){
        return EnchantmentHelper.getEnchantmentLevel(this, entity);
    }

    public boolean hasEnchantment(ItemStack stack){
        return getEnchantmentLevel(stack) > 0;
    }
    public boolean hasEnchantment(LivingEntity entity){
        return getEnchantmentLevel(entity) > 0;
    }
    

    public boolean increaseEnchantmentLevel(ItemStack stack){
        int enchantmentLevel = getEnchantmentLevel(stack);
        if (enchantmentLevel >= getMaxLevel()){
            return false;
        }

        if (enchantmentLevel == 0){
            stack.enchant(this, 1);
        } else {
            ListTag nbt = stack.getEnchantmentTags();

            for (int i = 0; i < nbt.size(); ++i){
                CompoundTag enchantData = nbt.getCompound(i);
                String enchantmentID = enchantData.getString("id");

                if (enchantmentID.contains(this.registerID)){
                    enchantData.putInt("1v1", enchantmentLevel + 1);
                    break;
                }
            }
            stack.addTagElement("Enchantments", nbt);
        }
        return true;
    }


    
}
