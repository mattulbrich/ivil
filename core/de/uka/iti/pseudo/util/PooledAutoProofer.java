package de.uka.iti.pseudo.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

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
public class PooledAutoProofer {
    private static class Job implements Runnable {
        final PooledAutoProofer parent;
        final ProofNode node;

        Job(PooledAutoProofer parent, ProofNode node) {
            this.parent = parent;
            this.node = node;

            parent.workCounter.incrementAndGet();
        }
        
        public void run() {
            try {
                if (parent.shouldStop)
                    return;

                RuleApplication ra;
                try {
                    ra = parent.strategy.findRuleApplication(node);
                } catch (StrategyException e) {
                    return; // we don't care
                }
                
                if (null != ra) {
                    try {
                        node.getProof().apply(ra, parent.env);
                    } catch (ProofException e) {
                        return; // we don't care
                    }

                    for (ProofNode n : node.getChildren()) {
                        todo.add(new Job(parent, n));
                    }
                }
            
            } finally {
                if (0 == parent.workCounter.decrementAndGet())
                    parent.wait.release();
            }
        }
    }
    
    private static class Worker implements Runnable {
        Job job;

        public void run() {
            while(true){
                try {
                    job = todo.take();
                } catch (InterruptedException e) {
                    // can happen, if someone calls pool.shutdownNow()
                    return;
                }
                job.run();
            }
        }
    }

    // TODO get thread pool size from environment or something more useful
    private static int POOL_SIZE = 16;
    private static ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);

    /**
     * Global to do list, which will be fetched by the workers in the pool.
     */
    private static LinkedBlockingQueue<Job> todo = new LinkedBlockingQueue<Job>();
    
    /**
     * Create worker threads
     */
    static{
        for (int i = 0; i < POOL_SIZE; i++)
            pool.submit(new Worker());
    }

    /**
     * This counter tells us how many of our jobs are still in the todo list.
     * 
     * This counter is meant to be used by jobs only.
     */
    private final AtomicInteger workCounter = new AtomicInteger(0);

    /**
     * The strategy to be used in this search.
     */
    public Strategy strategy;

    /**
     * Environment to be used by this auto proofer.
     */
    public Environment env;

    /**
     * Flag to signal jobs, that they should stop.
     */
    private boolean shouldStop = false;

    /**
     * This semaphore is used to implement wait, it can only have values 1 and 0
     */
    private Semaphore wait = new Semaphore(0);

    /**
     * @param strategy
     *            Strategy to be used by autoProof calls
     * @param environment
     *            Environment to be used
     */
    public PooledAutoProofer(Strategy strategy, Environment environment) {
        this.strategy = strategy;
        this.env = environment;
    }

    /**
     * Starts autoproofing on the target node. Does nothing if node has
     * children. You may invoke this while other nodes are processed, what will
     * cause the new node to be enqueued on the todolist.
     */
    public void autoProof(@NonNull ProofNode node) {
        if (null == node.getChildren()) {
            shouldStop = false;
            // ensure the semaphore is empty
            wait.tryAcquire();
            todo.add(new Job(this, node));
        }
    }

    /**
     * This function allows to start auto proofing with new strategy and
     * environments.
     * <p>
     * The purpose of this function is to reduce recreation of PooledAutoProofer
     * Objects when used for example as auto proof action in GUI mode.
     * <p>
     * <b>NOTE:</b> if you call this while buzy, with another strategy or
     * environment, the old job will be canceled; in this case, the function
     * will block until all jobs were canceled.
     */
    public void autoProof(@NonNull ProofNode node, @NonNull Strategy strategy,
            @NonNull Environment env) {
        if (workCounter.get() != 0
                && (this.strategy != strategy || this.env != env)) {
            stopAutoProof(true);
        }
        this.strategy = strategy;
        this.env = env;
        autoProof(node);
    }

    /**
     * Waits for current autocmatic proofing to finish.
     */
    public void waitAutoProof() {
        try {
            wait.acquire();
        } catch (InterruptedException e) {
            return;
        }
        wait.release();
    }

    /**
     * @see stopAutoProof(false)
     */
    public void stopAutoProof() {
        stopAutoProof(false);
    }

    /**
     * Stops the current automatic proofing.
     * 
     * @param waitForJobs
     *            set to true if you want to reuse the auto proofer.
     */
    public void stopAutoProof(boolean waitForJobs) {
        shouldStop = true;
        if (waitForJobs)
            waitAutoProof();
    }
}