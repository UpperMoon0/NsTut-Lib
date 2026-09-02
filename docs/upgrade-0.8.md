# Upgrade to NsTut Lib 0.8

Machine consumers can migrate duplicated processing loops to `MachineBlockEntity.processRecipeTransaction`. Active recipe ID/progress/input-consumed state are persisted by the base class. Multiblock pattern state properties are now enforced (except runtime `operating`).

Transactional machine item handlers must implement `IItemHandlerModifiable`, and transactional fluid handlers must be `FluidTank` instances or subclasses so failed commits can be restored atomically. Existing JSON compatibility is preserved: omitting `isConsumable` still means `false`; set it explicitly to `true` for consumed ingredients.
