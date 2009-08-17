package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.io.IOException;

import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;

public class SMTActivationAction extends BarAction implements InitialisingAction {

    private SMTBackgroundAction peer;
    
    public SMTActivationAction() {
        setSelected(false);
    }
    
    @Override 
    public void initialised() {
        try {
            peer = (SMTBackgroundAction) getProofCenter().getMainWindow().
                    getBarManager().makeAction(SMTBackgroundAction.class.getName());
        } catch (IOException ex) {
            throw new Error(ex);
        }
    }

    public void actionPerformed(ActionEvent e) {
       peer.setBackgroundActive(isSelected());
    }
}
