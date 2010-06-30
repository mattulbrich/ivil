package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

// FIXME FOR DEMONSTRATION PURPOSES ONLY ... WILL BE CHANGED

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
            super(location.split("/")[location.split("/").length-1]);
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
