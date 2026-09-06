package com.nstut.nstutlib.blocks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineTickPolicyTest {
    @Test
    void activeValidationIsBoundedAndMoreFrequentThanIdleValidation() {
        assertTrue(MachineTickPolicy.ACTIVE_STRUCTURE_CHECK_INTERVAL_TICKS > 0);
        assertTrue(MachineTickPolicy.ACTIVE_STRUCTURE_CHECK_INTERVAL_TICKS
                < MachineTickPolicy.IDLE_STRUCTURE_CHECK_INTERVAL_TICKS);
        assertEquals(4, MachineTickPolicy.nextStructureCheckCooldown(true));
        assertEquals(19, MachineTickPolicy.nextStructureCheckCooldown(false));
    }

    @Test
    void transactionRetryBackoffRemainsPositive() {
        assertTrue(MachineTickPolicy.PROCESSING_FAILURE_RETRY_TICKS > 0);
    }
}
