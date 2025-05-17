package com.nstut.nstutlib;

import com.electronwill.nightconfig.core.ConfigSpec;
import com.electronwill.nightconfig.core.conversion.Path;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.CreativeModeTab;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

public class Config
{
    private static final ConfigSpec.Builder BUILDER = new ConfigSpec.Builder();

    @Path("isDevEnv")
    private static final ConfigSpec.BooleanValue IS_DEV_ENV = BUILDER
            .comment("Whether the mod is running in a development environment")
            .define("isDevEnv", false);

    static final ConfigSpec SPEC = BUILDER.build();

    public static boolean isDevEnv;

    static void onLoad()
    {
        final CommentedFileConfig configData = CommentedFileConfig.builder(Platform.getConfigFolder().resolve("nstutlib.toml"))
                .sync()
                .autosave()
                .writingMode(com.electronwill.nightconfig.core.io.WritingMode.REPLACE)
                .build();

        configData.load();
        SPEC.setConfig(configData);

        isDevEnv = IS_DEV_ENV.get();
        NsTutLib.IS_DEV_ENV = isDevEnv;
    }

    public static void register() {
        LifecycleEvent.SETUP.register(() -> {
            isDevEnv = IS_DEV_ENV.get();
            NsTutLib.IS_DEV_ENV = isDevEnv;
        });
    }
}