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
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;

import javax.swing.JOptionPane;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * This is the action to load a problem file.
 * 
 * It is embedded into the menu.
 */
@SuppressWarnings("serial") 
public class LoadProblemURLAction extends BarAction implements PropertyChangeListener {

    public LoadProblemURLAction() {
        super("Load problem from URL ...", GUIUtil.makeIcon(LoadProblemURLAction.class.getResource("img/page_white_world.png")));
        putValue(ACTION_COMMAND_KEY, "loadProbURL");
        putValue(MNEMONIC_KEY, KeyEvent.VK_U);
        putValue(SHORT_DESCRIPTION, "open a problem from a URL into a new window");
    }
    
    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean)evt.getNewValue());
    }
    
    public void actionPerformed(ActionEvent e) {
        
        Settings settings = Settings.getInstance();
        String initialValue = settings.getProperty("pseudo.defaultURL", "http://");
        
        String result = JOptionPane.showInputDialog(getParentFrame(), "Enter the URL of the problem file to load", initialValue);
        if(result != null) {
            
            try {
                URL url = new URL(result);
                Main.openProverFromURL(url);
            } catch(Exception ex) {
                ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
            }       
            
        }
    }

}
