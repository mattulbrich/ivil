package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.StateConstants;
import de.uka.iti.pseudo.gui.bar.StateListener.StateListeningAction;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class ExitAction extends AbstractAction implements StateListeningAction {

    public ExitAction() {
        super("Exit", BarManager.makeIcon(ExitAction.class.getResource("img/exit.png")));
        putValue(ACTION_COMMAND_KEY, "exit");
    }
    
    public void stateChanged(StateChangeEvent e) {
        if(e.getState().equals(StateConstants.IN_PROOF)) {
            // switch off if within proof action
            setEnabled(!e.isActive());
        }
    }
    
    private ProofCenter getProofCenter() {
        return (ProofCenter) getValue(BarManager.CENTER);
    }

    public void actionPerformed(ActionEvent e) {
        System.err.println(e);
        
        boolean changed = getProofCenter().getProof().hasUnsafedChanges();
        if(changed) {
            int result = JOptionPane.showConfirmDialog(getProofCenter().getMainWindow(),
                    "There are changes in the current proof. Exit anyway?",
                    "Exit", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            
            if(result != JOptionPane.YES_OPTION)
                return;
        }
        
        System.exit(0);
    }

}
