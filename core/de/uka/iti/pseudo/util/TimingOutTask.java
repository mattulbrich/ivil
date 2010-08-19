package de.uka.iti.pseudo.util;

import java.util.Timer;
import java.util.TimerTask;

public class TimingOutTask extends TimerTask {
    
    private static Timer timer = null;
    
    private static Timer getTimer() {
        if(timer == null) {
            timer = new Timer("Timing out timer", true);
        }
        
        return timer;
    }
    
    private Process process;
    private Thread thread;
    
    private long timeout;
    
    private boolean hasFinished = false; 

    public TimingOutTask(long timeout, Process process) {
        this.timeout = timeout;
        this.process = process;
    }
    
    public TimingOutTask(long timeout, Thread thread) {
        this.thread = thread;
        this.timeout = timeout;
    }
    
    public TimingOutTask(int timeout) {
        this.timeout = timeout;
        this.thread = Thread.currentThread();
    }

    public void schedule() {
        getTimer().schedule(this, timeout);
    }

    @Override 
    public void run() {
        if(process != null) {
            try {
                process.exitValue();
            } catch (IllegalThreadStateException ex) {
                // was still running.
                process.destroy();
            }
            hasFinished = true;
        } else if (thread != null) {
            thread.interrupt();
            hasFinished = true;
        } else {
            assert false : "Either a process or a thread must be given";
        }
    }

    /**
     * @return the hasFinished
     */
    public boolean hasFinished() {
        return hasFinished;
    }
    
    
}
