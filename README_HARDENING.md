# NsTut Lib 0.8.1 hardening notes

0.8.1 establishes the supported transactional machine-processing and multiblock behavior used by Biotech 2.1. Consumers should depend on `0.8.1` or newer within the 0.8.x compatibility line and must not assume concrete Forge capability storage implementations.

Recoverable capability-commit divergence is reported as `RecipeTransactionException`; the machine layer preserves the active recipe and retries it with bounded backoff. Active machines validate structure every tick, and idle machines revalidate immediately before starting a recipe.

For NeoForge 26.1.2, the corrected `MachineBlock` API consumes the keyed `BlockBehaviour.Properties` provided by `DeferredRegister.registerBlock`. The earlier 0.8 binary is not a valid substitute for 0.8.1 on that target.
