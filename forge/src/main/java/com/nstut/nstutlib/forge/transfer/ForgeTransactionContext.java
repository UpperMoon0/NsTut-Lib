package com.nstut.nstutlib.forge.transfer;

import com.nstut.nstutlib.transfer.ITransactionContext;

/**
 * A Forge-specific implementation of ITransactionContext.
 * Since Forge capabilities (IItemHandler, IFluidHandler) don't have an explicit
 * global transaction object like Fabric, this context primarily manages an
 * aborted/committed state that can influence how wrapper operations behave.
 */
public class ForgeTransactionContext implements ITransactionContext {
    private boolean aborted = false;
    private boolean committed = false;

    // Private constructor, use static open()
    private ForgeTransactionContext() {
    }

    /**
     * Opens a new transaction context.
     * Meant to be used in a try-with-resources statement.
     */
    public static ForgeTransactionContext open() {
        return new ForgeTransactionContext();
    }

    @Override
    public void abort() {
        if (this.committed) {
            // Optionally, log a warning or allow aborting a committed transaction if semantics allow.
            // For now, strict: cannot abort if already committed.
            throw new IllegalStateException("Transaction has already been committed.");
        }
        this.aborted = true;
    }

    @Override
    public void commit() {
        if (this.aborted) {
            throw new IllegalStateException("Transaction has already been aborted.");
        }
        this.committed = true;
    }

    @Override
    public void close() {
        // Default behavior: if the transaction was not explicitly committed or aborted,
        // commit it upon closing. This is a common pattern for try-with-resources.
        if (!this.committed && !this.aborted) {
            this.commit();
        }
        // If it was aborted, 'aborted' remains true.
        // If it was committed, 'committed' remains true.
    }

    /**
     * Checks if the transaction has been aborted.
     * Used by Forge storage wrappers to alter behavior.
     * @return true if aborted, false otherwise.
     */
    public boolean isAborted() {
        return this.aborted;
    }

    /**
     * Checks if the transaction has been committed.
     * @return true if committed, false otherwise.
     */
    public boolean isCommitted() {
        return this.committed;
    }

    /**
     * Determines if an operation should be simulated based on the transaction's state
     * and the operation's own simulation flag.
     *
     * @param operationWantsToSimulate The simulate flag from the specific storage operation (e.g., IItemStorage.insert).
     * @return true if the operation should be simulated, false otherwise.
     */
    public boolean shouldSimulate(boolean operationWantsToSimulate) {
        if (this.aborted) {
            return true; // If the transaction is aborted, all operations within it are effectively simulated/no-ops.
        }
        // If the transaction is committed, subsequent operations are an issue or outside its scope.
        // For now, if committed, we'd ideally not have more operations. If they occur, they act independently.
        // If not aborted and not yet committed (active transaction), respect the operation's own flag.
        return operationWantsToSimulate;
    }
}
