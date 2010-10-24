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

import de.uka.iti.pseudo.gui.ProofComponent;
import de.uka.iti.pseudo.gui.actions.BarAction;

public class ExpandProofTreeBelowAction extends BarAction {

    private static final long serialVersionUID = 3169340947658903144L;

    public ExpandProofTreeBelowAction() {
        super("Expand the proof tree below");
    }

    @Override public void actionPerformed(ActionEvent e) {
        ProofComponent proofComponent = getProofCenter().getMainWindow().getProofComponent();
        int[] selections = proofComponent.getSelectionRows();
        if(selections != null && selections.length > 0) {
            proofComponent.expandRow(selections[0]);
        }
    }
    
}
