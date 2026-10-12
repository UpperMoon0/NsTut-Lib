# Smart Hammer replacement

Shift-use the hammer on a machine controller. The controller is retained and the current preview pattern is the only edit scope. Null pattern cells and positions outside that pattern are untouched. This is not an old-structure scanner or a general machine relocation tool.

## Ordinary blocks and materials

The hammer skips matching cells, replaces ordinary single-cell blocks from inventory, and returns each removed block item exactly once. Glass and stone are recovered as their block items rather than mined drops. Farmland/path recover dirt. Non-item replaceable states such as air/water have no invented recovery item; no filled water bucket is minted from world fluid. New water consumes a plain water bucket and returns an empty bucket.

Neighbour-dependent states are resolved against the live world before each placement. Fence, pipe and stair connection updates can adjust earlier placements without extra material consumption. Matching verifies the authored pattern requirements rather than unspecified default connection properties.

Required air cells consume no material. Same-block state corrections reuse the existing block without an extra item/refund. New material stacks must match a plain stack of that item: a renamed/component-bearing storage item is never silently consumed while its data is discarded.

Recovered items go into the player's inventory. Any remainder from a full inventory drops at the player's feet. Creative mode skips material consumption but does not bypass replacement safety.

## Blockers and permissions

Every pending cell is checked before the first tool mutation: loaded position, build bounds, world border, player build/spawn permissions, recovery support, native cancellable break/place events and the complete material budget. The placement preflight event exposes the desired state while its snapshot still records the original state; no block is temporarily overwritten just to ask permission.

Failure reports `Blocked by <block> at <x, y, z>: <reason>`. Unsupported entities, populated containers, multi-block doors/plants/piston heads, unavailable positions and protected blocks stop the plan. Protected operator/creative infrastructure includes bedrock, all command blocks, barriers, light, structure/structure-void/jigsaw blocks, portal frames/portals/gateways, moving pistons, GameMasterBlock implementations and negative-hardness blocks. These remain untouched in all game modes.

All supported entities are rechecked before removal. State, block-entity identity or recovery-payload changes stop safely. A placement failure attempts snapshot rollback; its consumed item is refunded only after rollback succeeds. This is per-step failure safety: completed earlier placements remain in the world if a later callback/world change interrupts the build. Reuse the hammer to replan the remaining cells.

## Supported block entities

The default is deny. Exact vanilla chest/barrel classes are accepted only if empty and their saved keys are limited to vanilla identity/position/items. Names, locks, unopened loot tables, attachments or other saved metadata block the built-in path rather than being silently discarded. Subclasses are not treated as plain vanilla containers.

Other mod block entities may implement:

```java
public interface SmartHammerRecoverable {
    SmartHammerRecovery prepareSmartHammerReplacement(Player player);
}
```

This method must be read-only. Return `SmartHammerRecovery.blocked("stored fluid")`, `blocked("active transaction")` or another short reason until every stored resource is empty and removal is safe. A ready decision returns exactly one own block item that preserves all remaining persistent cosmetic state. Item payloads are copied and compared again before committing.

The implementation must guarantee that removal emits no additional item/content drops. Do not opt in a populated inventory/tank/FE buffer or active controller; this API is intentionally for recoverable empty entities, not transaction or resource transfer. Recovery exceptions, null decisions, wrong items and invalid counts are rejected.

Existing consumer hatches/machines do not become supported automatically. They must implement this contract or be dismantled safely by the player.

## Validation

The PR adds native Forge build/refund/required-air tests, no-partial-preflight tests, real empty/populated chest checks, creative protection checks and break/place cancellation tests. All three targets compile the same shared policy/API; NeoForge unit tests exercise ordinary block recovery, protected infrastructure, saved-data/container rejection, explicit support and defensive recovery copies.
