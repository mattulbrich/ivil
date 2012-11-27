/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * This {@link Future} implementation is a corner case wrapping the result into
 * a future without implementation.
 *
 * @param <V>
 *            the value type
 */
public class CompletedFuture<V> implements Future<V> {

    private final V value;
    private final Throwable exception;

    public CompletedFuture(V value) {
        this.value = value;
        this.exception = null;
    }

    public CompletedFuture(V value, Throwable exception) {
        this.value = value;
        this.exception = exception;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public V get() throws ExecutionException {
        if(exception != null) {
            throw new ExecutionException(exception);
        } else {
            return value;
        }
    }

    @Override
    public V get(long timeout, TimeUnit unit)
            throws ExecutionException{
        return get();
    }

}
