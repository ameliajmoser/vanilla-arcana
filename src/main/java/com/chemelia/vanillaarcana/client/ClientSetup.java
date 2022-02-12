package com.chemelia.vanillaarcana.client;

import com.chemelia.vanillaarcana.RegistryHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientSetup {

    public ClientSetup() {
        Minecraft minecraft = Minecraft.getInstance();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::onClientSetup);
    }
    
    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event){
        ItemBlockRenderTypes.setRenderLayer(RegistryHandler.AEGIS_BLOCK.get(), RenderType.translucent());
    }
}
