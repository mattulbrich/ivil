/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions.proof;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.PluginManager;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.extensions.ContextExtension;
import de.uka.iti.pseudo.util.ExceptionDialog;

public class ContextExtensionsMenu extends JMenu implements MenuListener {

    private static final long serialVersionUID = 2656732349530151485L;

    private class ApplyExtensionAction extends AbstractAction {
        private static final long serialVersionUID = 6547255936403664041L;
        private final ContextExtension ext;

        /**
         * @param location
         *            URL to the problem to be loaded
         */
        public ApplyExtensionAction(ContextExtension ext) {
            super(ext.getKey());
            this.ext = ext;
            putValue(SHORT_DESCRIPTION, ext.getDescription());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                ext.run(getProofCenter());
            } catch (Exception ex) {
                ExceptionDialog.showExceptionDialog(getProofCenter().getMainWindow(), ex);
            }
        }

    }

    private List<ContextExtension> extensions;

    public ContextExtensionsMenu() {
        super("Extensions ...");
        setToolTipText("Proof scenarios can register additional actions which can be accessed here");
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
        ProofCenter center = getProofCenter();
        assert center != null;

        if(extensions == null) {
            try {
                PluginManager man = center.getEnvironment().getPluginManager();
                this.extensions = man.getPlugins("contextExtension", ContextExtension.class);
            } catch (EnvironmentException ex) {
                ExceptionDialog.showExceptionDialog(center.getMainWindow(),
                        "An error occurred while getting the extensions", ex);
                return;
            }
        }

        int count = 0;
        for (ContextExtension ext : extensions) {
            if(ext.shouldOffer(center)) {
                add(new JMenuItem(new ApplyExtensionAction(ext)));
                count ++;
            }
        }

        if(count == 0) {
            JMenuItem nothing = new JMenuItem("No extensions applicable");
            nothing.setEnabled(false);
            add(nothing);
        }
    }

    private ProofCenter getProofCenter() {
        ProofCenter center = (ProofCenter)getClientProperty(BarAction.CENTER);
        return center;
    }

}
