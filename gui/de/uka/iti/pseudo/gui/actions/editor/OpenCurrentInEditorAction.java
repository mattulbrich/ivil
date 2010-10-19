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
import java.io.IOException;
import java.net.URL;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class OpenCurrentInEditorAction extends BarAction {
    
    public OpenCurrentInEditorAction() {
        putValue(NAME, "Edit current problem");
        putValue(SMALL_ICON, GUIUtil.makeIcon(OpenCurrentInEditorAction.class.getResource("../img/link_edit.png")));
        putValue(SHORT_DESCRIPTION, "Open the currently active problem file in a new editor window");
    }
    
    public void actionPerformed(ActionEvent e) {
        try {        
            URL url = new URL(getProofCenter().getEnvironment().getResourceName());

            if(!"file".equals(url.getProtocol())) {
                ExceptionDialog.showExceptionDialog(getParentFrame(), 
                        "Only local files can be opened in an editor frame." +
                        "Copy the file to your file system and relaunch the " +
                "system.");
                return;
            }

            File file = new File(url.getPath());

            Main.openEditor(file);
        } catch (IOException e1) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), e1);
        }
    }
}
