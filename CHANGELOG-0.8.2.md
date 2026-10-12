# NsTut Lib 0.8.2

- Add Smart Hammer replacement of ordinary single-cell blocks and safe, explicitly supported block entities.
- Recover replaced block items into inventory, with visible overflow drops. Clear required air positions without consuming an item; same-block state corrections do not consume/refund an extra block.
- Preflight the complete edit/material plan before mutations, respect build permissions and cancellable native break/place events, recheck world/entity/recovery state before each placement, and refund a failed step's material only after successful rollback.
- Protect creative/operator infrastructure and all unbreakable blocks in every game mode. Unsupported entities, populated containers and unsafe multi-block structures report a blocker and stop preflight.
- Support empty vanilla chests/barrels only when their saved data contains no names, locks, loot tables or extra payload. Add a read-only, default-deny `SmartHammerRecoverable` API for other empty entities.
- Compile all three supported targets and add NeoForge policy tests plus Forge native replacement/permission/container/protected-block GameTests.

- Reject quantity-bearing states and compound blocks instead of losing or creating their stored resources. Double slabs, candles/pickles, filled flower pots and candle cakes require manual recovery; both replacement and same-block quantity corrections fail closed.
- Resolve neighbor-dependent placement states for fences/pipes and preserve exact water-bucket accounting.
- Preserve the recipe-preparation API baseline required by current Biotech consumers.
- Publish the changelog corresponding to the actual release version on GitHub and CurseForge.
