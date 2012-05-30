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
import java.io.IOException;

import javax.swing.KeyStroke;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.util.ExceptionDialog;


/**
 * GUI Action open a new editor with an empty document.
 */

@SuppressWarnings("serial") 
public class NewEditorAction extends BarAction {

    public NewEditorAction() {
        putValue(NAME, "New");
        putValue(ACTION_COMMAND_KEY, "new");
        putValue(SHORT_DESCRIPTION, "Create and edit a new problem file");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        putValue(MNEMONIC_KEY, KeyEvent.VK_N);
    }

    public void actionPerformed(ActionEvent e) {
        try {
            Main.openEditor(null);
        } catch (IOException e1) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), e1);
        }
    }
}