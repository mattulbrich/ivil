/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.util.GUIUtil;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class ExitAction extends BarAction implements PropertyChangeListener {

    public ExitAction() {
        super("Exit", GUIUtil.makeIcon(ExitAction.class.getResource("img/bullet_red.png")));
        putValue(ACTION_COMMAND_KEY, "exit");
        putValue(SHORT_DESCRIPTION, "closes all open windows of the program and exits");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
        putValue(MNEMONIC_KEY, KeyEvent.VK_X);
    }
    
    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean)evt.getNewValue());
    }
    
    public void actionPerformed(ActionEvent e) {
        tryExit();
    }

    private void tryExit() {
        boolean changed = Main.windowsHaveChanges();
        if(changed) {
            int result = JOptionPane.showConfirmDialog(getParentFrame(),
                    "There are changes in the a proof. Exit anyway?",
                    "Exit", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            
            if(result != JOptionPane.YES_OPTION)
                return;
        }
        
        System.exit(0);
    }

}
