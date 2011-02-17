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
package de.uka.iti.pseudo.gui.actions.sequent;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.gui.sequent.TermComponent;
import de.uka.iti.pseudo.prettyprint.TermTag;
import de.uka.iti.pseudo.term.Term;

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
 implements InitialisingAction, PropertyChangeListener, ClipboardOwner {

    private Term target = null;

    public CopyTerm() {
        putValue(NAME, "Copy");
        putValue(SHORT_DESCRIPTION, "Copy the selected term to the clipboard.");
    }

    @Override 
    public void actionPerformed(ActionEvent e) {
        if (null != target) {
            StringSelection stringSelection = new StringSelection(target.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, this);
        }
    }

    // initialise myself as listener to the proof center
    @Override
    public void initialised() {
        getProofCenter().addPropertyChangeListener(TermComponent.TERM_COMPONENT_SELECTED_TAG, this);
    }

    @Override public void propertyChange(PropertyChangeEvent evt) {
        assert TermComponent.TERM_COMPONENT_SELECTED_TAG.equals(evt.getPropertyName());
        
        TermTag selectedTermTag = ((TermComponent) evt.getNewValue()).getMouseSelection();
        if (null == selectedTermTag)
            return;
        
        target = selectedTermTag.getTerm();
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // we don't care if we lost ownership of the clipboard
    }
}