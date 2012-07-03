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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nonnull.NonNull;
import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;

/**
 * This class allows for pooled automatic proofing of problems. A global thread
 * pool is used to solve problems as fast as possible.
 *
 * @author felden@ira.uka.de
 */

public class PooledAutoProver {

    /**
     * Internal representation of a prover job. Jobs will try to recursively
     * solve all problems in parent.todo ????
     */
    private class Job implements Runnable {
        final ProofNode node;

        private Job(ProofNode node) {
            this.node = node;

            synchronized (monitor) {
                workCounter ++;
            }

        }

        /**
         * Try to solve node. On success, create new jobs for all children. If
         * exceptions occur here, add them to the exceptions list.
         */
        @Override
        public void run() {
            RuleApplication ra = null;
            try {

                if (shouldStop) {
                    return;
                }

                try {
                    ra = strategy.findRuleApplication(node);
                } catch (StrategyException e) {
                    exceptions.add(e);
                    return;
                }

                if (ra != null) {
                    try {
                        node.getProof().apply(ra, env);
                        strategy.notifyRuleApplication(ra);
                    } catch (ProofException e) {
                        Log.log(Log.ERROR, Dump.toString(ra));
                        Log.stacktrace(Log.ERROR, e);
                        exceptions.add(e);
                        return;
                    } catch (StrategyException e) {
                        exceptions.add(e);
                        return;
                    }

                    for (ProofNode n : node.getChildren()) {
                        pool.submit(new Job(n));
                    }
                } else {
                    Log.log(Log.TRACE, "could not find a rule application for " + node);

                }

            } finally {
                synchronized (monitor) {
                    if(ra != null) {
                        applicationsDone++;
                    } else {
                        unclosableGoalsCount++;
                    }
                    workCounter --;
                    if (workCounter == 0) {
                        monitor.notifyAll();
                    }
                }
            }
        }
    }

    // TODO get thread pool size from environment or something more useful
    private static int POOL_SIZE = 4;

    // maybe change this to cachedThreadPool, but make strategies parallelize
    // first
    private static ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);

    /**
     * The work counter counts the number of unfinished jobs.
     */
    private int workCounter = 0;

    /**
     * The number of successfully applied rules
     */
    private int applicationsDone = 0;

    /**
     * The number of goals that could not be closed with the current strategy.
     */
    private int unclosableGoalsCount = 0;

    /**
     * The strategy to be used in this search.
     */
    private final Strategy strategy;

    /**
     * Environment to be used by this auto proofer.
     */
    private final Environment env;

    /**
     * Flag to signal jobs, that they should stop.
     */
    private boolean shouldStop = false;

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
     * <b>Note</b>: it's up to the caller, to call strategy.begindSearch() and
     * strategy.endSearch()
     *
     * @param strategy
     *            Strategy to be used by autoProof calls
     * @param environment
     *            Environment to be used
     */
    public PooledAutoProver(Strategy strategy, Environment environment) {
        this.strategy = strategy;
        this.env = environment;
    }

    /**
     * Starts auto proving on the target node. Does nothing if node has
     * children. You may invoke this while other nodes are processed, what will
     * cause the new node to be enqueued on the todo list.
     *
     * @param node
     *            node to be enqueued
     */
    public void autoProve(@NonNull ProofNode node) {
        assert !shouldStop : "automatic prove request after stop";

        pool.submit(new Job(node));
    }

    /**
     * Only usable after all initial nodes have been submitted via autoProve
     *
     * @return true iff no more nodes are to be processed
     */
    public boolean done() {
        return 0 == workCounter;
    }

    /**
     * Waits for current automatic proving to finish.
     *
     * @throws CompoundException
     *             thrown to indicate exceptions were thrown by jobs. The
     *             created exceptions can be retrieved with getException()
     *
     * @throws InterruptedException
     *             rethrown, when interrupted, while waiting
     */
    public void waitAutoProve() throws CompoundException, InterruptedException {
        synchronized (monitor) {
            while (0 != workCounter) {
                monitor.wait();
            }
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
        // code duplication from #stopAutoProve(boolean) to get rid of declared
        // exceptions.
        shouldStop = true;
    }

    /**
     * Asynchronously tells the current automatic proving to stop. This method
     * may block if you set {@code waitForJobs}.
     *
     * @param waitForJobs
     *            set to true if you want to block till all jobs have finished.
     *
     * @throws CompoundException
     *             thrown if some jobs got exceptions
     *
     * @throws InterruptedException
     *             rethrown, when interrupted, while waiting
     */
    public void stopAutoProve(boolean waitForJobs) throws CompoundException, InterruptedException {
        shouldStop = true;
        if (waitForJobs) {
            waitAutoProve();
        }
    }

    /**
     * @return the number of already successfully applied RAs
     */
    public int getSuccessfullApplicationsCount() {
        // note: no synchronization needed here, as reading integers is allways
        // atomic
        return applicationsDone;
    }

    /**
     * @return the number of jobs in the queue
     */
    public int getOpenGoalsCount() {
        return workCounter;
    }

    /**
     * @return the amount of currently unclosable goals, that should have been
     *         clodes by the pool. this can be usefull as indicater to abort
     *         autoproving
     */
    public int getUnclosableCount() {
        return unclosableGoalsCount;
    }
}
