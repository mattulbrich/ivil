package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.StateConstants;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class ExitAction extends AbstractStateListeningAction {

    public ExitAction() {
        super("Exit", BarManager.makeIcon(ExitAction.class.getResource("img/bullet_red.png")));
        putValue(ACTION_COMMAND_KEY, "exit");
        putValue(SHORT_DESCRIPTION, "closes all open windows of the program");
    }
    
    public void stateChanged(StateChangeEvent e) {
        if(e.getState().equals(StateConstants.IN_PROOF)) {
            // switch off if within proof action
            setEnabled(!e.isActive());
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        tryExit();
    }

    private void tryExit() {
        boolean changed = Main.proofCentersHaveChanges();
        if(changed) {
            int result = JOptionPane.showConfirmDialog(getProofCenter().getMainWindow(),
                    "There are changes in the a proof. Exit anyway?",
                    "Exit", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            
            if(result != JOptionPane.YES_OPTION)
                return;
        }
        
        System.exit(0);
    }

}
