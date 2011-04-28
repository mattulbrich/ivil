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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import de.uka.iti.pseudo.gui.sequent.TermComponent;
import de.uka.iti.pseudo.proof.ProofNode;

/**
 * @author timm.felden@felden.com
 * 
 *         This class implements string based drag and drop for TermComponent.
 *         This allows for fast interactive rule applications by dropping terms
 *         from TermComponents or unformatted text from other applications such
 *         as text editors.
 */
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
                ProofNode target;
                if (null!= (target = tc.dropTermOnLocation(text))) {
                    // select the most interesting node
                    if (target.getChildren().size() > 0)
                        tc.getProofCenter().fireSelectedProofNode(target.getChildren().get(0));
                    else if (tc.getProofCenter().getProof().hasOpenGoals())
                        tc.getProofCenter().fireSelectedProofNode(tc.getProofCenter().getProof().getGoalbyNumber(0));
                    else
                        tc.getProofCenter().fireSelectedProofNode(tc.getProofCenter().getProof().getRoot());

                    return true;
                }
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
