/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
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

import javax.swing.KeyStroke;

import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.util.GUIUtil;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class SaveEditorAction extends SaveAsEditorAction {
    
    public SaveEditorAction() {
        putValue(NAME, "Save");
        putValue(SMALL_ICON, GUIUtil.makeIcon(SaveEditorAction.class.getResource("img/disk.png")));
        putValue(ACTION_COMMAND_KEY, "save");
        putValue(SHORT_DESCRIPTION, "save the currently edited file");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
    }
    
    public void actionPerformed(ActionEvent e) {
        
        PFileEditor editor = (PFileEditor) getValue(PARENT_FRAME);
        File selectedFile = editor.getFile();
        
        if(selectedFile != null) {
            saveUnder(editor, selectedFile);
        } else {
            super.actionPerformed(e);
        }
    }

}
