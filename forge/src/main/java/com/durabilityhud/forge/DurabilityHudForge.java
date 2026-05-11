package com.durabilityhud.forge;

import com.durabilityhud.forge.client.ForgeDurabilityHudClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(DurabilityHudForge.MOD_ID)
public class DurabilityHudForge {

    public static final String MOD_ID = "durabilityhud";

    public DurabilityHudForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ForgeDurabilityHudClient.init();
        }
    }
}
