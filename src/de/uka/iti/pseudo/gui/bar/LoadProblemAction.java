package de.uka.iti.pseudo.gui.bar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.StateConstants;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.parser.Parser;
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
    
    public void actionPerformed(ActionEvent e) {
        
        if(fileChooser == null)
            fileChooser = new JFileChooser(".");
        
        Component root = SwingUtilities.getRoot((Component) e.getSource());
        
        int result = fileChooser.showOpenDialog(root);
        if(result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                Parser fp = new Parser();
                EnvironmentMaker em = new EnvironmentMaker(fp, selectedFile);
                Environment env = em.getEnvironment();
                Proof proof = new Proof(em.getProblemTerm());
                ProofCenter pc = new ProofCenter(proof, env);
                Main.showProofCenter(pc);
            } catch(IOException ex) {
                // TODO gescheiter Fehlerdialog
            } catch(Exception ex) {
                int res = JOptionPane.showConfirmDialog(null, "'" + selectedFile + 
                        "' cannot be loaded. Do you want to open an editor to analyse?",
                        "Error in File",
                        JOptionPane.YES_NO_OPTION);
                
                if(res == JOptionPane.YES_OPTION) {
                    try {
                        JFrame f = new JFrame("Pseudo - Edit " + selectedFile);
                        PFileEditor editor = new PFileEditor(selectedFile);
                        f.getContentPane().add(editor, BorderLayout.CENTER);
                        f.setSize(300,600);
                        f.setVisible(true);
                    } catch (IOException e1) {
                        // TODO gescheiter Fehler!
                        e1.printStackTrace();
                    }
                    
                }
                    
            }            
        }
    }

    public void stateChanged(StateChangeEvent e) {
        if(e.getState().equals(StateConstants.IN_PROOF)) {
            // switch off if within proof action
            setEnabled(!e.isActive());
        }
    }

}
