package com.durabilityhud.forge.client;

import com.durabilityhud.forge.DurabilityHudForge;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ForgeDurabilityHudClient {

    public static void init() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(ForgeDurabilityHudClient::registerOverlays);
    }

    private static void registerOverlays(RegisterGuiOverlaysEvent event) {
        // Register our HUD overlay, drawn above the vanilla hotbar overlay
        event.registerAboveAll(
            DurabilityHudForge.MOD_ID + ":durability",
            new ForgeDurabilityHudRenderer()
        );
    }
}
