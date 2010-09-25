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

import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Util;

/**
 * Shows an About Box ... eventually
 */
public class AboutAction extends BarAction {

    private static final long serialVersionUID = -6434986871638617270L;
    
    private static final String VERSION_PATH = "/META-INF/VERSION";
    private static final String LOGO_PATH = "/de/uka/iti/pseudo/gui/img/logo.png";

    @Override
    public void actionPerformed(ActionEvent e) {

        final JDialog window = new JDialog(getParentFrame(), true);
        window.setTitle("ivil - About");
        Container cp = window.getContentPane();
        cp.setLayout(new GridBagLayout());
        Icon image = GUIUtil.makeIcon(getClass().getResource(LOGO_PATH));

        String version = "<unknown version>";
        try {
            URL resource = getClass().getResource(VERSION_PATH);
            if (resource != null)
                version = Util.readURLAsString(resource);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        JLabel versionLabel = new JLabel(version, null, SwingConstants.CENTER);
        cp.setBackground(Color.white);

        JButton button = new JButton("Dismiss");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                window.dispose();
            }
        });

        cp.add(new JLabel(image), new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                        0, 0, 0, 0), 0, 0));
        cp.add(versionLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                        10, 10, 10, 10), 0, 0));
        cp.add(button, new GridBagConstraints(0, 2, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
                        0, 0, 0, 0), 0, 0));

        window.pack();
        window.setVisible(true);
    }

    public static void main(String[] args) {
        new AboutAction().actionPerformed(null);
    }

}
