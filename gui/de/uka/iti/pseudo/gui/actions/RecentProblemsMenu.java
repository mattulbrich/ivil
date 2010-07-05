/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.util.ExceptionDialog;

// TODO DOC

@SuppressWarnings("serial")
public class RecentProblemsMenu extends JMenu implements MenuListener {

    static public class LoadProblem extends BarAction implements
            PropertyChangeListener {
        private final String location;

        /**
         * @param location
         *            URL to the problem to be loaded
         */
        public LoadProblem(String location) {
            //use only the last part of the URL in the menu
            super(location.substring(location.lastIndexOf('/') + 1));
            this.location = location;
            putValue(SHORT_DESCRIPTION, "Load the problem at " + location);
        }

        public void initialised() {
            getProofCenter().addPropertyChangeListener(
                    ProofCenter.ONGOING_PROOF, this);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            setEnabled(!(Boolean) evt.getNewValue());
        }

        public void actionPerformed(ActionEvent e) {
            try {
                Main.openProverFromURL(new URL(location));
            } catch (Exception ex) {
                ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
            }
        }

    }

    public RecentProblemsMenu() {
        super("Recent problems ...");
        add(new JMenuItem("empty"));
        addMenuListener(this);
    }

    @Override
    public void menuCanceled(MenuEvent e) {
    }

    @Override
    public void menuDeselected(MenuEvent e) {
    }

    @Override
    public void menuSelected(MenuEvent e) {
        removeAll();
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        String recent[] = prefs.get("recent problems", "").split("\n");
        for (int i = 0; i < recent.length; i++)
            add(new JMenuItem(new LoadProblem(recent[i])));
    }

}
