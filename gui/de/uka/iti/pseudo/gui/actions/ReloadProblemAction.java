package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

/**
 * This allows for reloading of the problem. If no problem was loaded yet, the
 * most recent problem will be loaded instead.
 * @author felden@ira.uka.de
 *
 */

public class ReloadProblemAction extends BarAction implements
		PropertyChangeListener {
	
	private static final long serialVersionUID = 8652614246864976171L;

	public ReloadProblemAction() {
        super("Reload problem ...", GUIUtil.makeIcon(LoadProblemAction.class.getResource("img/page_white_green_text.png")));
        putValue(ACTION_COMMAND_KEY, "reloadProb");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
        putValue(SHORT_DESCRIPTION, "reload the last problem file into a new window");
    }
    
    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean)evt.getNewValue());
    }
    
    public void actionPerformed(ActionEvent e) {
    	//get recent files
    	Preferences prefs = Preferences.userNodeForPackage( Main.class );
        String recent[] = prefs.get("recent files", "").split("\n");

        //open if there are recent files
        if(!recent[0].equals("")) {
            File selectedFile = new File(recent[0]);
            try {
                Main.openProver(selectedFile);
            } catch(IOException ex) {
                ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
            } catch(Exception ex) {
                ex.printStackTrace();
                int res = JOptionPane.showConfirmDialog(getParentFrame(), "'" + selectedFile + 
                        "' cannot be loaded. Do you want to open an editor to analyse?",
                        "Error in File",
                        JOptionPane.YES_NO_OPTION);
                
                if(res == JOptionPane.YES_OPTION) {
                    try {
                        Main.openEditor(selectedFile);
                    } catch (IOException e1) {
                        ExceptionDialog.showExceptionDialog(getParentFrame(), e1);
                    }
                    
                }
                    
            }            
        }
    }
}
