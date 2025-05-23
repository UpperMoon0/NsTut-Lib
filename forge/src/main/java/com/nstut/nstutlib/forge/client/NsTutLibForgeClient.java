package com.nstut.nstutlib.forge.client;

import com.nstut.nstutlib.NsTutLib;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = NsTutLib.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class NsTutLibForgeClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            NsTutLib.registerScreens();
            NsTutLib.getLogger().info("NsTutLib client setup complete (via FMLClientSetupEvent).");
        });
    }
}
