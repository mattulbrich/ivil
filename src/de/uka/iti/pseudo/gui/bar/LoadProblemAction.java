package de.uka.iti.pseudo.gui.bar;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.StateConstants;
import de.uka.iti.pseudo.parser.file.FileParser;
import de.uka.iti.pseudo.proof.Proof;

/**
 * This is the action to load a problem file.
 * 
 * It is embedded into the menu.
 */
@SuppressWarnings("serial") 
public class LoadProblemAction extends AbstractStateListeningAction {

    private JFileChooser fileChooser;

    public LoadProblemAction() {
        super("Load problem ...", BarManager.makeIcon(LoadProblemAction.class.getResource("img/page_white_text.png")));
        putValue(ACTION_COMMAND_KEY, "loadProb");
        putValue(SHORT_DESCRIPTION, "open a problem file into a new window");
    }
    
    @Override public void actionPerformed(ActionEvent e) {
        
        if(fileChooser == null)
            fileChooser = new JFileChooser(".");
        
        Component root = SwingUtilities.getRoot((Component) e.getSource());
        
        int result = fileChooser.showOpenDialog(root);
        if(result == JFileChooser.APPROVE_OPTION) {
            try {
                FileParser fp = new FileParser();
                EnvironmentMaker em = new EnvironmentMaker(fp, fileChooser.getSelectedFile());
                Environment env = em.getEnvironment();
                Proof proof = new Proof(em.getProblemTerm());
                ProofCenter pc = new ProofCenter(proof, env);
                Main.showProofCenter(pc);
            } catch (Exception ex) {
                // TODO gescheiter Fehlerdialog
                ex.printStackTrace();
            }
        }
    }

    @Override public void stateChanged(StateChangeEvent e) {
        if(e.getState().equals(StateConstants.IN_PROOF)) {
            // switch off if within proof action
            setEnabled(!e.isActive());
        }
    }

}
