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
package de.uka.iti.pseudo.util;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import de.uka.iti.pseudo.gui.TermComponent;
import de.uka.iti.pseudo.proof.TermSelector;

// TODO DOC
// Is this drag and drop?

public class TermSelectionTransfer extends TransferHandler {

    private static final long serialVersionUID = -1292983185215324664L;

    public int getSourceActions(JComponent c) {
        return COPY;
    }

    protected Transferable createTransferable(JComponent c) {
        if (c instanceof TermComponent) {
            TermComponent tc = (TermComponent) c;
            return tc.createTransferable();
        }
        return null;
    }
    
    public boolean importData(TransferSupport support) {
        try {
            Component c = support.getComponent();
            if (c instanceof TermComponent) {
                TermComponent tc = (TermComponent) c;
                Transferable t = support.getTransferable();
                TermSelector ts = (TermSelector) t.getTransferData(TermSelectionTransferable.TERM_DATA_FLAVOR);
                Point point = support.getDropLocation().getDropPoint();
                return tc.dropTermOnLocation(ts, point);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        for (DataFlavor dataFlavor : transferFlavors) {
            if(dataFlavor.equals(TermSelectionTransferable.TERM_DATA_FLAVOR)) {
                return true;
            }
        }
        return false;
    }
}
