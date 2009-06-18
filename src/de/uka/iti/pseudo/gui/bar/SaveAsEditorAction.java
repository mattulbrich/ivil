package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.uka.iti.pseudo.gui.MainWindow;
import de.uka.iti.pseudo.gui.editor.PFileEditor;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class SaveAsEditorAction extends AbstractStateListeningAction {
    
    private JFileChooser fileChooser;
    
    public SaveAsEditorAction() {
        super("Save proof ...");
        putValue(ACTION_COMMAND_KEY, "save");
        putValue(SHORT_DESCRIPTION, "save the edited file under a selected name");
    }
    
    public void actionPerformed(ActionEvent e) {
        if(fileChooser == null) {
            fileChooser = new JFileChooser(".");
        }
        
        while(true) {
            PFileEditor editor = (PFileEditor) getValue(BarManager.PARENT_FRAME);
            
            int result = fileChooser.showSaveDialog(editor);
            if(result == JFileChooser.APPROVE_OPTION) {

                File selectedFile = fileChooser.getSelectedFile();
                if(selectedFile.exists()) {
                    result = JOptionPane.showConfirmDialog(editor, "File " +
                            selectedFile + " exists. Overwrite?", 
                            "Overwrite file", JOptionPane.YES_NO_CANCEL_OPTION);
                    if(result == JOptionPane.NO_OPTION)
                        continue;
                    if(result == JOptionPane.CANCEL_OPTION)
                        return;
                }
                
                FileWriter fileWriter= null;
                try {
                    fileWriter = new FileWriter(selectedFile);
                    
                    fileWriter.write(editor.getContent());
                    fileWriter.flush();
                    editor.changesSaved();
                    
                } catch (Exception ex) {
                    // TODO gescheiter Fehlerdialog
                    ex.printStackTrace();
                } finally {
                    if(fileWriter != null)
                        try { fileWriter.close();
                        } catch (IOException ioex) {
                            ioex.printStackTrace();
                        }
                }
                return;
            }
        }
    public void stateChanged(StateChangeEvent e) {
    }

}
