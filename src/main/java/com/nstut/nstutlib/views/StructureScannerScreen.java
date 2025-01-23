package com.nstut.nstutlib.views;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.models.MultiblockBlock;
import com.nstut.nstutlib.models.MultiblockPattern;
import com.nstut.nstutlib.network.PacketRegistries;
import com.nstut.nstutlib.network.StructureScannerC2SPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StructureScannerScreen extends Screen {
    private static final String SCRIPT_OUTPUT_PATH = FMLPaths.GAMEDIR.get().resolve("nstut_script_output").toString();
    private static final Logger LOGGER = Logger.getLogger(StructureScannerScreen.class.getName());
    private static final ResourceLocation TEXTURE = new ResourceLocation(NsTutLib.MOD_ID, "textures/gui/structure_scanner.png");

    private final Level level;

    private EditBox firstCornerX;
    private EditBox firstCornerY;
    private EditBox firstCornerZ;
    private EditBox secondCornerX;
    private EditBox secondCornerY;
    private EditBox secondCornerZ;

    public StructureScannerScreen(Level level, ItemStack scannerItem) {
        super(Component.literal("Structure Scanner"));
        this.level = level;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Initialize input fields with the latest values
        this.firstCornerX = new EditBox(this.font, centerX - 70, centerY - 40, 40, 15, Component.literal("X"));
        this.firstCornerY = new EditBox(this.font, centerX - 20, centerY - 40, 40, 15, Component.literal("Y"));
        this.firstCornerZ = new EditBox(this.font, centerX + 30, centerY - 40, 40, 15, Component.literal("Z"));
        this.secondCornerX = new EditBox(this.font, centerX - 70, centerY + 10, 40, 15, Component.literal("X"));
        this.secondCornerY = new EditBox(this.font, centerX - 20, centerY + 10, 40, 15, Component.literal("Y"));
        this.secondCornerZ = new EditBox(this.font, centerX + 30, centerY + 10, 40, 15, Component.literal("Z"));

        // Set default values
        this.firstCornerX.setValue("0");
        this.firstCornerY.setValue("0");
        this.firstCornerZ.setValue("0");
        this.secondCornerX.setValue("0");
        this.secondCornerY.setValue("0");
        this.secondCornerZ.setValue("0");

        this.addRenderableWidget(this.firstCornerX);
        this.addRenderableWidget(this.firstCornerY);
        this.addRenderableWidget(this.firstCornerZ);
        this.addRenderableWidget(this.secondCornerX);
        this.addRenderableWidget(this.secondCornerY);
        this.addRenderableWidget(this.secondCornerZ);

        // Save Button
        Button saveButton = Button.builder(Component.literal("Save"), this::onSave)
                .pos(centerX - 60, centerY + 50)
                .size(50, 20)
                .build();
        this.addRenderableWidget(saveButton);

        // Export Button
        Button exportButton = Button.builder(Component.literal("Export"), this::onExport)
                .pos(centerX + 10, centerY + 50)
                .size(50, 20)
                .build();
        this.addRenderableWidget(exportButton);

        // Labels
        StringWidget firstCornerLabel = new StringWidget(centerX - 89, centerY - 60, 100, 20, Component.literal("First Corner"), this.font);
        StringWidget secondCornerLabel = new StringWidget(centerX - 84, centerY - 10, 100, 20, Component.literal("Second Corner"), this.font);
        this.addRenderableWidget(firstCornerLabel);
        this.addRenderableWidget(secondCornerLabel);
    }

    private void onSave(Button button) {
        try {
            int firstX = Integer.parseInt(this.firstCornerX.getValue());
            int firstY = Integer.parseInt(this.firstCornerY.getValue());
            int firstZ = Integer.parseInt(this.firstCornerZ.getValue());
            int secondX = Integer.parseInt(this.secondCornerX.getValue());
            int secondY = Integer.parseInt(this.secondCornerY.getValue());
            int secondZ = Integer.parseInt(this.secondCornerZ.getValue());

            // Send packet to the server with the updated corner values
            PacketRegistries.sendToServer(new StructureScannerC2SPacket(firstX, firstY, firstZ, secondX, secondY, secondZ));
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid input: " + e.getMessage());
        }
    }

    private void onExport(Button button) {
        int x1 = Integer.parseInt(this.firstCornerX.getValue());
        int y1 = Integer.parseInt(this.firstCornerY.getValue());
        int z1 = Integer.parseInt(this.firstCornerZ.getValue());
        int x2 = Integer.parseInt(this.secondCornerX.getValue());
        int y2 = Integer.parseInt(this.secondCornerY.getValue());
        int z2 = Integer.parseInt(this.secondCornerZ.getValue());

        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);
        int maxZ = Math.max(z1, z2);

        // Initialize pattern and mapping
        List<List<String>> pattern = new ArrayList<>();
        Map<String, MultiblockBlock> mapping = new HashMap<>();
        char currentChar = 'a';

        // Iterate over the area to capture block data
        for (int y = maxY; y >= minY; y--) {
            List<String> layer = new ArrayList<>();
            for (int z = minZ; z <= maxZ; z++) {
                StringBuilder row = new StringBuilder();
                for (int x = maxX; x >= minX; x--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    String blockName = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(state.getBlock())).toString();

                    // Capture block states
                    Map<String, String> stateMap = state.getProperties().stream()
                            .collect(Collectors.toMap
                                    (
                                            Property::getName,
                                            property -> state.getValue(property).toString()
                                    )
                            );

                    if (blockName.equals("minecraft:air")) {
                        row.append(" ");
                    } else {
                        MultiblockBlock multiblockBlock = new MultiblockBlock(state.getBlock(), stateMap);
                        String symbol = mapping.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(multiblockBlock))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse(null);

                        if (symbol == null) {
                            symbol = String.valueOf(currentChar);
                            mapping.put(symbol, multiblockBlock);
                            currentChar++;
                        }

                        row.append(symbol);
                    }
                }
                layer.add(row.toString()); // Add the reversed row to the layer
            }
            pattern.add(0, layer); // Add the layer in reverse order to fix Y-axis flipping
        }

        // Create a MultiblockPattern object from the collected pattern
        MultiblockBlock[][][] blockArray = new MultiblockBlock[pattern.size()][][];
        for (int y = 0; y < pattern.size(); y++) {
            List<String> layer = pattern.get(y);
            MultiblockBlock[][] layerArray = new MultiblockBlock[layer.size()][];
            for (int z = 0; z < layer.size(); z++) {
                String row = layer.get(z);
                MultiblockBlock[] rowArray = new MultiblockBlock[row.length()];
                for (int x = row.length() - 1; x >= 0; x--) {
                    String symbol = String.valueOf(row.charAt(x));
                    if (" ".equals(symbol)) {
                        rowArray[x] = null;
                    } else {
                        rowArray[x] = mapping.get(symbol);
                    }
                }
                layerArray[z] = rowArray;
            }
            blockArray[y] = layerArray;
        }

        // Create a MultiblockPattern object
        MultiblockPattern multiblockPattern = new MultiblockPattern(blockArray);

        // Write to files using the new MultiblockPattern object
        writeJson(multiblockPattern);
        writeTxt(multiblockPattern);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int screenHeight = 166;
        int screenWidth = 176;
        graphics.blit(TEXTURE, (this.width - screenWidth) / 2, (this.height - screenHeight) / 2, 0, 0, screenWidth, screenHeight);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.firstCornerX.render(graphics, mouseX, mouseY, partialTicks);
        this.firstCornerY.render(graphics, mouseX, mouseY, partialTicks);
        this.firstCornerZ.render(graphics, mouseX, mouseY, partialTicks);
        this.secondCornerX.render(graphics, mouseX, mouseY, partialTicks);
        this.secondCornerY.render(graphics, mouseX, mouseY, partialTicks);
        this.secondCornerZ.render(graphics, mouseX, mouseY, partialTicks);
    }

    public void setCorners(int firstX, int firstY, int firstZ, int secondX, int secondY, int secondZ) {
        this.firstCornerX.setValue(String.valueOf(firstX));
        this.firstCornerY.setValue(String.valueOf(firstY));
        this.firstCornerZ.setValue(String.valueOf(firstZ));
        this.secondCornerX.setValue(String.valueOf(secondX));
        this.secondCornerY.setValue(String.valueOf(secondY));
        this.secondCornerZ.setValue(String.valueOf(secondZ));
    }

    private void writeJson(MultiblockPattern multiblockPattern) {
        // Extract pattern and mapping from the MultiblockPattern instance
        MultiblockBlock[][][] pattern = multiblockPattern.getPattern();

        // Convert the pattern to a list of string layers for JSON format
        List<List<String>> jsonPattern = new ArrayList<>();
        Map<String, MultiblockBlock> mapping = new LinkedHashMap<>();
        char currentChar = 'b';

        for (int y = pattern.length - 1; y >= 0; y--) {
            List<String> layer = new ArrayList<>();
            for (int x = pattern[0][0].length - 1; x >= 0; x--) {
                StringBuilder row = new StringBuilder();
                for (int z = 0; z < pattern[0].length; z++) {
                    MultiblockBlock block = pattern[y][z][x];
                    if (block == null) {
                        row.append(" ");
                    } else {
                        String symbol = mapping.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(block))
                                .map(Map.Entry::getKey)
                                .findFirst()
                                .orElse(null);

                        if (symbol == null) {
                            symbol = String.valueOf(currentChar++);
                            mapping.put(symbol, block);
                        }

                        row.append(symbol);
                    }
                }
                layer.add(row.toString());
            }
            jsonPattern.add(layer);
        }

        // Create the JSON structure
        Map<String, Object> jsonData = new HashMap<>();
        jsonData.put("type", "patchouli:multiblock");

        Map<String, Object> multiblockData = new HashMap<>();
        multiblockData.put("pattern", jsonPattern);

        // Modify the mapping output to match the required format
        Map<String, String> formattedMapping = new LinkedHashMap<>();
        for (Map.Entry<String, MultiblockBlock> entry : mapping.entrySet()) {
            String blockStateString = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(entry.getValue().getBlock())).toString();

            // Add block state properties
            String blockStateProperties = entry.getValue().getStates().entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(", "));

            if (!blockStateProperties.isEmpty()) {
                blockStateString += "[" + blockStateProperties + "]";
            }

            formattedMapping.put(entry.getKey(), blockStateString);
        }

        multiblockData.put("mapping", formattedMapping);
        multiblockData.put("symmetrical", true);
        jsonData.put("multiblock", multiblockData);

        // Serialize to JSON and save to file
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        try (FileWriter writer = new FileWriter(SCRIPT_OUTPUT_PATH + "\\structure_patchouli.json")) {
            gson.toJson(jsonData, writer);
        } catch (IOException e) {
            LOGGER.severe("Failed to write JSON file: " + e.getMessage());
        }
    }

    private void writeTxt(MultiblockPattern multiblockPattern) {
        MultiblockBlock[][][] pattern = multiblockPattern.getPattern();
        Map<String, String> filteredMapping = new LinkedHashMap<>();
        Map<String, MultiblockBlock> mapping = new LinkedHashMap<>();
        char currentChar = 'b';

        // Populate the mapping only with unique blocks
        for (MultiblockBlock[][] layer : pattern) {
            for (MultiblockBlock[] row : layer) {
                for (MultiblockBlock block : row) {
                    if (block != null && !mapping.containsValue(block)) {
                        mapping.put(String.valueOf(currentChar++), block);
                    }
                }
            }
        }

        // Filter and map unique blocks to variable names
        for (Map.Entry<String, MultiblockBlock> entry : mapping.entrySet()) {
            MultiblockBlock block = entry.getValue();
            String blockName = ForgeRegistries.BLOCKS.getKey(block.getBlock()).toString();
            filteredMapping.putIfAbsent(entry.getKey(), blockName); // Avoids duplicates
        }

        // Write the multiblock pattern
        try (FileWriter writer = new FileWriter(SCRIPT_OUTPUT_PATH + "\\structure_pattern.txt")) {
            writer.write("@Override\n");
            writer.write("public MultiblockPattern getMultiblockPattern() {\n");

            // Write block declarations
            writer.write("    MultiblockBlock ");
            List<String> declarations = new ArrayList<>();
            for (Map.Entry<String, MultiblockBlock> entry : mapping.entrySet()) {
                MultiblockBlock block = entry.getValue();
                String blockName = ForgeRegistries.BLOCKS.getKey(block.getBlock())
                        .toString()
                        .replace("minecraft:", "")
                        .toUpperCase()
                        .replace(":", "_");
                String attributes = block.getStates().entrySet().stream()
                        .map(e -> "\"" + e.getKey() + "\", \"" + e.getValue() + "\"")
                        .collect(Collectors.joining(", "));
                declarations.add(String.format("%s = new MultiblockBlock(Blocks.%s, Map.of(%s))",
                        entry.getKey(), blockName, attributes));
            }
            writer.write(String.join(",\n    ", declarations) + ";\n\n");

            // Write the pattern array
            writer.write("    MultiblockBlock[][][] blockArray = new MultiblockBlock[][][] {\n");
            for (int y = pattern.length - 1; y >= 0; y--) {
                writer.write("        {\n");
                for (MultiblockBlock[] row : pattern[y]) {
                    // Reverse the row
                    List<MultiblockBlock> reversedRow = Arrays.asList(row.clone());
                    Collections.reverse(reversedRow);

                    String formattedRow = reversedRow.stream()
                            .map(block -> block == null ? "null" : mapping.entrySet().stream()
                                    .filter(entry -> entry.getValue().equals(block))
                                    .map(Map.Entry::getKey)
                                    .findFirst()
                                    .orElse("null"))
                            .collect(Collectors.joining(", "));
                    writer.write("            {" + formattedRow + "},\n");
                }
                writer.write("        },\n");
            }
            writer.write("    };\n\n");

            // Return the multiblock pattern
            writer.write("    return new MultiblockPattern(blockArray, false);\n");
            writer.write("}\n");
        } catch (IOException e) {
            LOGGER.severe("Failed to write TXT file: " + e.getMessage());
        }
    }
}

