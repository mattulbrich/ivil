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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import nonnull.NonNull;
import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;

/**
 * This class allows for pooled automatic proofing of problems. A fixed
 * collection of threads concurrently
 *
 * @author felden@ira.uka.de, mattias ulbrich
 */

public class PooledAutoProver {

    // TODO get thread pool size from environment or something more useful
    /**
     * The size of the thread pool of this prover.
     */
    private final static int POOL_SIZE = 4;

    /**
     * The worker thread which actually works on the proof nodes.
     */
    private final class Worker extends Thread {

        private Worker(int number) {
            super("Auto prove worker " + number);
            Log.log(Log.VERBOSE, "Created new worker %d.", number);
        }

        /**
         * Try to solve node. On success, create new jobs for all children. If
         * exceptions occur here, add them to the exceptions list.
         */
        @Override
        public void run() {
            RuleApplication ra = null;


            try {

                if (isInterrupted()) {
                    Log.log(Log.DEBUG, "Worker terminated");
                    return;
                }

                while(true) {
                    ProofNode node = getNodeFromQueue();

                    ra = strategy.findRuleApplication(node);
                    if (ra != null) {
                        node.getProof().apply(ra);
                        strategy.notifyRuleApplication(ra);
                        applicationsDone.incrementAndGet();
                        waitingQueue.addAll(node.getChildren());

                        synchronized (monitor) {
                            monitor.notifyAll();
                        }

                    } else {
                        unclosableGoalsCount.incrementAndGet();
                        Log.log(Log.TRACE, "could not find a rule application for " + node);
                    }

                }
            } catch (StrategyException e) {
                exceptions.add(e);
                stopAutoProve();
            } catch (ProofException e) {
                if(ra != null) {
                    Log.log(Log.ERROR, Dump.toString(ra));
                }
                exceptions.add(e);
                stopAutoProve();
            } catch (InterruptedException e) {
                Log.log(Log.DEBUG, "Worker has been interrupted, bailing out");
            }
        }

        private ProofNode getNodeFromQueue() throws InterruptedException {
            ProofNode result = waitingQueue.poll();
            while(result == null) {
                if(Thread.interrupted()) {
                    throw new InterruptedException("interrupted while polling queue");
                }

                synchronized (monitor) {
                    waitingThreadsCount ++;

                    // queue is definitely empty
                    if(waitingThreadsCount == workerPool.length) {
                        Log.log(Log.DEBUG, "All threads are waiting, stop auto prove");
                        stopAutoProve();
                    }

                    monitor.wait();

                    result = waitingQueue.poll();

                    waitingThreadsCount --;
                }
            }
            return result;
        }
    };

    /**
     * The producer/consumer list that holds all open goals.
     */
    private final BlockingQueue<ProofNode> waitingQueue =
            new LinkedBlockingQueue<ProofNode>();

    /**
     * The collection of special threads that work on the proof jobs.
     */
    private final Worker[] workerPool;

    /**
     * The number of successfully applied rules.
     */
    private final AtomicInteger applicationsDone = new AtomicInteger();

    /**
     * The number of goals that could not be closed with the current strategy.
     */
    private final AtomicInteger unclosableGoalsCount = new AtomicInteger();

    /**
     * The number of threads currently unemployed. If this reaches the total
     * number of threads, nothing is left to be done.
     *
     * This needs not be atomic since it is synchronised externally using
     * #monitor.
     */
    private int waitingThreadsCount = 0;

    /**
     * The strategy to be used in this search.
     */
    private final Strategy strategy;

    /**
     * Environment to be used by this auto proofer.
     */
    private final Environment env;

    /**
     * This object is used to notify waiting threads and to ensure consistency
     * of workCount.
     */
    private final Object monitor = new Object();

    /**
     * This list is used to keep track of exceptions.
     */
    private final List<Exception> exceptions =
            Collections.synchronizedList(new LinkedList<Exception>());

    /**
     * Instantiates a new pooled auto prover.
     *
     * @param strategy
     *            the strategy to be used to find rule applications
     * @param environment
     *            the environment to be used
     */
    public PooledAutoProver(Strategy strategy, Environment environment) {
        this.strategy = strategy;
        this.env = environment;
        this.workerPool = new Worker[POOL_SIZE];
        for (int i = 0; i < workerPool.length; i++) {
            workerPool[i] = new Worker(i);
        }
    }

    /**
     * Submits a new open proof goal to the job execution system.
     *
     * Does nothing if node has already fixed its children. You may invoke this
     * while other nodes are processed, what will cause the new node to be
     * enqueued on the todo list.
     *
     * @param node
     *            node for which the job is to be enqueued
     */
    public void submit(@NonNull ProofNode node) {
        waitingQueue.add(node);
    }

    /**
     * Start the prove process by starting the threads if not happened yet.
     *
     * <b>Note</b>: it's up to the caller, to call
     * {@link Strategy#beginSearch()} and {@link Strategy#endSearch()}
     */
    public void start() {
        for (Worker worker : workerPool) {
            try {
                worker.start();
            } catch (IllegalThreadStateException e) {
                // just ignore that fact ... it does not hurt ?!
                Log.stacktrace(e);
            }
        }
    }

    /**
     * Waits for all threads to finish(). This call is blocking if any of the
     * threads in the pool is still operating.
     *
     * It does not trigger a shutdown of the system. Do this by calling
     * {@link #stopAutoProve()}.
     *
     * @throws CompoundException
     *             contains the exceptions during the automatic prove process.
     *
     * @throws InterruptedException
     *             if the thread was interrupted while waiting
     */
    public void waitAutoProve() throws CompoundException, InterruptedException {
        for (Worker worker : workerPool) {
            worker.join();
        }

        if (!exceptions.isEmpty()) {
            throw new CompoundException(exceptions);
        }
    }

    /**
     * Asynchronously tells the current automatic proving to stop. This method
     * does not block.
     */
    public void stopAutoProve() {
        for (Worker worker : workerPool) {
            worker.interrupt();
        }

//        shouldStop = true;
    }

    /**
     * Gets the number of already successfully applied {@link RuleApplication}s.
     *
     * @return a non-negative number
     */
    public int getSuccessfullApplicationsCount() {
        return applicationsDone.get();
    }

    /**
     * Gets the number of waiting proof nodes in the queue.
     *
     * @return a non-negative number
     */
    public int getOpenGoalsCount() {
        return waitingQueue.size();
    }

    /**
     * Gets the number of goals for which no rule application could be found.
     *
     * @return a non-negative number
     */
    public int getUnclosableCount() {
        return unclosableGoalsCount.get();
    }
}
