package com.nstut.nstutlib.views;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nstut.nstutlib.NsTutLib;
import com.nstut.nstutlib.models.MultiblockBlock;
import com.nstut.nstutlib.models.MultiblockPattern;
import com.nstut.nstutlib.network.StructureScannerC2SPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StructureScannerScreen extends Screen {
    private static final Path OUTPUT_DIR = FMLPaths.GAMEDIR.get().resolve("nstut_script_output");
    private static final long MAX_EXPORT_BLOCKS = 32_768L;
    private static final String SYMBOLS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!#$%&()*+,-./:;<=>?@[]^_{|}~";
    private static final Logger LOGGER = Logger.getLogger(StructureScannerScreen.class.getName());
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(NsTutLib.MOD_ID, "textures/gui/structure_scanner.png");

    private final Level level;
    private final int initialFirstX, initialFirstY, initialFirstZ;
    private final int initialSecondX, initialSecondY, initialSecondZ;
    private EditBox firstCornerX, firstCornerY, firstCornerZ;
    private EditBox secondCornerX, secondCornerY, secondCornerZ;

    public StructureScannerScreen(Level level, int firstX, int firstY, int firstZ, int secondX, int secondY, int secondZ) {
        super(Component.literal("Structure Scanner"));
        this.level = level;
        this.initialFirstX = firstX;
        this.initialFirstY = firstY;
        this.initialFirstZ = firstZ;
        this.initialSecondX = secondX;
        this.initialSecondY = secondY;
        this.initialSecondZ = secondZ;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = width / 2;
        int centerY = height / 2;
        firstCornerX = coordinateBox(centerX - 70, centerY - 40, "X", initialFirstX);
        firstCornerY = coordinateBox(centerX - 20, centerY - 40, "Y", initialFirstY);
        firstCornerZ = coordinateBox(centerX + 30, centerY - 40, "Z", initialFirstZ);
        secondCornerX = coordinateBox(centerX - 70, centerY + 10, "X", initialSecondX);
        secondCornerY = coordinateBox(centerX - 20, centerY + 10, "Y", initialSecondY);
        secondCornerZ = coordinateBox(centerX + 30, centerY + 10, "Z", initialSecondZ);
        addRenderableWidget(firstCornerX); addRenderableWidget(firstCornerY); addRenderableWidget(firstCornerZ);
        addRenderableWidget(secondCornerX); addRenderableWidget(secondCornerY); addRenderableWidget(secondCornerZ);
        addRenderableWidget(Button.builder(Component.literal("Save"), this::onSave).pos(centerX - 60, centerY + 50).size(50, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Export"), this::onExport).pos(centerX + 10, centerY + 50).size(50, 20).build());
        addRenderableWidget(new StringWidget(centerX - 89, centerY - 60, 100, 20, Component.literal("First Corner"), font));
        addRenderableWidget(new StringWidget(centerX - 84, centerY - 10, 100, 20, Component.literal("Second Corner"), font));
    }

    private EditBox coordinateBox(int x, int y, String label, int value) {
        EditBox box = new EditBox(font, x, y, 40, 15, Component.literal(label));
        box.setValue(Integer.toString(value));
        box.setMaxLength(11);
        return box;
    }

    private void onSave(Button ignored) {
        int[] corners = readCorners();
        if (corners == null) return;
        ClientPacketDistributor.sendToServer(new StructureScannerC2SPacket(corners[0], corners[1], corners[2], corners[3], corners[4], corners[5]));
    }

    private void onExport(Button ignored) {
        int[] corners = readCorners();
        if (corners == null) return;
        int minX = Math.min(corners[0], corners[3]);
        int minY = Math.min(corners[1], corners[4]);
        int minZ = Math.min(corners[2], corners[5]);
        int maxX = Math.max(corners[0], corners[3]);
        int maxY = Math.max(corners[1], corners[4]);
        int maxZ = Math.max(corners[2], corners[5]);
        long width = (long) maxX - minX + 1L;
        long height = (long) maxY - minY + 1L;
        long depth = (long) maxZ - minZ + 1L;
        long volume;
        try {
            volume = Math.multiplyExact(Math.multiplyExact(width, height), depth);
        } catch (ArithmeticException exception) {
            notifyUser("Selection is too large");
            return;
        }
        if (volume <= 0 || volume > MAX_EXPORT_BLOCKS) {
            notifyUser("Selection must contain at most " + MAX_EXPORT_BLOCKS + " blocks");
            return;
        }
        for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    notifyUser("Load the entire selection before exporting");
                    return;
                }
            }
        }
        MultiblockBlock[][][] blockArray = new MultiblockBlock[(int) height][(int) depth][(int) width];
        for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int x = maxX; x >= minX; x--) {
                    BlockState state = level.getBlockState(new BlockPos(x, y, z));
                    if (state.isAir()) continue;
                    Map<String, String> stateMap = state.getProperties().stream()
                            .collect(Collectors.toMap(Property::getName, property -> propertyValue(state, property)));
                    blockArray[y - minY][z - minZ][maxX - x] = new MultiblockBlock(state.getBlock(), stateMap);
                }
            }
        }
        try {
            Files.createDirectories(OUTPUT_DIR);
            MultiblockPattern pattern = new MultiblockPattern(blockArray);
            writePatchouliJson(pattern, OUTPUT_DIR.resolve("structure_patchouli.json"));
            writeJavaPattern(pattern, OUTPUT_DIR.resolve("structure_pattern.txt"));
            notifyUser("Structure exported to " + OUTPUT_DIR);
        } catch (IOException | IllegalStateException exception) {
            LOGGER.warning("Structure export failed: " + exception.getMessage());
            notifyUser("Export failed: " + exception.getMessage());
        }
    }

    private int[] readCorners() {
        try {
            return new int[]{Integer.parseInt(firstCornerX.getValue()), Integer.parseInt(firstCornerY.getValue()), Integer.parseInt(firstCornerZ.getValue()),
                    Integer.parseInt(secondCornerX.getValue()), Integer.parseInt(secondCornerY.getValue()), Integer.parseInt(secondCornerZ.getValue())};
        } catch (NumberFormatException exception) {
            notifyUser("Coordinates must be valid 32-bit integers");
            return null;
        }
    }

    private static <T extends Comparable<T>> String propertyValue(BlockState state, Property<T> property) {
        return property.getName(state.getValue(property));
    }

    private void writePatchouliJson(MultiblockPattern multiblockPattern, Path path) throws IOException {
        MultiblockBlock[][][] pattern = multiblockPattern.getPattern();
        Map<MultiblockBlock, Character> symbols = createSymbolMap(pattern);
        List<List<String>> jsonPattern = new ArrayList<>();
        for (int y = pattern.length - 1; y >= 0; y--) {
            List<String> layer = new ArrayList<>();
            for (MultiblockBlock[] row : pattern[y]) {
                StringBuilder line = new StringBuilder();
                for (MultiblockBlock block : row) line.append(block == null ? ' ' : symbols.get(block));
                layer.add(line.toString());
            }
            jsonPattern.add(layer);
        }
        Map<String, String> formattedMapping = new LinkedHashMap<>();
        for (Map.Entry<MultiblockBlock, Character> entry : symbols.entrySet()) {
            MultiblockBlock block = entry.getKey();
            Identifier id = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block.getBlock()));
            String blockState = id.toString();
            if (!block.getStates().isEmpty()) {
                blockState += "[" + block.getStates().entrySet().stream()
                        .map(state -> state.getKey() + "=" + state.getValue()).collect(Collectors.joining(",")) + "]";
            }
            formattedMapping.put(String.valueOf(entry.getValue()), blockState);
        }
        Map<String, Object> multiblock = new LinkedHashMap<>();
        multiblock.put("pattern", jsonPattern);
        multiblock.put("mapping", formattedMapping);
        multiblock.put("symmetrical", true);
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("type", "patchouli:multiblock");
        root.put("multiblock", multiblock);
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        try (BufferedWriter writer = Files.newBufferedWriter(path)) { gson.toJson(root, writer); }
    }

    private void writeJavaPattern(MultiblockPattern multiblockPattern, Path path) throws IOException {
        MultiblockBlock[][][] pattern = multiblockPattern.getPattern();
        Map<MultiblockBlock, String> variables = new LinkedHashMap<>();
        int index = 0;
        for (MultiblockBlock[][] layer : pattern) for (MultiblockBlock[] row : layer) for (MultiblockBlock block : row)
            if (block != null && !variables.containsKey(block)) variables.put(block, "b" + index++);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("@Override\npublic MultiblockPattern getMultiblockPattern() {\n");
            for (Map.Entry<MultiblockBlock, String> entry : variables.entrySet()) {
                MultiblockBlock block = entry.getKey();
                Identifier id = Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block.getBlock()));
                writer.write("    MultiblockBlock " + entry.getValue() + " = new MultiblockBlock(");
                writer.write("java.util.Objects.requireNonNull(net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(");
                writer.write("net.minecraft.resources.Identifier.fromNamespaceAndPath(\"" + id.getNamespace() + "\", \"" + id.getPath() + "\"))), ");
                writer.write(formatStateMap(block.getStates()));
                writer.write(");\n");
            }
            writer.write("\n    MultiblockBlock[][][] blockArray = new MultiblockBlock[][][] {\n");
            for (MultiblockBlock[][] layer : pattern) {
                writer.write("        {\n");
                for (MultiblockBlock[] row : layer) {
                    StringJoiner joiner = new StringJoiner(", ", "            {", "},\n");
                    for (MultiblockBlock block : row) joiner.add(block == null ? "null" : variables.get(block));
                    writer.write(joiner.toString());
                }
                writer.write("        },\n");
            }
            writer.write("    };\n\n    return new MultiblockPattern(blockArray);\n}\n");
        }
    }

    private static String formatStateMap(Map<String, String> states) {
        if (states == null || states.isEmpty()) return "java.util.Map.of()";
        StringJoiner joiner = new StringJoiner(", ", "java.util.Map.of(", ")");
        states.forEach((key, value) -> { joiner.add("\"" + key + "\""); joiner.add("\"" + value + "\""); });
        return joiner.toString();
    }

    private static Map<MultiblockBlock, Character> createSymbolMap(MultiblockBlock[][][] pattern) {
        Map<MultiblockBlock, Character> symbols = new LinkedHashMap<>();
        for (MultiblockBlock[][] layer : pattern) for (MultiblockBlock[] row : layer) for (MultiblockBlock block : row) {
            if (block != null && !symbols.containsKey(block)) {
                int index = symbols.size();
                if (index >= SYMBOLS.length()) throw new IllegalStateException("Too many unique block states to export");
                symbols.put(block, SYMBOLS.charAt(index));
            }
        }
        return symbols;
    }

    private void notifyUser(String message) {
        if (minecraft != null && minecraft.player != null) minecraft.player.sendSystemMessage(Component.literal(message));
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int screenHeight = 166;
        int screenWidth = 176;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                (width - screenWidth) / 2, (height - screenHeight) / 2,
                0.0f, 0.0f, screenWidth, screenHeight, screenWidth, screenHeight);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }
}
