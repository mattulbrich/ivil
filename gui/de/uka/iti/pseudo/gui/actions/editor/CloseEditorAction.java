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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.util.GUIUtil;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class CloseEditorAction extends BarAction 
    implements WindowListener {

    public CloseEditorAction() {
        super("Close", GUIUtil.makeIcon(CloseEditorAction.class.getResource("../img/bullet_orange.png")));
        putValue(ACTION_COMMAND_KEY, "close");
        putValue(SHORT_DESCRIPTION, "closes the editor window");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
        putValue(MNEMONIC_KEY, KeyEvent.VK_C);
    }
    
    public void actionPerformed(ActionEvent e) {
        tryClose();
    }

    public void windowClosing(WindowEvent e) {
        if(isEnabled())
            tryClose();
    }

    private void tryClose() {
        PFileEditor editor = getEditor();
        boolean changed = editor.hasUnsafedChanges();
        if(changed) {
            int result = JOptionPane.showConfirmDialog(getParentFrame(),
                    "There are changes in the current edit window. Close anyway?",
                    "Close", JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            
            if(result != JOptionPane.YES_OPTION)
                return;
        }
        
        Main.closeFileEditor(editor);
    }

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
