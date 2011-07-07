/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2011 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.editor;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.Log;

/**
 * A modification listener can be added to listen to changes in an
 * {@link PFileEditor}. It reacts on changes to the document and on the mouse
 * pointer reentering the frame.
 * 
 * It then checks whether the underlying file has a changed modification date.
 * If so, it has been altered outside the editor. The user is asked whether she
 * wants to reload the file from disk or discard the external changes.
 * 
 * @author mattias ulbrich
 * @see PFileEditor
 */
public class ModificationListener implements DocumentListener, WindowListener {

    private final PFileEditor editor;
    private File editedFile;
    private long expectedModTime;
    private boolean inAction;
    
    private Runnable checkRunnable = new Runnable() {
        public void run() {
            checkForConcurrentMod();
        }
    };

    public ModificationListener(PFileEditor editor) {
        this.editor = editor;
    }
    
    private void checkForConcurrentMod() {
        Log.enter();
        if (!inAction &&
                editedFile != null &&
                editedFile.lastModified() != expectedModTime) {
                
            Log.log(Log.DEBUG, "File modification outside this program: Time=" + 
                    editedFile.lastModified() + " was: " + expectedModTime);
                
            String mess = "The file "
                + editedFile
                + " has been changed on the file system.\nDo you "
                + "want to reread the file from disk, overwriting "
                + "this buffer?";

            int res = JOptionPane.showConfirmDialog(editor, mess,
                    "File has been changed", JOptionPane.YES_NO_OPTION);

            if (res == JOptionPane.YES_OPTION) {
                try {
                    inAction = true;
                    editor.loadFile(editedFile);
                    inAction = false;
                } catch (IOException e) {
                    ExceptionDialog.showExceptionDialog(editor, e);
                }
            } else {
                expectedModTime = editedFile.lastModified();
            }
        }
    }

    public void setEditedFile(File file) {
        Log.enter(file);
        this.editedFile = file;
        if(file != null) {
            this.expectedModTime = file.lastModified();
        } else {
            this.expectedModTime = -1;
        }
    }
    
    // --- document listener
    /*
     * These listeners are a little "touchy" when it comes to changing the
     * document they are listening to. And since the above code spawns a new
     * event queue (via JOptionPane), there were cases which lead to exceptions
     * ("Attempt to mutate in notification"). Bugfix: Do the actual handling
     * some time after this event.
     */

    @Override
    public void changedUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(checkRunnable);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(checkRunnable);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(checkRunnable);
    }
    
    // --- window listener

    @Override
    public void windowActivated(WindowEvent e) {
        checkForConcurrentMod();
    }

    @Override
    public void windowClosed(WindowEvent e) {
        // do nothing
    }

    @Override
    public void windowClosing(WindowEvent e) {
        // do nothing
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // do nothing
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // do nothing
    }

    @Override
    public void windowIconified(WindowEvent e) {
        // do nothing
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // do nothing
    }

}
