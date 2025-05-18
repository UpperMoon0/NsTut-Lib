package com.nstut.nstutlib.transfer;

import java.io.Closeable;

/**
 * Represents a transaction that can be committed or aborted.
 * Implementations should ensure that operations within a transaction
 * are atomic and isolated until committed.
 */
public interface ITransaction extends Closeable {
    /**
     * Commits the changes made during this transaction.
     */
    void commit();

    /**
     * Aborts the changes made during this transaction, rolling them back.
     */
    void abort();

    /**
     * Closes this transaction, typically by aborting if not already committed.
     * This is to support try-with-resources.
     */
    @Override
    void close(); // Equivalent to abort() if not committed

    /**
     * Opens a nested transaction.
     *
     * @return A new nested transaction.
     */
    ITransaction openNested();


    /**
     * A utility class to manage transactions.
     */
    class TransactionUtil {
        private static ThreadLocal<ITransaction> CURRENT_TRANSACTION = new ThreadLocal<>();

        /**
         * Opens a new transaction or returns the current one if already open.
         * This needs to be platform-specific. This is a placeholder.
         *
         * @return The current or new transaction.
         * @throws IllegalStateException if the platform-specific transaction provider is not available.
         */
        public static ITransaction open() {
            // Platform-specific implementation will be needed here,
            // possibly using ServiceLoader to find a provider.
            // For now, this will not work without a concrete implementation.
            // This is a conceptual placeholder.
            ITransaction current = CURRENT_TRANSACTION.get();
            if (current != null) {
                return current.openNested();
            }
            // In a real scenario, you'd use ServiceLoader to get a TransactionProvider
            // ITransactionProvider provider = ServiceLoader.load(ITransactionProvider.class).findFirst()
            // .orElseThrow(() -> new IllegalStateException("No ITransactionProvider found"));
            // current = provider.createTransaction();
            // CURRENT_TRANSACTION.set(current);
            // return current;
            throw new UnsupportedOperationException("Transaction handling not implemented yet. Platform-specific provider needed.");
        }

        /**
         * Gets the current transaction, if one is open.
         *
         * @return The current transaction, or null if none is open.
         */
        public static ITransaction current() {
            return CURRENT_TRANSACTION.get();
        }

        // Internal methods for providers to set/clear the transaction
        protected static void setCurrent(ITransaction transaction) {
            CURRENT_TRANSACTION.set(transaction);
        }

        protected static void clearCurrent() {
            CURRENT_TRANSACTION.remove();
        }
    }

    /**
     * Interface for a provider that can create transactions.
     * To be implemented by platform-specific modules (Forge/Fabric).
     */
    interface ITransactionProvider {
        ITransaction createTransaction();
    }
}
