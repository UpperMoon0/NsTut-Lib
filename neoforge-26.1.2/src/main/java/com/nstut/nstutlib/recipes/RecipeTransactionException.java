package com.nstut.nstutlib.recipes;

/**
 * Signals a recipe capability commit that failed without leaving a partial mutation behind.
 * Machine processing may safely preserve the active recipe and retry these failures later.
 */
public final class RecipeTransactionException extends IllegalStateException {
    public RecipeTransactionException(String message) {
        super(message);
    }

    public RecipeTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
