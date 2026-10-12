package com.nstut.nstutlib.items;

import net.minecraft.world.entity.player.Player;

/**
 * Explicit opt-in for block entities whose removal is safe.
 * This method must be read-only and must veto active transactions or stored resources.
 * A ready decision must return exactly one own block item encoding all persistent cosmetic
 * data. Removal must not emit additional drops. Implementations with an inventory, tank,
 * FE storage or other resources must block until empty; this is not a machine-moving API.
 * The hammer asks again immediately before removal and compares the recovery payload.
 */
public interface SmartHammerRecoverable {
    SmartHammerRecovery prepareSmartHammerReplacement(Player player);
}
