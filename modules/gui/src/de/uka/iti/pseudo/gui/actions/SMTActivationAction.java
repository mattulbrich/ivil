/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
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
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * Simple Action to enable/disable background smt solver.
 * Used in Settings menu
 */
@SuppressWarnings("serial") 
public class SMTActivationAction extends BarAction implements InitialisingAction, PropertyChangeListener {

    private ProofCenter proofCenter;

    public SMTActivationAction() {
        super("Background SMT");
        putValue(SHORT_DESCRIPTION, "Activate background SMT solver");
    }
    
    public void initialised() {
        boolean selected = Settings.getInstance().getBoolean("pseudo.smt.background", false);
        
        proofCenter = getProofCenter();
        proofCenter.addPropertyChangeListener(SMTBackgroundAction.SMT_BACKGROUND_PROPERTY, this);
        proofCenter.firePropertyChange(SMTBackgroundAction.SMT_BACKGROUND_PROPERTY, selected);
    }

    public void actionPerformed(ActionEvent e) {
        boolean selectionState = isSelected();
        proofCenter.firePropertyChange(SMTBackgroundAction.SMT_BACKGROUND_PROPERTY, selectionState);
    }

    @Override 
    public void propertyChange(PropertyChangeEvent evt) {
        assert evt.getPropertyName().equals(SMTBackgroundAction.SMT_BACKGROUND_PROPERTY);
        setSelected((Boolean)evt.getNewValue());
    }
}
