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
import java.io.FileWriter;

import javax.swing.JOptionPane;

import de.uka.iti.pseudo.auto.SMTLib2Translator;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.util.ExceptionDialog;

@SuppressWarnings("serial")
public class ExportSMTAction extends BarAction {

    private static final String TMP_DIR =
            System.getProperty("java.io.tmpdir", "");

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            ProofCenter proofCenter = getProofCenter();
            ProofNode proofNode = proofCenter.getCurrentProofNode();
            Sequent seq = proofNode.getSequent();
            SMTLib2Translator trans = new SMTLib2Translator(proofCenter.getEnvironment());
            File tmp = createTempFile(proofNode.getNumber());
            FileWriter wr = new FileWriter(tmp);
            wr.write("; Sequent: " + seq.toString() + "\n");
            trans.export(seq, wr);
            wr.write("\n;-- Launch check\n(check-sat)\n");
            wr.close();

            Object[] options = {"Open in editor", "OK"};
            int n = JOptionPane.showOptionDialog(getParentFrame(),
                            "Current sequent's translation saved to " + tmp,
                            "SMT Exported",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            options,
                            options[1]);
            if (n == JOptionPane.YES_OPTION) {
                PFileEditor editor = Main.openEditor(tmp);
                editor.setProperty("syntaxCheck", false);
                editor.setProperty("syntaxHighlight", false);
            }
        } catch(Exception ex) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
        }
    }

    private File createTempFile(int number) {

        int count = 0;
        File tmpFile = new File(TMP_DIR, "ivilExport" + count + ".smt");
        while(tmpFile.exists()) {
            count ++;
            tmpFile = new File(TMP_DIR, "ivilExport" + count + ".smt");
        }
        return tmpFile;
    }
}
