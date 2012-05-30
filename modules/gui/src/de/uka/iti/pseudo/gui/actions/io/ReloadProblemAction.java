/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions.io;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.prefs.Preferences;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.util.ExceptionDialog;

/**
 * This allows for reloading of the problem. If no problem was loaded yet, the
 * last attempt to load a file will be repeated.
 * 
 * @author timm.felden@felden.com
 */

public class ReloadProblemAction extends BarAction implements
		PropertyChangeListener {
	
    private static final long serialVersionUID = 8652614246864976171L;

    public void initialised() {
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        setEnabled(!(Boolean)evt.getNewValue());
    }

    public void actionPerformed(ActionEvent e) {
        // Buggy: Two windows with same content would pop up. (MU)
//        // try to get the path of the currently opened problem; if there is one
//        // open it
//        try {
//            URL url = new URL(getProofCenter().getEnvironment().getResourceName());
//            Main.openProverFromURL(url);
//        } catch (Exception ex) {
//            // this exception indicates, that there is no problem, which can be
//            // reloaded, so let us just continue
//        }

        // get url of the last problem
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        String allProblems = prefs.get("recent problems", null);
        
        if(allProblems == null) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), 
                    "The history contains no problem to be reloaded");
            return;
        }
         
        String recent[] = allProblems.split("\n");

        try {
            URL url = new URL(recent[0]);
            Main.openProverFromURL(url);
        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
        }
    }
}
