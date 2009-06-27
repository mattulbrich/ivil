package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.MainWindow;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.term.Term;

/**
 * This is the action to load a problem file.
 * 
 * It is embedded into the menu.
 */
@SuppressWarnings("serial") 
public class LoadProblemAction extends BarAction implements PropertyChangeListener {

    private JFileChooser fileChooser;

    public LoadProblemAction() {
        super("Load problem ...", BarManager.makeIcon(LoadProblemAction.class.getResource("img/page_white_text.png")));
        putValue(ACTION_COMMAND_KEY, "loadProb");
        putValue(SHORT_DESCRIPTION, "open a problem file into a new window");
    }
    
    public void initialised() {
        getProofCenter().getMainWindow().addPropertyChangeListener(MainWindow.IN_PROOF, this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled((Boolean)evt.getOldValue());
    }
    
    public void actionPerformed(ActionEvent e) {
        
        if(fileChooser == null)
            fileChooser = new JFileChooser(".");
        
        int result = fileChooser.showOpenDialog(getParentFrame());
        if(result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                Parser fp = new Parser();
                EnvironmentMaker em = new EnvironmentMaker(fp, selectedFile);
                Environment env = em.getEnvironment();
                Term problemTerm = em.getProblemTerm();
                if(problemTerm == null)
                    throw new EnvironmentException("Cannot load an environment without problem");
                Proof proof = new Proof(problemTerm);
                ProofCenter pc = new ProofCenter(proof, env);
                Main.showProofCenter(pc);
            } catch(IOException ex) {
                // TODO gescheiter Fehlerdialog
                ex.printStackTrace();
            } catch(Exception ex) {
                ex.printStackTrace();
                int res = JOptionPane.showConfirmDialog(getParentFrame(), "'" + selectedFile + 
                        "' cannot be loaded. Do you want to open an editor to analyse?",
                        "Error in File",
                        JOptionPane.YES_NO_OPTION);
                
                if(res == JOptionPane.YES_OPTION) {
                    try {
                        PFileEditor editor = new PFileEditor(selectedFile);
                        editor.setSize(600, 800);
                        Main.showFileEditor(editor);
                    } catch (IOException e1) {
                        // TODO gescheiter Fehler!
                        e1.printStackTrace();
                    }
                    
                }
                    
            }            
        }
    }

}
