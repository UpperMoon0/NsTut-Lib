package com.nstut.nstutlib.recipes;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InputAwareRecipeSnapshotContractTest {
    private static final String[] TARGETS = {
            "forge-1.20.1",
            "neoforge-1.21.1",
            "neoforge-26.1.2"
    };

    @Test
    void transactionEngineExecutesAndPersistsInputAwareSnapshot() throws IOException {
        Path root = findRepositoryRoot();
        for (String target : TARGETS) {
            String hook = Files.readString(root.resolve(target
                    + "/src/main/java/com/nstut/nstutlib/recipes/InputAwareRecipeSnapshot.java"));
            String machine = Files.readString(root.resolve(target
                    + "/src/main/java/com/nstut/nstutlib/blocks/MachineBlockEntity.java"));

            assertTrue(hook.contains("snapshotForExecution"), target);
            assertTrue(machine.contains("recipe instanceof InputAwareRecipeSnapshot"), target);
            assertTrue(machine.contains("inputAware.snapshotForExecution(inputSlots, inputTanks)"), target);
            assertTrue(machine.contains("ModRecipe<?> executionRecipe = createSnapshotRecipe"), target);
            assertTrue(machine.contains("recipeHandler = Optional.of(executionRecipe)"), target);
            assertTrue(machine.contains("activeRecipeSnapshot = snapshot"), target);
            assertTrue(machine.contains("activeItemOutputIndexes = executionRecipe.rollItemOutputIndexes()"), target);
            assertTrue(machine.contains("recipe.getRecipe().copy()"), target + " default snapshot fallback");
        }
    }

    private static Path findRepositoryRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.isRegularFile(current.resolve("gradle.properties"))
                    && Files.isRegularFile(current.resolve("settings.gradle"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate NsTut-Lib repository root");
    }
}
