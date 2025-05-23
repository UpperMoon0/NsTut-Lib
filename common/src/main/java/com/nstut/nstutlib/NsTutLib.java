package com.nstut.nstutlib;

import com.mojang.logging.LogUtils;
import com.nstut.nstutlib.creative_tabs.CreativeTabRegistries;
import com.nstut.nstutlib.items.ItemRegistries;
import com.nstut.nstutlib.network.PacketRegistries;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent; 
import org.slf4j.Logger;

public class NsTutLib
{
    public static final String MOD_ID = "nstutlib";
    public static boolean IS_DEV_ENV;
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init()
    {
        // Load config
        Config.onLoad(); 

        // Register items and creative tabs
        // These methods will need to be adapted in their respective classes to use Architectury's registration
        ItemRegistries.register(); 
        CreativeTabRegistries.register(); 

        // Register network packets
        PacketRegistries.register();

        // Register common events
        LifecycleEvent.SERVER_STARTING.register(server -> {
            // Code to run when the server is starting
            LOGGER.info("NsTutLib: Server starting");
        });

        LOGGER.info("NsTutLib common setup complete.");
    }

    public static void initClient()
    {
        ClientLifecycleEvent.CLIENT_SETUP.register(minecraft -> { 
            LOGGER.info("NsTutLib client setup complete.");
        });
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
