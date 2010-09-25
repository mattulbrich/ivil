package de.uka.iti.pseudo.util;

import java.util.EventObject;

public class NotificationEvent extends EventObject {

    private static final long serialVersionUID = -4350007839854109727L;
    
    private String signal;
    
    private Object[] parameters;

    public NotificationEvent(Object source, String signal, Object... parameters) {
        super(source);
        this.signal = signal;
        this.parameters = parameters;
    }

    /**
     * @return the signal
     */
    public String getSignal() {
        return signal;
    }

    /**
     * @return the parameters
     */
    public Object getParameter(int index) {
        return parameters[index];
    }
    
    public int countParameters() {
        return parameters.length;
    }

    public boolean isSignal(String signal) {
        return this.signal.equals(signal);
    }
}
