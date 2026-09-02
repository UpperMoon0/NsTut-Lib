# NsTut Lib 0.8 hardening notes

0.8 establishes the transactional machine-processing and multiblock behavior used by Biotech 2.1. Consumers should depend on the 0.8.x compatibility line and must not assume concrete Forge capability storage implementations.

Recoverable capability-commit divergence is reported as `RecipeTransactionException`; the machine layer preserves the active recipe and retries it with bounded backoff. Active machines validate structure every tick, and idle machines revalidate immediately before starting a recipe.
