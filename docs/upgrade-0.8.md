# Upgrade to NsTut Lib 0.8

Machine consumers can migrate duplicated processing loops to `MachineBlockEntity.processRecipeTransaction`. Active recipe ID/progress/input-consumed state are persisted by the base class. Multiblock pattern state properties are now enforced (except runtime `operating`).
