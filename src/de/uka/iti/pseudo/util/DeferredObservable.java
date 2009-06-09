/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.util;

import java.util.Observable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The Class DeferredObservable provides an observable mechnism in which the
 * observations are stored in a queue. There is a dedicated thread that works on
 * the enqueued observations in FIFO order and invokes the observers. New
 * observations may be put into the queue in the meantime.
 * 
 * This is an asynchronous observable, since the observer may (but do not have
 * to) run in a different thread.
 */

public class DeferredObservable extends Observable {

    /**
     * DispatchThread is the thread to actually cann the observers.
     */
    private static class DispatchThread extends Thread {

        public DispatchThread() {
            super("DeferredObservable.DispatchThread");
        }

        /*
         * retrieve one message from the queue and call the observers.
         * blocks if queue is empty.
         */
        public void run() {
            while (true) {
                Pair<DeferredObservable, Object> event;
                try {
                    event = events.take();
                    assert event != null;
                    DeferredObservable obs = event.fst();
                    Object arg = event.snd();
                    obs.superNotifyObservers(arg);
                } catch (InterruptedException e) {
                    System.err.println("Waiting for event has been interrupted, but I will stay tuned");
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * The dispatching thread is common to all objects of this class
     */
    private static Thread theThread = null;

    /**
     * The queue of events.
     */
    private static BlockingQueue<Pair<DeferredObservable, Object>> events = 
        new LinkedBlockingQueue<Pair<DeferredObservable, Object>>();

    /**
     * Ensure that the thread is running. Create and start it if not so.
     * This is synchronized to avoid overlapping tests.
     */
    private static void ensureRunning() {

        synchronized (events) {
            if (theThread == null) {
                theThread = new DispatchThread();
                theThread.start();
            }
        }

    }

    /**
     * This method is used to allow the dispatching thread to call the method
     * {@link Observable#notifyObservers(Object)} of the superclass.
     * 
     * @param arg
     *            the argument to the observation
     *            
     * @see Observable#notifyObservers(Object)
     */
    protected void superNotifyObservers(Object arg) {
        super.notifyObservers(arg);
    }

    /*
     * delegate this observation to the queue.
     * make sure the thread is up and running
     */
    @Override
    public void notifyObservers(Object arg) {
        ensureRunning();
        events.offer(new Pair<DeferredObservable, Object>(this, arg));
    }

}
