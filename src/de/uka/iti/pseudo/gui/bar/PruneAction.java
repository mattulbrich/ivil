package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.StateConstants;
import de.uka.iti.pseudo.gui.bar.StateListener.StateListeningAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class PruneAction extends AbstractAction implements StateListeningAction {
    
    public PruneAction() {
        super("Prune", BarManager.makeIcon(PruneAction.class.getResource("img/cut.png")));
        putValue(SHORT_DESCRIPTION, "Cut the current proof at the selected node");
    }
    
    private ProofCenter getProofCenter() {
        return (ProofCenter) getValue(BarManager.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
        System.err.println(e);
        ProofNode proofNode = getProofCenter().getCurrentProofNode();
        Proof proof = getProofCenter().getProof();
        
        proof.prune(proofNode);
    }

    public void stateChanged(StateChangeEvent e) {
        if(e.getState().equals(StateConstants.IN_PROOF)) {
            // switch off if within proof action
            setEnabled(!e.isActive());
        }
    }

}
