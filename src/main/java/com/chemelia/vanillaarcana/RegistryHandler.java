package com.chemelia.vanillaarcana;

import com.chemelia.vanillaarcana.enchantments.PyrokinesisEnchantment;
import com.chemelia.vanillaarcana.item.custom.WandItem;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RegistryHandler {

    //Items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VanillaArcana.MOD_ID);

    public static final RegistryObject<Item> AMETHYST_WAND = ITEMS.register("amethyst_wand", 
        () -> new Item(new Item.Properties()
        .tab(CreativeModeTab.TAB_TOOLS)
        .stacksTo(1)
        ));

    public static void register(IEventBus eventBus) { //Add the list of our items to the deferred register
        ITEMS.register(eventBus);
        ENCHANTMENTS.register(eventBus);
    }


    //Enchantments
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, VanillaArcana.MOD_ID);

    public static final RegistryObject<Enchantment> PYROKINESIS = ENCHANTMENTS.register("pyrokinesis", () -> new PyrokinesisEnchantment());
    
    
    public static final EnchantmentCategory WAND_CATEGORY = EnchantmentCategory.create("wand", null);



}
