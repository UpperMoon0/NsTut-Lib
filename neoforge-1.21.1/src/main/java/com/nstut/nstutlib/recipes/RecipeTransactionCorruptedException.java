package com.nstut.nstutlib.recipes;

/**
 * Signals that a recipe capability commit failed and its rollback also failed.
 * The active recipe must not be retried because the handler state may already be partially mutated.
 */
public final class RecipeTransactionCorruptedException extends IllegalStateException {
    public RecipeTransactionCorruptedException(String message, Throwable commitFailure, Throwable rollbackFailure) {
        super(message, commitFailure);
        addSuppressed(rollbackFailure);
    }
}
