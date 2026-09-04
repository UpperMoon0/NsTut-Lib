# Changelog

## 0.8.1

- Reworked machine recipe execution into a resumable transaction with persisted recipe identity, progress, and one-time input consumption.
- Fixed item/fluid output duplication, overstacking, NBT-insensitive matching, and wrong-fluid drains.
- Added recipe/network bounds and recipe-data validation.
- Made multiblock validation honor authored block states and rectangular rotations while ignoring dynamic `operating` state.
- Reduced multiblock polling and stopped rewriting controller block state when no operating-state change occurred.
- Replaced reflective machine block-entity construction with factories.
- Fixed the NeoForge 26.1.2 keyed `MachineBlock` construction API so consumers receive the registry-owned `BlockBehaviour.Properties` instance.
- Removed dedicated-server client-class access from Smart Hammer and Structure Scanner flows.
- Restricted Structure Scanner sync to the requesting player, tightened network protocol compatibility, and hardened export bounds/path handling.
- Made Smart Hammer use the invoking player, preflight resources, refuse destructive replacement, and require water buckets in survival.
- Removed tracked runtime logs/local Maven output and added CI coverage.

> 0.8.1 supersedes the incompatible 0.8 binary. NeoForge 26.1.2 consumers should require 0.8.1 or newer within the 0.8.x compatibility line.
