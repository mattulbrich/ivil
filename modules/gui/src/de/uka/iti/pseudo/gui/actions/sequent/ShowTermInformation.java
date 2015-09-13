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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
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
import de.uka.iti.pseudo.proof.SubtermSelector;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;
import de.uka.iti.pseudo.util.settings.Settings;

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

    private String text = "";
    private final JDialog window;
    private final JEditorPane editorPane;

    private static final Color BACKGROUND = Settings.getInstance().getColor("pseudo.sequentview.background",
            Color.WHITE);

    public ShowTermInformation() {
        putValue(NAME, "Show information");
        putValue(SHORT_DESCRIPTION, "Show information for this formula.");

        window = new JDialog(getParentFrame(), false);
        window.setTitle("History");
        Container cp = window.getContentPane();
        cp.setLayout(new BorderLayout());

        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setBackground(BACKGROUND);
        editorPane.setContentType("text/html");
        editorPane.setText(text);

        // Put the editor pane in a scroll pane.
        JScrollPane editorScrollPane = new JScrollPane(editorPane);
        editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setMinimumSize(new Dimension(10, 10));
        editorScrollPane.setPreferredSize(new Dimension(600, 200));

        cp.add(editorScrollPane);

        window.pack();
        window.setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (text.length() != 0) {
            window.setVisible(true);
        }
    }

    // initialise myself as listener to the proof center
    @Override
    public void initialised() {
        getProofCenter().addPropertyChangeListener(TermComponent.TERM_COMPONENT_SELECTED_TAG, this);
        getProofCenter().addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ProofCenter.ONGOING_PROOF.equals(evt.getPropertyName())) {
            setEnabled(!(Boolean) evt.getNewValue());
            // close the information window, as the history wont update during
            // auto proofing
            if (!isEnabled()) {
                window.setVisible(false);
            }

        } else if(TermComponent.TERM_COMPONENT_SELECTED_TAG.equals(evt.getPropertyName())) {
            text = "";

            TermComponent component = (TermComponent) evt.getNewValue();
            SubtermSelector selectedTermTag = component.getMouseSelection();
            if (null == selectedTermTag) {
                return;
            }

            if (Boolean.TRUE.equals(getProofCenter().getProperty(ProofCenter.ONGOING_PROOF))) {
                return;
            }

            text = component.makeFormatedTermHistory(selectedTermTag);
            editorPane.setText(text);
            editorPane.updateUI();
            editorPane.setBackground(BACKGROUND);
            editorPane.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
        }
    }
}