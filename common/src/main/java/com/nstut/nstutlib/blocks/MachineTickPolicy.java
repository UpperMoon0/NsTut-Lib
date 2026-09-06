package com.nstut.nstutlib.blocks;

/**
 * Cross-target scheduling policy for machine validation and transaction retries.
 *
 * Keeping these values in one shared source prevents Forge/NeoForge behavior from
 * drifting when the loader-specific MachineBlockEntity implementations evolve.
 */
final class MachineTickPolicy {
    static final int ACTIVE_STRUCTURE_CHECK_INTERVAL_TICKS = 5;
    static final int IDLE_STRUCTURE_CHECK_INTERVAL_TICKS = 20;
    static final int PROCESSING_FAILURE_RETRY_TICKS = 20;

    private MachineTickPolicy() {
    }

    /**
     * MachineBlockEntity checks when its cooldown reaches zero, so store one less
     * than the human-facing interval to obtain the requested check cadence.
     */
    static int nextStructureCheckCooldown(boolean activeRecipe) {
        int interval = activeRecipe
                ? ACTIVE_STRUCTURE_CHECK_INTERVAL_TICKS
                : IDLE_STRUCTURE_CHECK_INTERVAL_TICKS;
        return Math.max(0, interval - 1);
    }
}
