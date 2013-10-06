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


/**
 * The Interface ProgressIndicator is used to publish progress within a process.
 * This is then used to inform the user about ongoing longer processes.
 *
 * An integer value between 0 and a definable maximum value is used to indicate
 * the progress of the process.
 */
public interface ProgressIndicator {

    /**
     * The process has finished, indication will finish.
     */
    public void close();

    /**
     * Sets the maximum number for the action.
     * This resets the indicator and the progress is set to 0.
     *
     * @param value
     *            the maximum number of steps, positive number
     */
    public void setMaximum(int value);

    /**
     * Sets the current progress.
     *
     * The new progress must be at least as higher than the old progress and
     * must not exceed the maximum.
     *
     * @param progress
     *            the new progress
     */
    public void setProgress(int progress);
}
