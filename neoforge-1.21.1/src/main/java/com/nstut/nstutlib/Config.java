package com.nstut.nstutlib;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue IS_DEV_ENV = BUILDER
            .comment("Whether the mod is running in a development environment")
            .define("isDevEnv", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean isDevEnv;

    private Config() {
    }

    static void onLoad(final ModConfigEvent event) {
        isDevEnv = IS_DEV_ENV.get();
        NsTutLib.IS_DEV_ENV = isDevEnv;
    }
}
