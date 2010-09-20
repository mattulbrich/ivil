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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import de.uka.iti.pseudo.proof.SubtermSelector;
import de.uka.iti.pseudo.proof.TermSelector;

public class TermSelectionTransferable extends Pair<TermSelector, String> implements Transferable {
    
    public static final DataFlavor TERM_DATA_FLAVOR =
        new DataFlavor(TermSelector.class, "term selection");
    
    private static final DataFlavor[] FLAVOURS = { 
        DataFlavor.stringFlavor,
        TERM_DATA_FLAVOR
    };

    public TermSelectionTransferable(TermSelector fst, String snd) {
        super(fst, snd);
    }
    
    public String toString() {
        return snd();
    }

    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if(flavor.equals(FLAVOURS[0])) {
            return snd();
        }
        if(flavor.equals(FLAVOURS[1])) {
            return fst();
        }
        throw new UnsupportedFlavorException(flavor);
    }

    public DataFlavor[] getTransferDataFlavors() {
        return FLAVOURS;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor f : FLAVOURS) {
            if(flavor.equals(f))
                return true;
        }
        return false;
    }
    
}