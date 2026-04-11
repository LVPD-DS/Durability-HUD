package com.durabilityhud.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class DurabilityHudClient implements ClientModInitializer {

    public static final String MOD_ID = "durabilityhud";
    // В 1.16.5 используется Log4j, а не SLF4J
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("Durability HUD mod loaded! (1.16.5)");
        HudRenderCallback.EVENT.register(new DurabilityHudRenderer());
    }
}
