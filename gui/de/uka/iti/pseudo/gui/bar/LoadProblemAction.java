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
import de.uka.iti.pseudo.util.ExceptionDialog;

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
        
        int result = Main.makeFileChooser(Main.PROBLEM_FILE).showOpenDialog(getParentFrame());
        if(result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                ProofCenter pc = Main.openProver(selectedFile);
            } catch(IOException ex) {
                ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
            } catch(Exception ex) {
                ex.printStackTrace();
                int res = JOptionPane.showConfirmDialog(getParentFrame(), "'" + selectedFile + 
                        "' cannot be loaded. Do you want to open an editor to analyse?",
                        "Error in File",
                        JOptionPane.YES_NO_OPTION);
                
                if(res == JOptionPane.YES_OPTION) {
                    try {
                        Main.openEditor(selectedFile);
                    } catch (IOException e1) {
                        ExceptionDialog.showExceptionDialog(getParentFrame(), e1);
                    }
                    
                }
                    
            }            
        }
    }

}
