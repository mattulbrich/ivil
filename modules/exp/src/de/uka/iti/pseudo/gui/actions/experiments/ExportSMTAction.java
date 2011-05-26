package de.uka.iti.pseudo.gui.actions.experiments;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;

import javax.swing.JOptionPane;

import de.uka.iti.pseudo.auto.SMTLibTranslator;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.util.ExceptionDialog;

@SuppressWarnings("serial")
public class ExportSMTAction extends BarAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            ProofCenter proofCenter = getProofCenter();
            ProofNode proofNode = proofCenter.getCurrentProofNode();
            Sequent seq = proofNode.getSequent();
            SMTLibTranslator trans = new SMTLibTranslator(proofCenter.getEnvironment());
            File tmp = File.createTempFile("ivilExport" + proofNode.getNumber() + ".", ".smt");
            FileWriter wr = new FileWriter(tmp);
            wr.write("; Sequent: " + seq.toString() + "\n");
            trans.export(seq, wr);
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

}
