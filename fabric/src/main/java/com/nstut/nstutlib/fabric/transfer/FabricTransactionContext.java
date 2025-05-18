package com.nstut.nstutlib.fabric.transfer;

import com.nstut.nstutlib.transfer.ITransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class FabricTransactionContext implements ITransactionContext {
    final Transaction transaction; // Made package-private
    private boolean committed = false;
    private boolean aborted = false;

    /**
     * Opens a new top-level transaction.
     * This is meant to be used in a try-with-resources statement.
     * e.g.: try (FabricTransactionContext ctx = FabricTransactionContext.open()) { ... ctx.commit(); }
     */
    public static FabricTransactionContext open() {
        return new FabricTransactionContext(Transaction.openOuter());
    }

    /**
     * Opens a nested transaction within an existing Fabric TransactionContext.
     * This is meant to be used in a try-with-resources statement.
     */
    public static FabricTransactionContext openNested(TransactionContext outerCtx) {
        if (outerCtx == null) {
            // If no outer Fabric context, open a new top-level one.
            return new FabricTransactionContext(Transaction.openOuter());
        }
        return new FabricTransactionContext(Transaction.openNested(outerCtx));
    }
    
    /**
     * Opens a nested transaction within an existing ITransactionContext (which might be a FabricTransactionContext).
     * This is meant to be used in a try-with-resources statement.
     */
    public static FabricTransactionContext openNested(ITransactionContext outerCtx) {
        if (outerCtx instanceof FabricTransactionContext fabricOuterCtx) {
            return new FabricTransactionContext(Transaction.openNested(fabricOuterCtx.transaction));
        } else {
            // If the outer context is not a Fabric one, or null, open a new top-level Fabric transaction.
            // This might not be ideal if true cross-platform nested transactions are needed,
            // but it's a safe default for Fabric-specific operations.
            return new FabricTransactionContext(Transaction.openOuter());
        }
    }


    private FabricTransactionContext(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getFabricTransaction() {
        return transaction;
    }

    @Override
    public void abort() {
        if (!committed && !aborted) {
            transaction.abort();
            aborted = true;
        }
    }

    @Override
    public void commit() {
        if (!aborted && !committed) {
            transaction.commit();
            committed = true;
        }
    }

    @Override
    public void close() {
        // This implements AutoCloseable.
        // If neither commit nor abort has been called, Fabric's Transaction.close() will abort by default.
        if (!committed && !aborted) {
            // transaction.abort(); // Fabric's default behavior on close if not committed.
        }
        transaction.close();
    }
}
