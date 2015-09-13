/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions.sequent;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.gui.sequent.TermComponent;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.SubtermSelector;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Log;

/**
 * GUI Action that copies the selected term to the clipboard without
 * prettyPrinting.
 *
 * <p>
 * This action is part of the popup menu in a term component. Before showing the
 * popup the property {@value TermComponent#TERM_COMPONENT_SELECTED_TAG} is set
 * to the currently mouse-selected term tag.
 */

@SuppressWarnings("serial")
public class CopyTerm
    extends BarAction
 implements InitialisingAction, ClipboardOwner {

    public CopyTerm() {
        putValue(NAME, "Copy");
        putValue(SHORT_DESCRIPTION, "Copy the selected term to the clipboard.");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        TermComponent termComp =
                (TermComponent) getProofCenter().
                   getProperty(TermComponent.TERM_COMPONENT_SELECTED_TAG);
        SubtermSelector selectedTermTag = termComp.getMouseSelection();

        if (null == selectedTermTag) {
            return;
        }

        Term target;
        try {
            target = selectedTermTag.selectSubterm(termComp.getTerm());
        } catch (ProofException e1) {
            Log.stacktrace(e1);
            return;
        }

        if (null == target) {
            return;
        }

        StringSelection stringSelection = new StringSelection(target.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, this);
    }

    // initialise myself as listener to the proof center
    @Override
    public void initialised() {
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // we don't care if we lost ownership of the clipboard
    }
}