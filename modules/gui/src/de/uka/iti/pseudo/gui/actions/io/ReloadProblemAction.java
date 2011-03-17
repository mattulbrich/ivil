package de.uka.iti.pseudo.gui.actions.io;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.prefs.Preferences;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.util.ExceptionDialog;

/**
 * This allows for reloading of the problem. If no problem was loaded yet, the
 * last attempt to load a file will be repeated.
 * 
 * @author timm.felden@felden.com
 */

public class ReloadProblemAction extends BarAction implements
		PropertyChangeListener {
	
	private static final long serialVersionUID = 8652614246864976171L;

//	public ReloadProblemAction() {
//        super("Reload problem ...", GUIUtil.makeIcon(LoadProblemAction.class
//                .getResource("../img/page_white_green_text.png")));
//        putValue(ACTION_COMMAND_KEY, "reloadProb");
//        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
//        putValue(MNEMONIC_KEY, KeyEvent.VK_R);
//        putValue(SHORT_DESCRIPTION, "reload the last problem file into a new window");
//    }
    
    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean)evt.getNewValue());
    }

    public void actionPerformed(ActionEvent e) {
        // try to get the path of the currently opened problem; if there is one
        // open it
        try {
            URL url = new URL(getProofCenter().getEnvironment().getResourceName());
            Main.openProverFromURL(url);
        } catch (Exception ex) {
            // this exception indicates, that there is no problem, which can be
            // reloaded, so let us just continue
        }

        // get url of the last problem
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        String last = prefs.get("last problem", "");

        // open if there are recent files
        if (!last.equals("")) {
            try {
                URL url = new URL(last);
                Main.openProverFromURL(url);
            } catch (Exception ex) {
                ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
            }

        }
    }
}
