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
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.KeyStroke;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class OpenEditorAction extends BarAction {
    
    public OpenEditorAction() {
        putValue(NAME, "Edit problem file ...");
        putValue(SMALL_ICON, GUIUtil.makeIcon(OpenEditorAction.class.getResource("img/page_white_text_edit.png")));
        putValue(ACTION_COMMAND_KEY, "open");
        putValue(SHORT_DESCRIPTION, "Open a problem file in a new editor window");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
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
