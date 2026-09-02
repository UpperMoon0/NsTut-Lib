# NsTut Lib 0.8

## Fixed
- Make recipe processing transactional and persist active recipe/progress state across reloads.
- Prevent item/fluid output duplication, illegal overstacking, NBT-insensitive matching, and wrong-fluid draining.
- Roll probabilistic outputs once per recipe output and validate output capacity before committing.
- Respect per-ingredient consumable semantics and consume exact remaining energy on the final tick.
- Validate recipe JSON/network payloads and reject incompatible network protocol versions.
- Validate multiblock block states and support rectangular pattern rotation.
- Reduce multiblock validation churn and controller block-state writes.
- Replace reflective machine construction with typed factories.
- Make Smart Hammer dedicated-server safe, deterministic to its invoking player, resource-preflighted, and non-destructive.
- Make Structure Scanner sync player-targeted, volume-bounded, cross-platform, and generate valid pattern output.
- Remove tracked runtime/local-Maven artifacts and add CI/regression tests.
