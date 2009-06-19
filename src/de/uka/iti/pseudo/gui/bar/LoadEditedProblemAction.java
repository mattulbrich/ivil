package de.uka.iti.pseudo.gui.bar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.MainWindow;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.proof.Proof;

/**
 * This is the action to load a problem file.
 * 
 * It is embedded into the menu.
 */
@SuppressWarnings("serial") 
public class LoadEditedProblemAction extends BarAction {

    private JFileChooser fileChooser;

    public LoadEditedProblemAction() {
        super("Load problem ...", BarManager.makeIcon(LoadEditedProblemAction.class.getResource("img/link_go.png")));
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
            Parser fp = new Parser();
            EnvironmentMaker em = new EnvironmentMaker(fp, file);
            Environment env = em.getEnvironment();
            Proof proof = new Proof(em.getProblemTerm());
            ProofCenter pc = new ProofCenter(proof, env);
            Main.showProofCenter(pc);
        } catch(Exception ex) {
            // TODO gescheiter Fehlerdialog
            ex.printStackTrace();
        } 
    }

}
