package com.nstut.nstutlib;

import com.nstut.nstutlib.creative_tabs.CreativeTabRegistries;
import com.nstut.nstutlib.items.ItemRegistries;
import com.nstut.nstutlib.network.PacketRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(NsTutLib.MOD_ID)
public class NsTutLib {
    public static final String MOD_ID = "nstutlib";
    public static boolean IS_DEV_ENV;

    public NsTutLib(IEventBus modEventBus, ModContainer modContainer) {
        ItemRegistries.ITEMS.register(modEventBus);
        CreativeTabRegistries.CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(Config::onLoad);
        modEventBus.addListener(PacketRegistries::register);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
