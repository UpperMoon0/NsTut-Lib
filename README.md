# NsTut Lib

Shared Minecraft modding library used by NsTut projects.

## Current release line

- Version: **0.8.1**
- Supported targets: Forge 1.20.1, NeoForge 1.21.1, NeoForge 26.1.2
- NeoForge 26.1.2 consumers must use **0.8.1 or newer within the 0.8.x line**. The earlier 0.8 binary contains an incompatible `MachineBlock` construction API.

## Machine API

`MachineBlockEntity.processRecipeTransaction` provides persisted transactional recipe processing. Active recipe identity, progress, selected probabilistic outputs, and input-consumed state survive reloads. Recoverable capability divergence is rolled back and retried with bounded backoff; rollback corruption cancels the transaction rather than risking duplication.

Transactional item handlers must implement `IItemHandlerModifiable`. Transactional fluid handlers must be `FluidTank` instances or subclasses so snapshots can be restored atomically.

For NeoForge 26.1.2, create machine blocks from the keyed `BlockBehaviour.Properties` supplied by `DeferredRegister.registerBlock` and pass that same instance to `MachineBlock`.

## Compatibility notes

Network protocol compatibility is strict. Multiblock validation honors authored block-state properties except the runtime `operating` property. Structure Scanner synchronization is player-targeted and export is bounded/path-safe.

## Documentation

- `CHANGELOG.md` for the canonical changelog
- `CHANGELOG-0.8.1.md` for the complete 0.8.1 release notes
- `docs/upgrade-0.8.1.md` for the 0.8.1 migration contract
- `docs/recipes-0.8.md` for the 0.8.x transactional recipe API
- `docs/networking-0.8.md` for the 0.8.x networking contract
- `README_HARDENING.md` for the hardening summary
