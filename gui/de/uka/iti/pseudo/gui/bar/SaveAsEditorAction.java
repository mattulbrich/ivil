package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.util.ExceptionDialog;

//TODO Documentation needed
@SuppressWarnings("serial") 
public class SaveAsEditorAction extends BarAction {

    private JFileChooser fileChooser;

    public SaveAsEditorAction() {
        super("Save As ...");
        putValue(ACTION_COMMAND_KEY, "save");
        putValue(SHORT_DESCRIPTION, "save the edited file under a selected name");
    }

    public void actionPerformed(ActionEvent e) {
        if(fileChooser == null) {
            fileChooser = new JFileChooser(".");
        }

        while(true) {
            PFileEditor editor = (PFileEditor) getValue(PARENT_FRAME);

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

                saveUnder(editor, selectedFile);
                return;
            } else {
                return;
            }
        }
    }
    
    protected void saveUnder(PFileEditor editor, File selectedFile) {
        FileWriter fileWriter= null;
        try {
            backupFile(selectedFile);
            fileWriter = new FileWriter(selectedFile);

            fileWriter.write(editor.getContent());
            fileWriter.flush();
            editor.setHasChanges(false);
            editor.setFilename(selectedFile);

        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
        } finally {
            if(fileWriter != null)
                try { fileWriter.close();
                } catch (IOException ioex) {
                    ioex.printStackTrace();
                }
        }
    }

    private void backupFile(File file) {
        String name = file.getPath();
        for(int i = 0; i < 100; i++) {
            File backupFile = new File(name + "~" + i);
            if(!backupFile.exists()) {
                file.renameTo(backupFile);
                return;
            }
        }
        System.err.println("No backup made ... exceeding limit");
    }
    
}
