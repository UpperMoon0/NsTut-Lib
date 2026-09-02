# Recipe transactions in 0.8

`MachineBlockEntity.processRecipeTransaction` centralizes selection, one-time input consumption, exact energy progress, persisted active recipe state, output-capacity checks and final output commit.

Input and output commits preflight and snapshot restorable Forge handlers before mutation. Transactional item storage must implement `IItemHandlerModifiable`, and transactional fluid storage must be a `FluidTank` (subclasses are supported). `ModRecipe.requireRestorableStorage` rejects unsupported handlers before any transaction mutation occurs.

Probabilistic item outputs are rolled once when an active recipe starts. The selected output indexes are persisted with the machine transaction, so chunk/world reloads and safe rollback retries reuse the exact same result instead of rerolling. Pre-start recipe matching remains conservative and reserves capacity as though every chance output succeeds; once a recipe is active, completion checks the persisted rolled output set.

A capability commit divergence whose snapshot restores successfully raises `RecipeTransactionException`; the machine preserves the active recipe and retries after a bounded backoff. If restoration itself fails, the transaction instead raises `RecipeTransactionCorruptedException`. That state is deliberately non-retriable: the machine cancels the active recipe and logs a severe error rather than risking duplicate output or repeated input consumption.

Active machines validate their multiblock every tick, and an idle machine performs a fresh validation immediately before starting a recipe, so stale structure validity cannot consume inputs. An invalid structure pauses an already active transaction without discarding its persisted recipe, progress, consumed-input flag, or rolled-output decisions.
