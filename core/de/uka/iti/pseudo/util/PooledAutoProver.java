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
     * solve all problems in parent.todo
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
        public void run() {
            try {
                if (shouldStop)
                    return;

                RuleApplication ra;
                try {
                    ra = strategy.findRuleApplication(node);
                } catch (StrategyException e) {
                    exceptions.add(e);
                    return;
                }

                if (null != ra) {
                    try {
                        node.getProof().apply(ra, env);
                    } catch (ProofException e) {
                        exceptions.add(e);
                        return;
                    }

                    for (ProofNode n : node.getChildren()) {
                        pool.submit(new Job(n));
                    }
                } else {
                    Log.log(Log.TRACE, "could not find a rule application for %s\n", node.toString());
                }

            } finally {
                synchronized (monitor) {
                    workCounter --;
                    if (workCounter == 0) {
                        strategy.endSearch();
                        Log.log(Log.TRACE, "strategy.endSearch()");
                        monitor.notifyAll();
                    }
                }
            }
        }
    }

    // TODO get thread pool size from environment or something more useful
    private static int POOL_SIZE = 16;
    // maybe change this to cachedThreadPool, but make strategies parallelize
    // first
    private static ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);

    /**
     * The work counter counts the number of unfinished jobs.
     */
    private int workCounter = 0;

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
    private Object monitor = new Object();

    /**
     * This list is used to keep track of exceptions.
     */
    private List<Exception> exceptions = Collections.synchronizedList(new LinkedList<Exception>());

    /**
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
     * 
     * @throws StrategyException
     *             will be thrown if strategy.beginSearch() throws an exception
     */
    public void autoProve(@NonNull ProofNode node) throws StrategyException {
        assert !shouldStop : "automatic prove request after stop";

        synchronized (monitor) {
            if (0 == workCounter)
            strategy.beginSearch();
            Log.log(Log.TRACE, "strategy.beginSearch()");
        }

        pool.submit(new Job(node));
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
            if (0 != workCounter) {
                monitor.wait();
            }
        }
        
        if (!exceptions.isEmpty())
            throw new CompoundException(exceptions);
    }

    /**
     * Stops the current automatic proving not waiting for its end.
     * 
     * @throws CompoundException
     *             thrown if some jobs got exceptions
     * 
     * @throws InterruptedException
     *             rethrown, when interrupted, while waiting
     */
    public void stopAutoProve() throws CompoundException, InterruptedException {
        stopAutoProve(false);
    }

    /**
     * Stops the current automatic proving.
     * 
     * @param waitForJobs
     *            set to true if you want to reuse the auto proofer.
     * 
     * @throws CompoundException
     *             thrown if some jobs got exceptions
     * 
     * @throws InterruptedException
     *             rethrown, when interrupted, while waiting
     */
    public void stopAutoProve(boolean waitForJobs) throws CompoundException, InterruptedException {
        shouldStop = true;
        if (waitForJobs)
            waitAutoProve();
    }
}