package com.durabilityhud.fabric.client;

import com.durabilityhud.client.DurabilityHudRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class FabricDurabilityHudClient implements ClientModInitializer {

    public static final String MOD_ID = "durabilityhud";
    public static final Logger LOGGER  = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Durability HUD (Fabric 1.16.5) loaded!");
        HudRenderCallback.EVENT.register(new DurabilityHudRenderer());
    }
}
