# NsTut Lib

Shared Minecraft modding library used by NsTut projects.

## Current release line

- Version: **0.8.1**
- Supported targets: Forge 1.20.1, NeoForge 1.21.1, NeoForge 26.1.2
- NeoForge 26.1.2 consumers must use **0.8.1 or newer within the 0.8.x line**. The earlier 0.8 binary contains an incompatible `MachineBlock` construction API.

## Machine API

NsTut Lib owns the reusable **multiblock machine framework**: controller lifecycle, structure validation, generic item/fluid/energy processing, recipe transaction persistence, rollback/retry safety, probabilistic-output persistence, and generic operating state. Consumer mods own all domain semantics and machine-specific rules.

`MachineBlockEntity.processRecipeTransaction` provides persisted transactional recipe processing. Active recipe identity, progress, selected probabilistic outputs, and input-consumed state survive reloads. Recoverable capability divergence is rolled back and retried with bounded backoff; rollback corruption cancels the transaction rather than risking duplication.

The optional recipe-preparation callback runs after generic recipe selection/input preflight and before the transaction snapshot is persisted or inputs are consumed. It may derive a transaction-local recipe from the concrete inputs, but NsTut Lib does not define what that transformation means. Animal genetics, crop quality, loot-table behavior, or any other mod-specific semantics belong entirely to the consuming mod.

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
