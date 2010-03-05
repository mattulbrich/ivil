/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.GUIUtil;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class PruneAction extends BarAction implements PropertyChangeListener {
    
    public PruneAction() {
        super("Prune", GUIUtil.makeIcon(PruneAction.class.getResource("img/cut.png")));
        putValue(SHORT_DESCRIPTION, "Cut the current proof at the selected node");
    }
    
    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
    }
    
    public void actionPerformed(ActionEvent e) {
        System.err.println(e);
        ProofNode proofNode = getProofCenter().getCurrentProofNode();
        Proof proof = getProofCenter().getProof();
        
        proof.prune(proofNode);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean)evt.getNewValue());
    }

}
