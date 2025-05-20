package com.nstut.nstutlib;

import dev.architectury.platform.Platform;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE_NAME = NsTutLib.MOD_ID + ".properties";
    private static final Path CONFIG_PATH = Platform.getConfigFolder().resolve(CONFIG_FILE_NAME);

    // Define config fields directly as static variables with default values
    public static boolean isDevEnv = false;
    private static final String IS_DEV_ENV_KEY = "general.isDevEnvironment";

    static void onLoad() {
        Properties props = new Properties();

        if (Files.exists(CONFIG_PATH)) {
            try (FileInputStream fis = new FileInputStream(CONFIG_PATH.toFile())) {
                props.load(fis);
                isDevEnv = Boolean.parseBoolean(props.getProperty(IS_DEV_ENV_KEY, "false"));
            } catch (IOException e) {
                NsTutLib.getLogger().error("Could not load config file: " + CONFIG_PATH, e);
            }
        } else {
            // Config file doesn't exist, create it with defaults
            props.setProperty(IS_DEV_ENV_KEY, String.valueOf(isDevEnv));
            saveProperties(props);
        }

        // Update the static field in NsTutLib if it's still used this way
        NsTutLib.IS_DEV_ENV = isDevEnv;
        NsTutLib.getLogger().info("NsTutLib Config Loaded. Running in " + (isDevEnv ? "DEV" : "PROD") + " environment.");
    }

    private static void saveProperties(Properties props) {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_PATH.toFile())) {
            props.store(fos, "NsTutLib Configuration");
        } catch (IOException e) {
            NsTutLib.getLogger().error("Could not save config file: " + CONFIG_PATH, e);
        }
    }

    public static void register() {
        NsTutLib.getLogger().info("Config registration called. Loading config...");
        onLoad();
    }

    public static boolean isDevEnvironment() {
        return isDevEnv;
    }

    public static void setIsDevEnv(boolean value) {
        if (isDevEnv != value) {
            isDevEnv = value;
            NsTutLib.IS_DEV_ENV = value; // Keep NsTutLib's static field in sync

            Properties props = new Properties();
            // Load existing properties first to not overwrite other settings if they exist
            if (Files.exists(CONFIG_PATH)) {
                try (FileInputStream fis = new FileInputStream(CONFIG_PATH.toFile())) {
                    props.load(fis);
                } catch (IOException e) {
                    NsTutLib.getLogger().error("Could not load config file for saving: " + CONFIG_PATH, e);
                    // Initialize props to avoid losing the new value if read fails
                }
            }
            props.setProperty(IS_DEV_ENV_KEY, String.valueOf(isDevEnv));
            saveProperties(props);
            NsTutLib.getLogger().info("isDevEnv changed to: " + isDevEnv + " and saved.");
        }
    }
}
