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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.gui.sequent.TermComponent;
import de.uka.iti.pseudo.prettyprint.TermTag;

/**
 * GUI Action to show additional information about the selected term.
 * 
 * <p>
 * This action is part of the popup menu in a term component. Before showing the
 * popup the property {@value TermComponent#TERM_COMPONENT_SELECTED_TAG} is set
 * to the currently mouse-selected term tag.
 */

@SuppressWarnings("serial")
public class ShowTermInformation
    extends BarAction 
    implements InitialisingAction, PropertyChangeListener {

    private String text = null;

    public ShowTermInformation() {
        putValue(NAME, "Show information");
        putValue(SHORT_DESCRIPTION, "Show information for this formula.");
    }

    @Override 
    public void actionPerformed(ActionEvent e) {
        if (null != text) {
            final JDialog window = new JDialog(getParentFrame(), true);
            window.setTitle("History");
            Container cp = window.getContentPane();
            cp.setLayout(new GridBagLayout());

            JEditorPane editorPane = new JEditorPane();
            editorPane.setEditable(false);
            editorPane.setContentType("text/html");
            editorPane.setText(text);

            //Put the editor pane in a scroll pane.
            JScrollPane editorScrollPane = new JScrollPane(editorPane);
            editorScrollPane.setVerticalScrollBarPolicy(
                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(250, 145));
            editorScrollPane.setMinimumSize(new Dimension(10, 10));
            
            cp.add(editorScrollPane);

            window.pack();
            window.setVisible(true);
        }
    }

    // initialise myself as listener to the proof center
    @Override
    public void initialised() {
        getProofCenter().addPropertyChangeListener(TermComponent.TERM_COMPONENT_SELECTED_TAG, this);
    }

    @Override public void propertyChange(PropertyChangeEvent evt) {
        assert TermComponent.TERM_COMPONENT_SELECTED_TAG.equals(evt.getPropertyName());

        setEnabled(false);

        TermComponent component = (TermComponent) evt.getNewValue();
        TermTag selectedTermTag = component.getMouseSelection();
        if (null == selectedTermTag)
            return;

        if (Boolean.TRUE.equals(getProofCenter().getProperty(ProofCenter.ONGOING_PROOF)))
            return;

        text = component.makeFormatedTermHistory(selectedTermTag);
        setEnabled(true);
    }
    

}