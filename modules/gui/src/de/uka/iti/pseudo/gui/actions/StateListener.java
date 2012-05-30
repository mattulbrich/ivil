/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions;

import java.util.EventObject;

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
