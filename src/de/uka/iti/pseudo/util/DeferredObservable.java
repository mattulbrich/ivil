package de.uka.iti.pseudo.util;

import java.util.Observable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DeferredObservable extends Observable {
    
    private static class DispatchThread extends Thread {
        
        public DispatchThread() {
            super("DeferredObservable.DispatchThread");
        }
        
        public void run() {
            while(true) {
                Pair<DeferredObservable, Object> event;
                event = events.poll();
                DeferredObservable obs = event.fst();
                Object arg = event.snd();
                obs.superNotifyObservers(arg);
            }
        }
        
    }
    
    private static Thread theThread = null;
    
    private static BlockingQueue<Pair<DeferredObservable, Object>> events = 
        new LinkedBlockingQueue<Pair<DeferredObservable,Object>>();
    
    private static void ensureRunning() {
        
        synchronized(events) {
            if(theThread == null) {
                theThread = new DispatchThread();
                theThread.start();
            }
        }
        
    }
    
    protected void superNotifyObservers(Object arg) {
        super.notifyObservers(arg);
    }
    
    @Override
    public void notifyObservers(Object arg) {
        ensureRunning();
        events.offer(new Pair<DeferredObservable, Object>(this, arg));
    }

}
