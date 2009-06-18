package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.uka.iti.pseudo.gui.MainWindow;
import de.uka.iti.pseudo.gui.StateConstants;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.proof.Proof;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class SaveEditorAction extends AbstractStateListeningAction {
    
    private JFileChooser fileChooser;
    
    public SaveEditorAction() {
        super("Save proof ...", BarManager.makeIcon(SaveEditorAction.class.getResource("img/disk.png")));
        putValue(ACTION_COMMAND_KEY, "save");
        putValue(SHORT_DESCRIPTION, "save the edited file");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
    }
    
    public void actionPerformed(ActionEvent e) {
        
        if(fileChooser == null) {
            fileChooser = new JFileChooser(".");
        }
        
        PFileEditor editor = (PFileEditor) getValue(BarManager.PARENT_FRAME);
        File selectedFile = editor.getFile();
        
        if(selectedFile == null) {
            
        }
            if(fileChooser == null) {
                fileChooser = new JFileChooser(".");
            }

            while(true) {

                int result = fileChooser.showSaveDialog(editor);
                if(result == JFileChooser.APPROVE_OPTION) {

                    selectedFile = fileChooser.getSelectedFile();
                    if(selectedFile.exists()) {
                        result = JOptionPane.showConfirmDialog(editor, "File " +
                                selectedFile + " exists. Overwrite?", 
                                "Overwrite file", JOptionPane.YES_NO_CANCEL_OPTION);
                        if(result == JOptionPane.NO_OPTION)
                            continue;
                        if(result == JOptionPane.CANCEL_OPTION)
                            return;
                    }

                }
            }
}


    public void stateChanged(StateChangeEvent e) {
    }

}
