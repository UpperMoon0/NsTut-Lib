package com.nstut.nstutlib.blocks;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineProcessingPolicyTest {
    @Test
    void activeValidationIsBoundedAndMoreFrequentThanIdleValidation() {
        assertTrue(MachineProcessingPolicy.ACTIVE_STRUCTURE_CHECK_INTERVAL_TICKS > 0);
        assertTrue(MachineProcessingPolicy.ACTIVE_STRUCTURE_CHECK_INTERVAL_TICKS
                < MachineProcessingPolicy.IDLE_STRUCTURE_CHECK_INTERVAL_TICKS);
        assertEquals(4, MachineProcessingPolicy.nextStructureCheckCooldown(true));
        assertEquals(19, MachineProcessingPolicy.nextStructureCheckCooldown(false));
    }

    @Test
    void transactionRetryBackoffRemainsPositive() {
        assertTrue(MachineProcessingPolicy.PROCESSING_FAILURE_RETRY_TICKS > 0);
    }
}
