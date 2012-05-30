/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions.experiments;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JOptionPane;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.justify.EnvironmentExporter;
import de.uka.iti.pseudo.util.ExceptionDialog;

@SuppressWarnings("serial")
public class ExportEnvironmentAction extends BarAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            ProofCenter proofCenter = getProofCenter();
            Environment env = proofCenter.getEnvironment();
            File tmp = File.createTempFile("ivilEnvironment", ".p");
            
            EnvironmentExporter exporter = new EnvironmentExporter(tmp);
            exporter.exportComplete(env);
            exporter.close();
            
            Object[] options = {"Open in editor", "OK"};
            int n = JOptionPane.showOptionDialog(getParentFrame(),
                            "Current environment saved to " + tmp,
                            "Environment Exported",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            options,
                            options[1]);
            
            if (n == JOptionPane.YES_OPTION) {
                PFileEditor editor = Main.openEditor(tmp);
            } 
            
            
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
        }
    }

}
