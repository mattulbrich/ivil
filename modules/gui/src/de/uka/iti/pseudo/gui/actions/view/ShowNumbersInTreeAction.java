/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions.view;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.util.Log;

/**
 * A simple toggle action used to trigger displaying numbers in the proof
 * component.
 * 
 * @author mattias ulbrich
 */
@SuppressWarnings("serial") 
public class ShowNumbersInTreeAction extends BarAction 
    implements PropertyChangeListener, InitialisingAction {
    
    public ShowNumbersInTreeAction() {
        super("Show node numbers in tree");
    }
    
    public void actionPerformed(ActionEvent e) {
        ProofCenter pc = getProofCenter();
        Log.log(Log.TRACE, "show numbers activated, isSelected:" + isSelected());
        pc.firePropertyChange(ProofCenter.TREE_SHOW_NUMBERS, isSelected());
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        Log.enter(evt);
        setSelected((Boolean) evt.getNewValue());
    }
    
    public void initialised() {
        ProofCenter pc = getProofCenter();
        pc.addPropertyChangeListener(ProofCenter.TREE_SHOW_NUMBERS, this);
        setSelected((Boolean) pc.getProperty(ProofCenter.TREE_SHOW_NUMBERS));
    }

}
