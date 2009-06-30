package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.uka.iti.pseudo.gui.MainWindow;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class PruneAction extends BarAction implements PropertyChangeListener {
    
    public PruneAction() {
        super("Prune", BarManager.makeIcon(PruneAction.class.getResource("img/cut.png")));
        putValue(SHORT_DESCRIPTION, "Cut the current proof at the selected node");
    }
    
    public void initialised() {
        getProofCenter().getMainWindow().addPropertyChangeListener(MainWindow.IN_PROOF, this);
    }
    
    public void actionPerformed(ActionEvent e) {
        System.err.println(e);
        ProofNode proofNode = getProofCenter().getCurrentProofNode();
        Proof proof = getProofCenter().getProof();
        
        proof.prune(proofNode);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled((Boolean)evt.getOldValue());
    }

}
