package com.nstut.nstutlib.fabric.client;

import com.nstut.nstutlib.NsTutLib;
import net.fabricmc.api.ClientModInitializer;

public class NsTutLibFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Client-specific initialization code here
        NsTutLib.registerScreens(); // Call the new method to register screens
        NsTutLib.getLogger().info("NsTutLib client setup complete (via ClientModInitializer).");
    }
}
