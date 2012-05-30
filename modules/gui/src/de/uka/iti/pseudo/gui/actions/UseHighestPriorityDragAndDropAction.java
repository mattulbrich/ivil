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
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.gui.sequent.TermComponent;
import de.uka.iti.pseudo.util.Log;

/**
 * A simple toggle action used to trigger whether the rule with the highest
 * priority will be applied on term drops or a popup menu is shown.
 * 
 * @author timm.felden@felden.com
 */
@SuppressWarnings("serial") 
public class UseHighestPriorityDragAndDropAction extends BarAction 
    implements PropertyChangeListener, InitialisingAction {
    
    private static final boolean DEFAULT_STATE = false;

    public UseHighestPriorityDragAndDropAction() {
        super("Highest priority D&D mode");
        putValue(SHORT_DESCRIPTION, "Automatically use the rule with the highest priority for Drag & Drop");
    }
    
    public void actionPerformed(ActionEvent e) {
        ProofCenter pc = getProofCenter();
        Log.log(Log.TRACE, "high priority drag and drop, isSelected:" + isSelected());
        pc.firePropertyChange(TermComponent.HIGHEST_PRIORITY_DRAG_AND_DROP, isSelected());
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        Log.enter(evt);
        setSelected((Boolean) evt.getNewValue());
    }
    
    public void initialised() {
        ProofCenter pc = getProofCenter();
        pc.addPropertyChangeListener(TermComponent.HIGHEST_PRIORITY_DRAG_AND_DROP, this);
        if (null == pc.getProperty(TermComponent.HIGHEST_PRIORITY_DRAG_AND_DROP))
            pc.firePropertyChange(TermComponent.HIGHEST_PRIORITY_DRAG_AND_DROP, DEFAULT_STATE);
        else
        setSelected((Boolean) pc.getProperty(TermComponent.HIGHEST_PRIORITY_DRAG_AND_DROP));
    }

}
