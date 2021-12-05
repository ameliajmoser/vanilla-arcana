package com.chemelia.vanillaarcana.item;

import com.chemelia.vanillaarcana.VanillaArcana;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, VanillaArcana.MOD_ID); //list of created items - forge handles where things go


    public static final RegistryObject<Item> AMETHYST_WAND = ITEMS.register("amethyst_wand", 
        () -> new Item(new Item.Properties().tab(CreativeModeTab.TAB_TOOLS)));
    

    public static void register(IEventBus eventBus) { //Add the list of our items to the deferred register
        ITEMS.register(eventBus);
    }
}