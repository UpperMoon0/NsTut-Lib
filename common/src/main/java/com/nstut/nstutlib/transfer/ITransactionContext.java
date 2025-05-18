package com.nstut.nstutlib.transfer;

/**
 * Interface for custom transaction context.
 */
public interface ITransactionContext extends AutoCloseable {
    /**
     * Aborts the transaction.
     */
    void abort();

    /**
     * Commits the transaction.
     */
    void commit();

    /**
     * Closes the transaction, committing it if not already aborted.
     */
    @Override
    void close();
}
