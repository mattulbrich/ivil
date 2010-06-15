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
package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager;
import de.uka.iti.pseudo.util.GUIUtil;

// TODO Documentation needed
public class StartupWindow extends JFrame {
    private BarManager barManager;
    
    public StartupWindow() throws IOException {
        super("ivil");
        makeGUI();
    }

    private void makeGUI() throws IOException {
        {
            URL resource = getClass().getResource("bar/menu.properties");
            if(resource == null)
                throw new IOException("resource bar/menu.properties not found");
            barManager = new BarManager(null, resource);
            barManager.putProperty(BarAction.PARENT_FRAME, this);
            setJMenuBar(barManager.makeMenubar("menubar.startup"));
        }
        getContentPane().add(new JLabel(makeImage()), BorderLayout.CENTER);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
    }
    
    private Icon makeImage() {
        return GUIUtil.makeIcon(getClass().getResource("img/logo.png"));
    }
}
