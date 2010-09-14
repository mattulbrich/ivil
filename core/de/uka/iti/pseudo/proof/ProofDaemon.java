package de.uka.iti.pseudo.proof;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author felden@ira.uka.de
 * 
 *         This class executes all jobs, that change the proof tree. This
 *         improves usability and security, as changes dont have to be computed
 *         in UI threads and there is no need for locks anymore.
 * 
 */
public class ProofDaemon {

    /**
     * This represents a task for the daemon. Only run is left to be
     * implemented.
     * 
     * @author felden@ira.uka.de
     * 
     * @param <T>
     *            return type of the job
     */
    public static class Job<T> implements Future<T> {

        /**
         * True, if job was canceled before it started running. After a job was
         * started, there is no way to cancel it. This behavior is needed to
         * ensure atomicity of changes on the proof tree.
         */
        private boolean canceled;

        /**
         * True, if the job is active or done. This interpretation makes state
         * tracking easier.
         */
        private boolean running;

        /**
         * True, if the job finished.
         */
        private boolean done;

        /**
         * This lock is needed to implement get.
         */
        private final ReentrantLock lock = new ReentrantLock();

        /**
         * the result of this job.
         */
        T result;

        /**
         * an exception that might have been thrown by the job.
         */
        Exception exception = null;

        /**
         * As for any other Runnable, you have to implement this method.
         * 
         * @return what ever you want to return
         */
        public T run() {
            return null;
        }

        public boolean cancel() {
            return cancel(false);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning)
                throws IllegalArgumentException {
            if (!running)
                return (canceled = true);
            else if (done)
                return false;
            else if (mayInterruptIfRunning)
                throw new IllegalArgumentException(
                        "It is currently not allowed to cancel half done jobs.");
            return false;
        }

        @Override
        public boolean isCancelled() {
            return canceled;
        }

        @Override
        public boolean isDone() {
            return done;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            if (canceled)
                throw new CancellationException();

            lock.lock();
            if (Thread.interrupted())
                throw new InterruptedException();

            lock.unlock();

            if (exception != null)
                throw new ExecutionException(exception);

            return result;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            if (canceled)
                throw new CancellationException();

            if (!lock.tryLock(timeout, unit))
                throw new TimeoutException();

            if (Thread.interrupted())
                throw new InterruptedException();

            lock.unlock();

            if (exception != null)
                throw new ExecutionException(exception);

            return result;
        }

    }

    private final Proof proof;

    private final LinkedBlockingQueue<Job> jobs;

    ProofDaemon(Proof proof) {
        this.proof = proof;
        jobs = new LinkedBlockingQueue<Job>();
    }

    /**
     * Applies the given rule application safely.
     * 
     * @param ra
     *            the complete rule application
     * @return false, if the rule could not be applied; this can happen if the
     *         rule application would change a non open node
     */
    public Future<Boolean> applyRule(RuleApplication ra) {

        // TODO implementation

        return null;
    }

    /**
     * Removes all children of the given node.
     * 
     * @param node
     *            this node wont have children after this operation
     */
    public void pruneUntil(ProofNode node) {
        // TODO implement
    }

    public <T> void doJob(Job<T> job) {
        // TODO implement
    }

}
