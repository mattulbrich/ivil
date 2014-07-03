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

import java.util.Timer;
import java.util.TimerTask;

import nonnull.NonNull;

public class TimingOutTask extends TimerTask {

    private static Timer timer = null;

    private static Timer getTimer() {
        if(timer == null) {
            timer = new Timer("Timing out timer", true);
        }

        return timer;
    }

    private final Thread thread;

    private final long timeout;

    private boolean hasFinished = false;

    public TimingOutTask(long timeout, @NonNull Thread thread) {
        assert timeout > 0;
        this.thread = thread;
        this.timeout = timeout;
    }

    public TimingOutTask(int timeout) {
        this(timeout, Thread.currentThread());
    }

    public void schedule() {
        getTimer().schedule(this, timeout);
    }

    /* my version is synchronised */
    @Override
    public synchronized boolean cancel() {
        hasFinished = true;
        return super.cancel();
    }

    @Override
    public synchronized void run() {
        if(hasFinished()) {
            return;
        }

        thread.interrupt();
        hasFinished = true;
    }


    /**
     * @return the hasFinished
     */
    public boolean hasFinished() {
        return hasFinished;
    }


}
