# Recipe transactions in 0.8

`MachineBlockEntity.processRecipeTransaction` centralizes selection, one-time input consumption, exact energy progress, persisted active recipe state, output-capacity checks and final output commit. Recipe implementations use simulated capability operations before mutating inventory or fluid state.
