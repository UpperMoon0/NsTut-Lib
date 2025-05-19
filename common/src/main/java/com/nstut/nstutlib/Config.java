package com.nstut.nstutlib;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import dev.architectury.platform.Platform;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final String CONFIG_FILE_NAME = NsTutLib.MOD_ID + ".toml";
    private static CommentedFileConfig configData;

    public static boolean isDevEnv;

    // Default values
    private static final Map<String, Object> DEFAULTS = new HashMap<>();
    static {
        DEFAULTS.put("general.isDevEnvironment", false); 
    }

    static void onLoad() {
        Path configPath = Platform.getConfigFolder().resolve(CONFIG_FILE_NAME);

        // Create config file with defaults if it doesn't exist
        if (!Files.exists(configPath)) {
            NsTutLib.getLogger().info("Config file not found, creating with defaults: " + configPath);
            CommentedFileConfig newConfig = CommentedFileConfig.builder(configPath, TomlFormat.instance())
                    .writingMode(WritingMode.REPLACE)
                    .build();
            DEFAULTS.forEach(newConfig::set);
            newConfig.save();
            newConfig.close(); // Close after writing
        }

        // Load the config
        configData = CommentedFileConfig.builder(configPath, TomlFormat.instance())
                .sync()
                .autosave() // Autosave on set/remove
                .writingMode(WritingMode.REPLACE) // Replace existing values on save
                .build();

        configData.load(); // Load values from file

        // Ensure all default values are present if the file existed but was missing some
        boolean missingValues = false;
        for (Map.Entry<String, Object> entry : DEFAULTS.entrySet()) {
            if (!configData.contains(entry.getKey())) {
                configData.set(entry.getKey(), entry.getValue());
                missingValues = true;
            }
        }
        if (missingValues) {
            configData.save(); // Save if defaults were added
        }

        // Load values into static fields
        isDevEnv = configData.getOrElse("general.isDevEnvironment", false); // Ensure this also uses false as ultimate default
        NsTutLib.IS_DEV_ENV = isDevEnv; // Update the static field in NsTutLib

        NsTutLib.getLogger().info("NsTutLib Config Loaded. Running in " + (isDevEnv ? "DEV" : "PROD") + " environment.");
    }

    public static void register() {
        // Config loading is handled by onLoad(), called from NsTutLib.init().
        NsTutLib.getLogger().info("Config registration called. Actual loading happens in onLoad().");
    }

    // Helper method to get a boolean value
    public static boolean getBoolean(String path, boolean defaultValue) {
        if (configData == null) {
            NsTutLib.getLogger().warn("Attempted to get config value before config loaded: " + path);
            // Ensure this reflects the new default if called before load for this specific key
            if ("general.isDevEnvironment".equals(path)) {
                return false;
            }
            return defaultValue;
        }
        return configData.getOptional(path).map(o -> (Boolean) o).orElse(defaultValue);
    }

    // Add more getters as needed for other config types (int, String, etc.)
}
