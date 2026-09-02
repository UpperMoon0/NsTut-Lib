package com.nstut.nstutlib;

import com.nstut.nstutlib.creative_tabs.CreativeTabRegistries;
import com.nstut.nstutlib.items.ItemRegistries;
import com.nstut.nstutlib.network.PacketRegistries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NsTutLib.MOD_ID)
public class NsTutLib {
    public static final String MOD_ID = "nstutlib";
    public static boolean IS_DEV_ENV;

    public NsTutLib(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        ItemRegistries.ITEMS.register(modEventBus);
        CreativeTabRegistries.CREATIVE_MODE_TABS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(PacketRegistries::register);
    }
}
