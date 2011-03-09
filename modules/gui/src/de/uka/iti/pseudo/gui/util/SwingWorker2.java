/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.util;

import java.util.concurrent.CancellationException;

import javax.swing.SwingWorker;

import nonnull.Nullable;

/**
 * The Class SwingWorker2 is a simple extension of {@link SwingWorker}. It
 * allows the extraction of the exception possibly thrown during
 * {@link #doInBackground()}.
 */
public abstract class SwingWorker2<T, V> extends SwingWorker<T, V> {

    /**
     * Gets the exception thrown during {@link #doInBackground()}.
     * 
     * @return the exception as it would be thrown by the {@link #get()} method.
     *         <code>null</code> if no exception is available.
     */
    protected final @Nullable Exception getException() {
        try {
            get();
            return null;
        } catch(CancellationException cex) {
            return null;
        } catch(Exception ex) {
            return ex;
        }
    }
    
}
