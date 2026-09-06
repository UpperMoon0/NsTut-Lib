# Upgrade to NsTut Lib 0.8.1

NsTut Lib 0.8.1 is the supported hardening release for the 0.8.x API line and supersedes the incompatible 0.8 binary on NeoForge 26.1.2.

Machine consumers can migrate duplicated processing loops to `MachineBlockEntity.processRecipeTransaction`. Active recipe ID/progress/input-consumed state are persisted by the base class. Multiblock pattern state properties are now enforced except for the runtime `operating` state.

Transactional machine item handlers must implement `IItemHandlerModifiable`, and transactional fluid handlers must be `FluidTank` instances or subclasses so failed commits can be restored atomically. Existing JSON compatibility is preserved: omitting `isConsumable` still means `false`; set it explicitly to `true` for consumed ingredients.

On NeoForge 26.1.2, construct `MachineBlock` instances with the keyed `BlockBehaviour.Properties` supplied by `DeferredRegister.registerBlock`. Consumers should require NsTut Lib `0.8.1` or newer within the `0.8.x` line rather than accepting plain `0.8`.
