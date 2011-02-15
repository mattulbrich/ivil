package de.uka.iti.pseudo.gui.actions.experiments;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;

import javax.swing.JOptionPane;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
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
            FileWriter wr = new FileWriter(tmp);
            
            EnvironmentExporter exporter = new EnvironmentExporter(tmp);
            exporter.exportComplete(env);
            exporter.close();
            
            JOptionPane.showMessageDialog(getParentFrame(), "Current environment saved to " + tmp);
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
        }
    }

}
