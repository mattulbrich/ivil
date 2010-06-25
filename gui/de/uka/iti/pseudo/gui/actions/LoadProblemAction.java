/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Util;

/**
 * This is the action to load a problem file.
 * 
 * It is embedded into the menu.
 */
@SuppressWarnings("serial") 
public class LoadProblemAction extends BarAction implements PropertyChangeListener {

    public LoadProblemAction() {
        super("Load problem ...", GUIUtil.makeIcon(LoadProblemAction.class.getResource("img/page_white_text.png")));
        putValue(ACTION_COMMAND_KEY, "loadProb");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        putValue(SHORT_DESCRIPTION, "open a problem file into a new window");
    }
    
    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean)evt.getNewValue());
    }
    
    public void actionPerformed(ActionEvent e) {
        
        JFileChooser fileChooser = Main.makeFileChooser(Main.PROBLEM_FILE);
        int result = fileChooser.showOpenDialog(getParentFrame());
        if(result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                ProofCenter pc = Main.openProver(selectedFile);
                addToRecentFiles(selectedFile);
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

    private void addToRecentFiles(File selectedFile) {
        Preferences prefs = Preferences.userNodeForPackage( Main.class );
        String recent[] = prefs.get("recent files", "").split("\n");
        List<String> recentList = new LinkedList<String>(Util.readOnlyArrayList(recent));
        recentList.add(0, selectedFile.getAbsolutePath());
        while(recentList.size() > 10)
            recentList.remove(recentList.size() - 1);

        String newVal = Util.join((Collection<?>)recentList, "\n");
        prefs.put("recent files", newVal);
    }

}
