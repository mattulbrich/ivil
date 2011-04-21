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
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;

import de.uka.iti.pseudo.gui.sequent.TermComponent;
import de.uka.iti.pseudo.proof.TermSelector;

// TODO DOC
// Is this drag and drop?

public class TermSelectionTransfer extends TransferHandler {

    private static final long serialVersionUID = -1292983185215324664L;

    public int getSourceActions(JComponent c) {
        Log.enter(c);
        return COPY;
    }

    protected Transferable createTransferable(JComponent c) {
        Log.enter(c);
        if (c instanceof TermComponent) {
            TermComponent tc = (TermComponent) c;
            return tc.createTransferable();
        }
        return null;
    }
    
    public boolean importData(TransferSupport support) {
        Log.enter(support);
        try {
            Component c = support.getComponent();
            if (c instanceof TermComponent) {
                TermComponent tc = (TermComponent) c;
                Transferable t = support.getTransferable();
                
                String text = (String) t.getTransferData(DataFlavor.stringFlavor);
                return tc.dropTermOnLocation(text, support.getDropLocation().getDropPoint());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.leave();
        }
        return false;
    }
    
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
        Log.enter(comp, Arrays.asList(transferFlavors));
        for (DataFlavor dataFlavor : transferFlavors) {
            if (dataFlavor.equals(DataFlavor.stringFlavor)) {
                return true;
            }
        }
        return false;
    }
}
