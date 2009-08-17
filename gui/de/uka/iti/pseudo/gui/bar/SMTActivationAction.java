package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.io.IOException;

import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;

/**
 * Simple Action to enable/disable background smt solver.
 * Used in Settings menu
 */
@SuppressWarnings("serial") 
public class SMTActivationAction extends BarAction implements InitialisingAction {

    private SMTBackgroundAction peer;
    
    public SMTActivationAction() {
        super("Background SMT");
        setSelected(false);
        putValue(SHORT_DESCRIPTION, "Activate background SMT solver");
    }
    
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
