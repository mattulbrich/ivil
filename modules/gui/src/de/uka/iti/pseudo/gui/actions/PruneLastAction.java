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
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.KeyStroke;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.Log;

/**
 * Prunes the newest proof node, i.e. the proof node with the highest number.
 * 
 * @note: this action might actually remove more then one node in cases, where
 *        the last rule application created a branch on the proof tree. This is
 *        however the expected behavior
 */
public class PruneLastAction extends BarAction implements InitialisingAction, PropertyChangeListener {
    
    private static final long serialVersionUID = 2727224331227052729L;

    public PruneLastAction() {
        super("Prune newest node");
        putValue(
                SHORT_DESCRIPTION,
                "Prunes the newest proof node, which is useful if a rule application turns out to be a step into the wrong direction. There is no unprune!");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
    }
    
    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
    }
    
    /**
     * @param parent
     * @return the node with the highest node number which is below parent
     *         including parent
     */
    private ProofNode getNewestNode(final ProofNode parent) {
        ProofNode rval = parent;
        if (null != parent.getChildren()) {
            for (ProofNode n : parent.getChildren()) {
                final ProofNode t = getNewestNode(n);
                rval = (t.getNumber() > rval.getNumber() ? t : rval);
            }
        }

        return rval;
    }

    public void actionPerformed(ActionEvent e) {
        Log.enter(e);

        // in most situations, the current proof node will be the oldest
        ProofNode proofNode = getNewestNode(getProofCenter().getProof().getRoot()).getParent();

        // if the node has no parent, it is the root node, which can not be
        // removed
        if (null == proofNode)
            return;
        

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
