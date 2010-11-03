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

import javax.swing.JOptionPane;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.Log;

/**
 * Prunes the children of the current proof node.
 */
public class PruneAction extends BarAction implements InitialisingAction, PropertyChangeListener {
    
    private static final long serialVersionUID = 2727224331227052729L;

    public PruneAction() {
        super("Prune");
        putValue(SHORT_DESCRIPTION, "Cut the current proof at the selected node");
    }
    
    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
    }
    
    public void actionPerformed(ActionEvent e) {
        // pruning in closed proofs is mostly undesired, so better ask if that's
        // really intended
        if (!getProofCenter().getProof().hasOpenGoals()) {
            int answer = JOptionPane.showOptionDialog(getParentFrame(),
                    "The proof is already closed, do you really want to prune?", "Really?", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE, null, null, JOptionPane.NO_OPTION);
            
            if (JOptionPane.NO_OPTION == answer)
                return;
        }

        Log.enter(e);
        ProofNode proofNode = getProofCenter().getCurrentProofNode();
        
        try {
            getProofCenter().prune(proofNode);
        } catch (ProofException ex) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
        }
        
        // no need to select new goal.
        getProofCenter().fireProoftreeChangedNotification(false);
        getProofCenter().fireSelectedProofNode(proofNode);
        Log.leave();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean)evt.getNewValue());
    }

}
