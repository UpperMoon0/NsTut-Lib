package com.nstut.nstutlib;

import com.mojang.logging.LogUtils;
import com.nstut.nstutlib.creative_tabs.CreativeTabRegistries;
import com.nstut.nstutlib.items.ItemRegistries;
import com.nstut.nstutlib.network.PacketRegistries;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.client.ClientLifecycleEvent; // Added for CLIENT_SETUP
// import dev.architectury.event.events.common.CreativeModeTabEvent; // Example for creative tabs if needed later
import org.slf4j.Logger;

public class NsTutLib
{
    public static final String MOD_ID = "nstutlib";
    public static boolean IS_DEV_ENV;
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void init()
    {
        // Load config
        Config.onLoad(); // Ensure config is loaded; Config.register() also hooks into LifecycleEvent.SETUP

        // Register items and creative tabs
        // These methods will need to be adapted in their respective classes to use Architectury's registration
        // ItemRegistries.register(); // Commented out for now
        // CreativeTabRegistries.register(); // Commented out for now

        // Register network packets
        PacketRegistries.register();

        // Register common events
        LifecycleEvent.SERVER_STARTING.register(server -> {
            // Code to run when the server is starting
            LOGGER.info("NsTutLib: Server starting");
        });

        // Example for adding items to creative tabs - this will likely be handled within CreativeTabRegistries
        // CreativeModeTabEvent.BUILD_CONTENTS.register((tabKey, event) -> {
        // if (tabKey.equals(CreativeTabRegistries.YOUR_TAB_KEY)) { // Replace with your actual tab key
        // event.accept(ItemRegistries.YOUR_ITEM_SUPPLIER);
        // }
        // });

        LOGGER.info("NsTutLib common setup complete.");
    }

    public static void initClient()
    {
        ClientLifecycleEvent.CLIENT_SETUP.register(minecraft -> { // Corrected event
            // Code to run during client setup
            // For example, screen registration if not handled by Architectury's @RegisterScreen
            LOGGER.info("NsTutLib client setup complete.");
        });
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}
