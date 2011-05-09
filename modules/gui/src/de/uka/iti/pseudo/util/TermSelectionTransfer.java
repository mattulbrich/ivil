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
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.text.JTextComponent;

import de.uka.iti.pseudo.gui.sequent.TermComponent;
import de.uka.iti.pseudo.proof.ProofNode;

/**
 * This class implements string based drag and drop for TermComponent. This
 * allows for fast interactive rule applications by dropping terms from
 * TermComponents or unformatted text from other applications such as text
 * editors.
 * 
 * @author timm.felden@felden.com
 */
public class TermSelectionTransfer extends TransferHandler {

    private static final long serialVersionUID = -1292983185215324664L;
    private static final TransferHandler instance = new TermSelectionTransfer();

    /**
     * The transfer handler has no state and is therefore thread safe and can be
     * used as singleton.
     */
    private TermSelectionTransfer(){
    }

    public int getSourceActions(JComponent c) {
        Log.enter(c);
        return COPY;
    }

    protected Transferable createTransferable(JComponent c) {
        Log.enter(c);
        if (c instanceof TermComponent) {
            TermComponent tc = (TermComponent) c;
            return tc.createTransferable();
        } else if (c instanceof JTextComponent)
            return new StringSelection(((JTextComponent) c).getText());

        return null;
    }
    
    @Override
    public boolean importData(TransferSupport support) {
        Log.enter(support);
        try {
            Component c = support.getComponent();
            Transferable t = support.getTransferable();
            if (c instanceof TermComponent) {
                TermComponent tc = (TermComponent) c;
                
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
            } else if (c instanceof JTextComponent) {
                String text = (String) t.getTransferData(DataFlavor.stringFlavor);
                ((JTextComponent) c).setText(text);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.leave();
        }
        return false;
    }
    
    @Override
    public boolean canImport(TransferSupport support) {
        
        if (support.getTransferable() instanceof TermSelectionTransferable)
            return true;

        return support.isDataFlavorSupported(DataFlavor.stringFlavor);
    }

    public static TransferHandler getInstance() {
        return instance;
    }
}
