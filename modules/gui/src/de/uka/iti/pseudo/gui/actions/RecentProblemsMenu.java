/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import nonnull.Nullable;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.Log;

/**
 * Presents a List of recently used problems. Very useful for manual testing and
 * demonstration.
 *
 * @author timm.felden@felden.com, mulbrich@ira.uka.de
 */
public class RecentProblemsMenu extends JMenu implements MenuListener {

    private static final long serialVersionUID = 2656732349530151485L;

    private class LoadProblem extends AbstractAction implements PropertyChangeListener {
        private static final long serialVersionUID = 6547255936403664041L;
        private final String location;

        /**
         * @param location
         *            URL to the problem to be loaded
         */
        public LoadProblem(String location) {
            // use only the last part of the URL in the menu
            super(location.substring(location.lastIndexOf('/') + 1));
            this.location = location;
            putValue(SHORT_DESCRIPTION, "Load the problem at " + location);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            setEnabled(!(Boolean) evt.getNewValue());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                URL url = new URL(location);
                try {
                    Main.openProverFromURL(url);
                } catch (IOException ex) {
                    // prevent the exception from going to the next catch-all
                    throw ex;
                } catch(Exception ex) {
                    Log.log(Log.DEBUG, ex);
                    if(url.getProtocol().equals("file")) {
                        String selectedFile = url.getFile();
                        String message = "'" + selectedFile + "' cannot be loaded. " +
                                "Do you want to open an editor to analyse?";
                        boolean answer = ExceptionDialog.showExceptionDialog(
                                RecentProblemsMenu.this.getParentFrame(),
                                message, ex, "Open in Editor");

                        if(answer) {
                            Main.openEditor(new File(selectedFile));
                        }
                    } else {
                        ExceptionDialog.showExceptionDialog(
                                RecentProblemsMenu.this.getParentFrame(), ex);
                    }
                }
            } catch(IOException ex) {
                ExceptionDialog.showExceptionDialog(
                        RecentProblemsMenu.this.getParentFrame(), ex);
            }
        }
    }

    public RecentProblemsMenu() {
        super("Recent problems ...");
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
        String recent[] = getRecentProblems();

        if(recent.length > 0) {
            for (int i = 0; i < recent.length; i++) {
                add(new JMenuItem(new LoadProblem(recent[i])));
            }
        } else {
            // Indicate that nothing can be reopened
            JMenuItem item = new JMenuItem("empty.");
            item.setEnabled(false);
            add(item);
        }
    }

    private static String[] getRecentProblems() {
        Preferences prefs = Preferences.userNodeForPackage(Main.class);
        String allProblems = prefs.get("recent problems", null);

        if(allProblems != null) {
            String recent[] = allProblems.split("\n");
            return recent;
        } else {
            return new String[0];
        }
    }

    /**
     * Queries the preferences for the most recent problem.
     * Used from the Main class
     *
     * @returns the most recently loaded problem, <code>null</code> if no such element exists.
     */
    public static @Nullable String getMostRecentProblem() {
        String recent[] = getRecentProblems();
        if(recent.length > 0) {
            return recent[0];
        } else {
            return null;
        }
    }

    /**
     * @return the parentWindow
     */
    public JFrame getParentFrame() {
        return (JFrame) getClientProperty(BarAction.PARENT_FRAME);
    }

}
