package de.uka.iti.pseudo.gui.bar;

import java.util.EventObject;

import javax.swing.Action;

// TODO Documentation needed
public interface StateListener {
    
    public interface StateListeningAction extends Action, StateListener {}

    public static class StateChangeEvent extends EventObject {

        private static final long serialVersionUID = -7918620500210682982L;

        private String state;
        
        private boolean active;

        public StateChangeEvent(Object source, String state, boolean active) {
            super(source);
            this.state = state;
            this.active = active;
        }

        public String getState() {
            return state;
        }

        public boolean isActive() {
            return active;
        }
    }
    
    public void stateChanged(StateChangeEvent e);
    
}
