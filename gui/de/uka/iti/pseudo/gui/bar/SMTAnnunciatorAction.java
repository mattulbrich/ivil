package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.Icon;

import de.uka.iti.pseudo.gui.MainWindow;
import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;

public class SMTAnnunciatorAction extends BarAction implements InitialisingAction, Observer, PropertyChangeListener {

    private Proof proof;
    private Set<ProofNode> provableNodes = new HashSet<ProofNode>();
    
    private Icon flashImg;
    private Icon offImg;
    
    public SMTAnnunciatorAction() {
        flashImg = BarManager.makeIcon(getClass().getResource("img/flashing_lightbulb.png"));
        offImg = BarManager.makeIcon(getClass().getResource("img/lightbulb_off.png"));
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println("TODO");
    }

    public void addProvable(ProofNode pn) {
        provableNodes.add(pn);
        setEnabled(true);
    }

    public void initialised() {
        getProofCenter().getMainWindow().addPropertyChangeListener(MainWindow.IN_PROOF, this);
        proof = getProofCenter().getProof();
        proof.addObserver(this);
    }

    public void update(Observable o, Object arg) {
        Iterator<ProofNode> it = provableNodes.iterator();
        while(it.hasNext()) {
            if(!proof.getOpenGoals().contains(it.next()))
                it.remove();
        }
        if(provableNodes.isEmpty())
            setEnabled(false);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled((Boolean) evt.getOldValue());
    }
    
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setIcon(enabled ? flashImg : offImg);
    }

}
