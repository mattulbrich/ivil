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
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Log;

/**
 * Prunes the children of the current proof node.
 */
public class PruneAction extends BarAction implements InitialisingAction, PropertyChangeListener {
    
    private static final long serialVersionUID = 2727224331227052729L;

    public PruneAction() {
        super("Prune", GUIUtil.makeIcon(PruneAction.class.getResource("img/cut.png")));
        putValue(SHORT_DESCRIPTION, "Cut the current proof at the selected node");
    }
    
    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
    }
    
    public void actionPerformed(ActionEvent e) {
        Log.enter(e);
        ProofNode proofNode = getProofCenter().getCurrentProofNode();
        
        try {
            getProofCenter().prune(proofNode);
        } catch (ProofException e1) {
            e1.printStackTrace();
        }
        Log.leave();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean)evt.getNewValue());
    }

}
