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

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * This is a generic action to allow the creation of toggle actions by the
 * specification of a property. The action is initialised from the property read
 * from the {@link Settings}. It listens to the property on the
 * {@link ProofCenter} and it fires {@link PropertyChangeEvent}s if the boolean
 * value changes.
 * 
 * It is configured by a single parameter given to the constructor.
 * 
 * Text, tooltip and other things are configures in "menu.xml".
 */
@SuppressWarnings("serial")
public final class ProofCenterPropertyAction extends BarAction implements InitialisingAction, PropertyChangeListener {

    private ProofCenter proofCenter;
    private String property;
    
    public ProofCenterPropertyAction(String property) {
        this.property = property;
    }
    
    public void initialised() {
        boolean selected = Settings.getInstance().getBoolean(property, false);
        
        proofCenter = getProofCenter();
        proofCenter.addPropertyChangeListener(property, this);
        proofCenter.firePropertyChange(property, selected);
    }

    public void actionPerformed(ActionEvent e) {
        boolean selectionState = isSelected();
        Log.log("State when choosing the menu:" + selectionState);
        proofCenter.firePropertyChange(property, selectionState);
    }

    @Override 
    public void propertyChange(PropertyChangeEvent evt) {
        assert evt.getPropertyName().equals(property);
        Log.enter(evt.getPropertyName(), evt.getNewValue());
        setSelected((Boolean)evt.getNewValue());
    }
}


