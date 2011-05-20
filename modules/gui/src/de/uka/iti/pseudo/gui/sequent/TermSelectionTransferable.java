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
package de.uka.iti.pseudo.gui.sequent;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import de.uka.iti.pseudo.proof.TermSelector;

public class TermSelectionTransferable implements Transferable {

    public static final DataFlavor TERM_DATA_FLAVOR = new DataFlavor(TermSelectionTransferable.class, "term selection");

    private static final DataFlavor[] FLAVOURS = { DataFlavor.stringFlavor, TERM_DATA_FLAVOR };
    
    /**
     * The String representation is used to transfer terms between applications.
     */
    private final String text;
    /**
     * The selector transfers the position of the term in the sequent of the
     * current proof node.
     */
    private final TermSelector selector;
    /**
     * The source component is needed to ensure that the drag location and the
     * drop location belong to the same proof(center).
     */
    private final TermComponent sourceComponent;

    public TermSelectionTransferable(TermComponent sourceComponent, TermSelector selector, String text) {
        this.text = text;
        this.selector = selector;
        this.sourceComponent = sourceComponent;
    }

    public String toString() {
        return text;
    }

    public TermSelector getSelector() {
        return selector;
    }

    public TermComponent getSource() {
        return sourceComponent;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(FLAVOURS[0])) {
            return text;
        }
        if (flavor.equals(FLAVOURS[1])) {
            return this;
        }
        throw new UnsupportedFlavorException(flavor);
    }

    public DataFlavor[] getTransferDataFlavors() {
        return FLAVOURS;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor f : FLAVOURS) {
            if (flavor.equals(f))
                return true;
        }
        return false;
    }

}