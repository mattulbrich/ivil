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
import java.io.File;

import javax.swing.JOptionPane;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.util.ExceptionDialog;

/**
 * This is the action to load a problem file.
 *
 * It is embedded into the menu.
 */
@SuppressWarnings("serial")
public class LoadEditedProblemAction extends BarAction {

    public LoadEditedProblemAction() {
        super("Load problem ...");
        putValue(ACTION_COMMAND_KEY, "loadEditedProb");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        PFileEditor editor = getEditor();
        File file = editor.getFile();

        if(file == null) {
            JOptionPane.showMessageDialog(getParentFrame(), "The content of this editor has not yet been saved to a file. Save it.");
            return;
        }

        if(editor.hasUnsavedChanges()) {
            int res = JOptionPane
                    .showConfirmDialog(
                            getParentFrame(),
                            "There are unsafed changes in this editor window. Do you still want to launch the prover (on the old version)?",
                            "Unsafed", JOptionPane.YES_NO_OPTION);
            if(res == JOptionPane.NO_OPTION) {
                return;
            }
        }

        try {
            Main.openProver(file);
        } catch(Exception ex) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
        }
    }
}
