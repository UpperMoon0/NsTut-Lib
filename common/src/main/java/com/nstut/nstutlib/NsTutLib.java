package com.nstut.nstutlib;

import com.mojang.logging.LogUtils;
import com.nstut.nstutlib.creative_tabs.CreativeTabRegistries;
import com.nstut.nstutlib.items.ItemRegistries;
import com.nstut.nstutlib.network.PacketRegistries;
import com.nstut.nstutlib.core.registry.NsTutLibBlockEntities;
import com.nstut.nstutlib.core.registry.NsTutLibBlocks;
import com.nstut.nstutlib.core.registry.NsTutLibMenuTypes;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent; 
import dev.architectury.registry.menu.MenuRegistry;
import com.nstut.nstutlib.views.screens.HatchScreen;
import org.slf4j.Logger;

public class NsTutLib
{
    public static final String MOD_ID = "nstutlib";
    public static boolean IS_DEV_ENV;
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init()
    {
        LOGGER.info("NsTutLib Initializing...");

        // Load config
        Config.onLoad(); 

        // Register items and creative tabs
        // These methods will need to be adapted in their respective classes to use Architectury's registration
        ItemRegistries.register(); 
        CreativeTabRegistries.register(); 

        // Register network packets
        PacketRegistries.register();

        // Register blocks and block entities
        NsTutLibBlocks.register();
        NsTutLibBlockEntities.register();
        NsTutLibMenuTypes.register();

        // Register common events
        LifecycleEvent.SERVER_STARTING.register(server -> {
            // Code to run when the server is starting
            LOGGER.info("NsTutLib: Server starting");
        });

        LOGGER.info("NsTutLib Initialization Complete.");
    }

    public static void registerScreens() {
        MenuRegistry.registerScreenFactory(NsTutLibMenuTypes.HATCH_MENU.get(), HatchScreen::new);
        LOGGER.info("NsTutLib screen factories registered.");
    }

    public static void initClient()
    {
        ClientLifecycleEvent.CLIENT_SETUP.register(minecraft -> { 
            // Screen factory registration was moved to registerScreens()
            LOGGER.info("NsTutLib client setup complete (via ClientLifecycleEvent.CLIENT_SETUP).");
        });
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
