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
package de.uka.iti.pseudo.gui.actions.view;

import java.awt.event.ActionEvent;

import javax.swing.Icon;

import de.uka.iti.pseudo.gui.ProofComponent;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.auto.AutoProofAction;
import de.uka.iti.pseudo.util.GUIUtil;

@SuppressWarnings("serial") 
public class ExpandProofTreeBelowAction extends BarAction {
    
    private static Icon plusIcon = 
 GUIUtil.makeIcon(AutoProofAction.class.getResource("../img/bullet_toggle_plus.png"));
    
    public ExpandProofTreeBelowAction() {
        super("Expand the proof tree below", plusIcon);
    }

    @Override public void actionPerformed(ActionEvent e) {
        ProofComponent proofComponent = getProofCenter().getMainWindow().getProofComponent();
        int[] selections = proofComponent.getSelectionRows();
        if(selections != null && selections.length > 0) {
            proofComponent.expandRow(selections[0]);
        }
    }
    
}
