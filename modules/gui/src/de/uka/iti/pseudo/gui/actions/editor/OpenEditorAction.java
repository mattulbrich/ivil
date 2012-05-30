/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions.editor;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class OpenEditorAction extends BarAction {
    
    public OpenEditorAction() {
        putValue(NAME, "Edit problem file ...");
        putValue(ACTION_COMMAND_KEY, "open");
        putValue(SHORT_DESCRIPTION, "Open a problem file in a new editor window");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
        putValue(MNEMONIC_KEY, KeyEvent.VK_E);
    }
    
    public void actionPerformed(ActionEvent e) {
        
        JFileChooser fileChooser = Main.makeFileChooser(Main.PROBLEM_FILE);
        int result = fileChooser.showOpenDialog(getParentFrame());
        if(result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                Main.openEditor(selectedFile);
            } catch (IOException e1) {
                ExceptionDialog.showExceptionDialog(getParentFrame(), e1);
            }
        }
    }

}
