package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JOptionPane;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.StateConstants;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class CloseAction extends AbstractStateListeningAction implements WindowListener {

    public CloseAction() {
        super("Close", BarManager.makeIcon(CloseAction.class.getResource("img/bullet_orange.png")));
        putValue(ACTION_COMMAND_KEY, "close");
        putValue(SHORT_DESCRIPTION, "closes the current proof window");
    }
    
    public void stateChanged(StateChangeEvent e) {
        if(e.getState().equals(StateConstants.IN_PROOF)) {
            // switch off if within proof action
            setEnabled(!e.isActive());
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        tryClose();
    }

    public void windowClosing(WindowEvent e) {
        tryClose();
    }

    private void tryClose() {
        boolean changed = getProofCenter().getProof().hasUnsafedChanges();
        if(changed) {
            int result = JOptionPane.showConfirmDialog(getProofCenter().getMainWindow(),
                    "There are changes in the current proof. Close anyway?",
                    "Exit", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            
            if(result != JOptionPane.YES_OPTION)
                return;
        }
        
        Main.closeProofCenter(getProofCenter());
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

}
