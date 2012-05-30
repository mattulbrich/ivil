/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions.editor;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.settings.Settings;

//TODO Documentation needed
@SuppressWarnings("serial") 
public class SaveAsEditorAction extends BarAction {

    private static final int ROTATE_COUNT = 
            Settings.getInstance().getInteger("pseudo.countbackup", 10);
    
    public SaveAsEditorAction() {
        putValue(ACTION_COMMAND_KEY, "saveas");
    }

    public void actionPerformed(ActionEvent e) {

        JFileChooser fileChooser = Main.makeFileChooser(Main.PROBLEM_FILE);
        
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
            editor.setHasUnsavedChanges(false);
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

    /*
     * get the next file name for backup.
     * 
     * Append ~0, ~1, ..., ~n to the file name to create backup file names. Use
     * the oldest of the existing ones or the first which has not been created
     * yet.
     */
    private void backupFile(File file) {
        String name = file.getPath();
        
        File backupFile = null;
        for(int i = 0; i < ROTATE_COUNT; i++) {
            File f = new File(name + "~" + i);
            if(!f.exists()) {
                // does not exists: that is it
                backupFile = f;
                break;
            }
            if(backupFile == null 
                    || f.lastModified() < backupFile.lastModified()) {
                // change if older
                backupFile = f;
            }
        }
        
        assert backupFile != null : "nullness, there must be one backup file";
        
        file.renameTo(backupFile);
        Log.log(Log.DEBUG, file + " backed up to " + backupFile);
    }
    
}
