# NsTut Lib 0.8.1

## Fixed
- Make recipe processing transactional and persist active recipe/progress state across reloads.
- Roll back partial item/fluid input and output commits when a Forge handler diverges during execution; unsafe non-restorable handlers are rejected before mutation.
- Add a NeoForge 26.1.2 transactional fluid adapter so native `ResourceHandler`-backed fluid storage can safely participate in legacy `IFluidHandler` recipe transactions while preserving rollback support.
- Treat rollback failure as non-retriable transaction corruption and cancel the active recipe instead of risking duplicate output or repeated input consumption.
- Prevent item/fluid output duplication, illegal overstacking, NBT-insensitive matching, and wrong-fluid draining.
- Roll probabilistic item outputs once when a recipe starts, persist the selected outputs, and reuse the same result across reloads and safe rollback retries.
- Respect per-ingredient consumable semantics, preserve the legacy omitted-`isConsumable = false` JSON behavior, and consume exact remaining energy on the final tick.
- Validate recipe JSON/network payloads and reject incompatible network protocol versions.
- Validate multiblock block states and support rectangular pattern rotation.
- Reduce multiblock validation churn and controller block-state writes.
- Validate active machines every tick and revalidate immediately before recipe start, preventing stale-validity consumption after structure breaks.
- Preserve safely rolled-back active recipes after transactional capability divergence and retry with bounded backoff instead of failing the server tick.
- Replace reflective machine construction with typed factories.
- Fix NeoForge 26.1.2 keyed `MachineBlock` construction by accepting and forwarding the registry-owned `BlockBehaviour.Properties` instance.
- Restore NeoForge 26.1.2 client item definitions for Smart Hammer and Structure Scanner so their existing models render through the modern client-item layer.
- Make Smart Hammer dedicated-server safe, deterministic to its invoking player, resource-preflighted, and non-destructive.
- Make Structure Scanner sync player-targeted, volume-bounded, cross-platform, and generate valid pattern output.
- Remove tracked runtime/local-Maven artifacts and add CI/regression tests.

0.8.1 supersedes the incompatible 0.8 binary for NeoForge 26.1.2 consumers.
