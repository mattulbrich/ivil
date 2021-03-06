/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;

// TODO Documentation needed
public class StartupWindow extends JFrame {
    private static final long serialVersionUID = -250971288261688572L;

    private BarManager barManager;

    public StartupWindow() throws IOException {
        super("ivil");
        makeGUI();
    }

    private void makeGUI() throws IOException {
        {
            URL resource = getClass().getResource("actions/menu.xml");
            if(resource == null) {
                throw new IOException("resource actions/menu.xml not found");
            }
            barManager = new BarManager(null, resource);
            barManager.putProperty(BarAction.PARENT_FRAME, this);
            setJMenuBar(barManager.makeMenubar("startup.menubar"));
        }
        getContentPane().add(new JLabel(makeImage()), BorderLayout.CENTER);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
    }

    private Icon makeImage() {
        return GUIUtil.makeIcon(getClass().getResource("img/logo.png"));
    }

    public void showSampleBrowser() {
        Action action = barManager.getAction("file.sampleBrowser");
        if(action == null) {
            ExceptionDialog.showExceptionDialog(this, "The sample browser cannot be initialised");
        } else {
            action.actionPerformed(new ActionEvent(this, 0, "commandline"));
        }
    }
}
