/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2013 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.util;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import de.uka.iti.pseudo.util.ProgressIndicator;

/**
 * The Class ProgressMonitorIndicator relays all progress indiciation to a
 * {@link ProgressMonitor}.
 *
 * Calls to {@link #setProgress(int)} are also made thread-safe such that they
 * may also be called on any thread, not only the AWT event queue.
 */
public class ProgressMonitorIndicator implements ProgressIndicator {

    private final ProgressMonitor monitor;

    public ProgressMonitorIndicator(ProgressMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void close() {
        monitor.close();
    }

    @Override
    public void setMaximum(int value) {
        monitor.setMaximum(value);
    }

    @Override
    public void setProgress(final int progress) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                monitor.setProgress(progress);
            }
        });
    }
}
