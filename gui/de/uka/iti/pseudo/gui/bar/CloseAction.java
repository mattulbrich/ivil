/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
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
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.editor.PFileEditor;

// TODO: Auto-generated Javadoc
// TODO Documentation needed
/**
 * This GUI action is for closing a window.
 * 
 * Since there are both prover and editor windows, case
 * distinction have to be performed at couple of places.
 */
@SuppressWarnings("serial") 
public class CloseAction extends BarAction 
    implements WindowListener, PropertyChangeListener {

    public CloseAction() {
        super("Close", BarManager.makeIcon(CloseAction.class.getResource("img/bullet_orange.png")));
        putValue(ACTION_COMMAND_KEY, "close");
        putValue(SHORT_DESCRIPTION, "closes the current window");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
        putValue(MNEMONIC_KEY, KeyEvent.VK_C);
    }
    
    public void initialised() {
        ProofCenter proofCenter = getProofCenter();
        if(proofCenter != null)
            proofCenter.addPropertyChangeListener(ProofCenter.PROPERTY_ONGOING_PROOF, this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean)evt.getNewValue());
    }

    public void actionPerformed(ActionEvent e) {
        tryClose();
    }

    public void windowClosing(WindowEvent e) {
        if(isEnabled())
            tryClose();
    }

    private void tryClose() {
        ProofCenter proofCenter = getProofCenter();
        PFileEditor editor = getEditor();
        boolean changed = false;
        if(proofCenter != null) {
            changed = proofCenter.getProof().hasUnsafedChanges();
        } else {
            changed = editor.hasUnsafedChanges();
        }
        
        if(changed) {
            int result = JOptionPane.showConfirmDialog(getParentFrame(),
                    "There are changes in the current window. Close anyway?",
                    "Exit", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            
            if(result != JOptionPane.YES_OPTION)
                return;
        }
        
        if(proofCenter != null)
            Main.closeProofCenter(proofCenter);
        else
            Main.closeFileEditor(editor);
    }

    // WindowListener methods which are to be ignored
    
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
