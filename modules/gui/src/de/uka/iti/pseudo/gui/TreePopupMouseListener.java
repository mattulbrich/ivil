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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

public class TreePopupMouseListener extends MouseAdapter {
    
    private ProofComponent theTree;
    private JPopupMenu popupMenu;

    public TreePopupMouseListener(ProofComponent theTree, JPopupMenu popupMenu) {
        this.theTree = theTree;
        this.popupMenu = popupMenu;
    }

    @Override public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger()) {
            int row = theTree.getRowForLocation(e.getX(), e.getY());
            if (row == -1)
                return;
            theTree.setSelectionRow(row);

            popupMenu.show(theTree, e.getX(), e.getY());
        }
    }

    @Override public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            int row = theTree.getRowForLocation(e.getX(), e.getY());
            if (row == -1)
                return;
            theTree.setSelectionRow(row);

            popupMenu.show(theTree, e.getX(), e.getY());
        }
    }

    @Override public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            int row = theTree.getRowForLocation(e.getX(), e.getY());
            if (row == -1)
                return;
            theTree.setSelectionRow(row);

            popupMenu.show(theTree, e.getX(), e.getY());
        }
    }

}
