/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.bar;

import java.util.EventObject;

import javax.swing.Action;
import javax.swing.Icon;

// TODO Documentation needed
public interface StateListener {
    
    public static class StateChangeEvent extends EventObject {
        
        public static final String INITIALISED = "initialised";

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
