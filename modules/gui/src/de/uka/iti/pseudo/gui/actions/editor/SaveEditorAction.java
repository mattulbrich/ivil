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
package de.uka.iti.pseudo.gui.actions.editor;

import java.awt.event.ActionEvent;
import java.io.File;

import de.uka.iti.pseudo.gui.editor.PFileEditor;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class SaveEditorAction extends SaveAsEditorAction {
    
    public SaveEditorAction() {
        putValue(ACTION_COMMAND_KEY, "save");
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
