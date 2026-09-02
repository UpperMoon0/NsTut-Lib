# Recipe transactions in 0.8

`MachineBlockEntity.processRecipeTransaction` centralizes selection, one-time input consumption, exact energy progress, persisted active recipe state, output-capacity checks and final output commit.

Input and output commits now snapshot restorable Forge handlers before mutation and roll the complete item/fluid state back if any handler rejects or diverges during execution. Transactional machine handlers must therefore expose item storage as `IItemHandlerModifiable` and fluid storage as `FluidTank` (subclasses are supported). The commit is refused before mutation when a handler cannot be restored safely.

A rolled-back capability divergence raises `RecipeTransactionException`; the machine layer catches only that typed recoverable failure, preserves the active recipe, and retries after a bounded backoff instead of escaping the server tick. Active machines validate their multiblock every tick, and an idle machine performs a fresh validation immediately before starting a recipe, so stale structure validity cannot consume inputs.

Recipe matching remains conservative for probabilistic outputs: capacity is reserved as though every chance output succeeds, so a successful roll cannot overfill the destination.
