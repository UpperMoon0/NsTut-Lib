package com.nstut.nstutlib;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;

// Removed NightConfig imports as we are simplifying config handling for now

public class Config
{
    public static boolean isDevEnv;

    // Simplified onLoad - directly sets isDevEnv
    static void onLoad()
    {
        // Attempt to use Architectury's platform check, default to false if not available or for simplicity
        isDevEnv = Platform.isDevelopmentEnvironment();
        NsTutLib.IS_DEV_ENV = isDevEnv;
        NsTutLib.getLogger().info("NsTutLib running in " + (isDevEnv ? "DEV" : "PROD") + " environment (Config.onLoad)");
    }

    // Simplified register - ensures isDevEnv is set during setup phase
    public static void register() {
        LifecycleEvent.SETUP.register(() -> {
            isDevEnv = Platform.isDevelopmentEnvironment();
            NsTutLib.IS_DEV_ENV = isDevEnv;
            NsTutLib.getLogger().info("NsTutLib running in " + (isDevEnv ? "DEV" : "PROD") + " environment (Config.register via SETUP)");
        });
    }
}
