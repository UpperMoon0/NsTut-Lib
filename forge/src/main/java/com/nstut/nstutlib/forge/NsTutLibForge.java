package com.nstut.nstutlib.forge;

import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.items.ItemRegistries;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NsTutLib.MOD_ID)
public class NsTutLibForge {
    public NsTutLibForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus(); // Get IEventBus

        // Register the DeferredRegister to the mod event bus
        modEventBus.register(ItemRegistries.ITEMS);
        EventBuses.registerModEventBus(NsTutLib.MOD_ID, modEventBus);

        NsTutLib.init(); // This will call the register methods in ItemRegistries and CreativeTabRegistries
    }
}
