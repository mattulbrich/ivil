package de.uka.iti.pseudo.util;

import java.util.LinkedList;
import java.util.List;

public class NotificationSupport {
    
    private static class SignalListener implements NotificationListener {
        private NotificationListener listener;
        private String signal;

        private SignalListener(NotificationListener listener, String signal) {
            this.listener = listener;
            this.signal = signal;
        }

        public void handleNotification(NotificationEvent event) {
            if(signal.equals(event.getSignal())) {
                listener.handleNotification(event);
            }
        }
    }
    
    private Object source;
    
    private List<NotificationListener> listeners = new LinkedList<NotificationListener>();
    

    public NotificationSupport(Object source) {
        this.source = source;
    }
    
    public void addNotificationListener(String signal, NotificationListener listener) {
        addNotificationListener(new SignalListener(listener, signal));
    }

    public void addNotificationListener(NotificationListener listener) {
        listeners.add(listener);
    }

    public void fireNotification(String signalName, Object... parameters) {
        NotificationEvent event = new NotificationEvent(source, signalName, parameters);
        for (NotificationListener listener : listeners) {
            listener.handleNotification(event);
        }
    }

    public void removeNotificationListener(String signal,
            NotificationListener listener) {
        
    }

}
