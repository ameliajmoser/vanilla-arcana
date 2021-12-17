package com.chemelia.vanillaarcana;

import com.chemelia.vanillaarcana.enchantments.AlacrityEnchantment;
import com.chemelia.vanillaarcana.enchantments.PyrokinesisEnchantment;
import com.chemelia.vanillaarcana.enchantments.SyphonEnchantment;
import com.chemelia.vanillaarcana.item.custom.WandItem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RegistryHandler {


    //Sounds
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, VanillaArcana.MOD_ID);
    public static final RegistryObject<SoundEvent> SPELL_CAST = SOUNDS.register("item.wand.cast", 
        () -> new SoundEvent(new ResourceLocation(VanillaArcana.MOD_ID, "item.wand.cast")));
    public static final RegistryObject<SoundEvent> SPELL_FAIL = SOUNDS.register("item.wand.fail", 
        () -> new SoundEvent(new ResourceLocation(VanillaArcana.MOD_ID, "item.wand.fail")));
    public static final RegistryObject<SoundEvent> SPELL_SAP = SOUNDS.register("item.wand.sap", 
        () -> new SoundEvent(new ResourceLocation(VanillaArcana.MOD_ID, "item.wand.sap")));


    //Items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VanillaArcana.MOD_ID);

    public static final RegistryObject<Item> AMETHYST_WAND = ITEMS.register("amethyst_wand", () -> new WandItem(new Item.Properties()
        .stacksTo(1)
        .tab(CreativeModeTab.TAB_TOOLS)));
    public static final RegistryObject<Item> NETHER_WAND = ITEMS.register("nether_wand", () -> new WandItem(new Item.Properties()
        .stacksTo(1)
        .tab(CreativeModeTab.TAB_TOOLS)));

    

    //Enchantments
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, VanillaArcana.MOD_ID);
    public static final EnchantmentCategory WAND_CATEGORY = EnchantmentCategory.create("wand", item -> item instanceof WandItem);

    //public static final RegistryObject<Enchantment> ALACRITY = ENCHANTMENTS.register("alacrity", AlacrityEnchantment::new);
    
    public static final RegistryObject<Enchantment> PYROKINESIS = ENCHANTMENTS.register("pyrokinesis", PyrokinesisEnchantment::new);
    public static final RegistryObject<Enchantment> SYPHON = ENCHANTMENTS.register("syphon", SyphonEnchantment::new);
    





    public static void register(IEventBus eventBus) { //Add the list of our items to the deferred register
        SOUNDS.register(eventBus);
        ITEMS.register(eventBus);
        ENCHANTMENTS.register(eventBus);
    }
}
