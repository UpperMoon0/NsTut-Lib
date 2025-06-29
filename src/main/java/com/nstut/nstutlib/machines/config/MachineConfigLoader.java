package com.nstut.nstutlib.machines.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Optional;
import java.util.logging.Logger;

public class MachineConfigLoader {
    private static final Logger LOGGER = Logger.getLogger(MachineConfigLoader.class.getName());
    private static final Gson GSON = new GsonBuilder().create();

    public static Optional<MachineConfig> loadConfig(ResourceManager resourceManager, ResourceLocation location) {
        try {
            Optional<Resource> resourceOptional = resourceManager.getResource(location);
            if (resourceOptional.isPresent()) {
                Resource resource = resourceOptional.get();
                try (Reader reader = new InputStreamReader(resource.open())) {
                    return Optional.of(GSON.fromJson(reader, MachineConfig.class));
                }
            } else {
                LOGGER.warning("Machine configuration file not found: " + location);
                return Optional.empty();
            }
        } catch (IOException e) {
            LOGGER.severe("Failed to read machine configuration file " + location + ": " + e.getMessage());
            return Optional.empty();
        } catch (JsonParseException e) {
            LOGGER.severe("Failed to parse machine configuration file " + location + ": " + e.getMessage());
            return Optional.empty();
        }
    }
}