package de.uka.iti.pseudo.proof;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.uka.iti.pseudo.environment.Environment;

/**
 * @author felden@ira.uka.de
 * 
 *         This class executes all jobs, that change the proof tree. This
 *         improves usability and security, as changes don't have to be computed
 *         in UI threads and there is no need for locks anymore.
 * 
 */
public class ProofDaemon implements Runnable {

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
         * This semaphore is needed to implement get. It will be set to 1, when
         * the job was executed.
         */
        private final Semaphore sem = new Semaphore(0);

        /**
         * the result of this job.
         */
        Object result;

        /**
         * an exception that might have been thrown by the job.
         */
        Exception exception = null;

        /**
         * As for any other Runnable, you have to implement this method.
         * 
         * @return what ever you want to return
         */
        public T run() throws Exception {
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
                        "It is currently not allowed to cancel running half done jobs.");
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

        @SuppressWarnings("unchecked")
        // no error can occur here, as the user has to provide the proper type
        @Override
        public T get() throws InterruptedException, ExecutionException {
            if (canceled)
                throw new CancellationException();

            sem.acquire();
            if (Thread.interrupted())
                throw new InterruptedException();

            sem.release();

            if (exception != null)
                throw new ExecutionException(exception);

            return (T) result;
        }

        @SuppressWarnings("unchecked")
        // no error can occur here, as the user has to provide the proper type
        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            if (canceled)
                throw new CancellationException();

            if (!sem.tryAcquire(timeout, unit))
                throw new TimeoutException();

            if (Thread.interrupted())
                throw new InterruptedException();

            sem.release();

            if (exception != null)
                throw new ExecutionException(exception);

            return (T) result;
        }
    }

    private final Proof proof;

    /**
     * The daemons job queue.
     */
    private final LinkedBlockingQueue<Job<?>> jobs;

    /**
     * The Thread that is used to do the work.
     */
    final Thread thread;

    ProofDaemon(Proof proof) {
        this.proof = proof;
        jobs = new LinkedBlockingQueue<Job<?>>();

        thread = new Thread(this);
        thread.setDaemon(true);
        thread.setName("ProofDaemon of " + proof.toString());
        thread.start();
    }

    /**
     * Applies the given rule application safely.
     * 
     * @param ra
     *            the complete rule application
     * @param env
     *            the environment to be used
     * 
     * @return false, if the rule could not be applied; this can happen if the
     *         rule application would change a non open node
     */
    public Future<Boolean> applyRule(final RuleApplication ra,
            final Environment env) {
        Job<Boolean> rval = new Job<Boolean>() {
            public Boolean run() throws ProofException {
                if (ra.getProofNode().getChildren() != null)
                    return false;

                proof.apply(ra, env);
                return true;
            }
        };

        jobs.add(rval);

        return rval;
    }

    /**
     * Removes all children of the given node.
     * 
     * @param node
     *            this node wont have children after this operation
     */
    public void pruneUntil(final ProofNode node) {
        jobs.add(new Job<Void>() {
            public Void run() throws ProofException {
                proof.prune(node);
                return null;
            }
        });
    }

    /**
     * Enques a job. After the job is done, all observers of proof will we
     * notified.
     * 
     * @param <T>
     *            Result type of the job
     * @param job
     *            The job to be enqueued
     */
    public <T> void addJob(Job<T> job) {
        jobs.add(job);
    }

    /**
     * Creates a job from a runnable, enqueues it and returns the appropriate
     * future. After the job is done, all observers of proof will we notified.
     * 
     * @param job
     * @return The future belonging to the job
     */
    public Future<Void> addJob(final Runnable job) {
        Job<Void> rval = new Job<Void>() {
            public Void run() {
                job.run();
                return null;
            }
        };
        addJob(rval);
        return rval;
    }

    public boolean isIdle() {
        return jobs.isEmpty() && thread.getState() == Thread.State.WAITING;
    }

    /**
     * Waits for jobs and executes them.
     */
    @Override
    public void run() {
        Job<?> job = null;

        // NOTE: the daemon is not meant to be halted. If you want to collect a
        // complete proof it should be safe to stop it.

        // TODO ensure threads will be collected by the garbage collector; maybe
        // a weak reference is needed here
        while (true) {
            try {
                job = jobs.take();
                job.running = true;
            } catch (InterruptedException e1) {
                // can not happen, as no one will interrupt the daemon

                // if someone broke my concept and we actually were interrupted,
                // we need to return to the while statement, as job would be
                // null or an old job
                continue;
            }
            try{
                job.result = (Object) job.run();
            } catch (Exception e) {
                // if an exception was thrown, safe it
                job.exception = e;
            } finally {
                job.done = true;
                job.sem.release();
            }
        }
    }

}
