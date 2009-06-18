package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.MainWindow;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class CloseAction extends BarAction 
    implements WindowListener, PropertyChangeListener {

    public CloseAction() {
        super("Close", BarManager.makeIcon(CloseAction.class.getResource("img/bullet_orange.png")));
        putValue(ACTION_COMMAND_KEY, "close");
        putValue(SHORT_DESCRIPTION, "closes the current proof window");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
        putValue(MNEMONIC_KEY, KeyEvent.VK_C);
    }
    
    public void initialised() {
        getProofCenter().getMainWindow().addPropertyChangeListener(MainWindow.IN_PROOF, this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled((Boolean)evt.getOldValue());
    }

    public void actionPerformed(ActionEvent e) {
        tryClose();
    }

    public void windowClosing(WindowEvent e) {
        if(isEnabled())
            tryClose();
    }

    private void tryClose() {
        boolean changed = getProofCenter().getProof().hasUnsafedChanges();
        if(changed) {
            int result = JOptionPane.showConfirmDialog(getParentFrame(),
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
