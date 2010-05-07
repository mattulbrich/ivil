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
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

/**
 * This is the action to load a problem file.
 * 
 * It is embedded into the menu.
 */
@SuppressWarnings("serial") 
public class LoadEditedProblemAction extends BarAction {

    private JFileChooser fileChooser;

    public LoadEditedProblemAction() {
        super("Load problem ...", GUIUtil.makeIcon(LoadEditedProblemAction.class.getResource("img/link_go.png")));
        putValue(ACTION_COMMAND_KEY, "loadEditedProb");
        putValue(SHORT_DESCRIPTION, "open the currently edited problem in a new prover window");
    }
    
    public void actionPerformed(ActionEvent e) {
        
        PFileEditor editor = getEditor();
        File file = editor.getFile();
        
        if(file == null) {
            JOptionPane.showMessageDialog(getParentFrame(), "The content of this editor has not yet been saved to a file. Save it.");
            return;
        }
        
        if(editor.hasUnsafedChanges()) {
            int res = JOptionPane
                    .showConfirmDialog(
                            getParentFrame(),
                            "There are unsafed changes in this editor window. Do you still want to launch the prover (on the old version)?",
                            "Unsafed", JOptionPane.YES_NO_OPTION);
            if(res == JOptionPane.NO_OPTION)
                return;
        }

        try {
            ProofCenter pc = Main.openProver(file);
        } catch(Exception ex) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
        } 
    }

}
