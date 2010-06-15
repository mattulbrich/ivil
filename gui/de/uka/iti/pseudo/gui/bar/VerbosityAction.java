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
package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JOptionPane;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.ProofComponent;
import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;

/**
 * This action is used to bring up a simple dialog from which the verbosity of
 * the proof tree window can be determined.
 * 
 * <p>The value ranges from 1 to {@value #MAX_VALUE}. By default, the value 
 * {@value #DEFAULT_VERBOSITY} is selected.
 */
@SuppressWarnings("serial") 
public class VerbosityAction extends BarAction
        implements InitialisingAction, PropertyChangeListener {

    public static final String TREE_VERBOSITY = ProofComponent.VERBOSITY_PROPERTY;
    public static final int DEFAULT_VERBOSITY = ProofComponent.DEFAULT_VERBOSITY;
    public static final int MAX_VALUE = 10;
    
    private static String MESSAGE = "<html>Choose the level of verbosity in which the proof<br>"
            + "tree component displays the proof.<br>"
            + "<font size=\"-1\">A choice of 1 only shows the most important rules,<br>"
            + "while a choice of "
            + MAX_VALUE + " displays every rule application.</font>";

    @Override public void actionPerformed(ActionEvent e) {
        
        ProofCenter pc = getProofCenter();

        Object[] values = new Object[MAX_VALUE];
        for (int i = 0; i < MAX_VALUE; i++) {
            values[i] = i+1;
        }

        Object preselected = pc.getProperty(TREE_VERBOSITY);

        Integer result = (Integer) JOptionPane.showInputDialog(
                getParentFrame(), MESSAGE, "Verbosity",
                JOptionPane.QUESTION_MESSAGE, null, values, preselected);
        
        if(result != null)
            pc.firePropertyChange(TREE_VERBOSITY, result);
        
    }

    @Override public void initialised() {
        ProofCenter pc = getProofCenter();
        pc.addPropertyChangeListener(TREE_VERBOSITY, this);

        pc.firePropertyChange(TREE_VERBOSITY, DEFAULT_VERBOSITY);
    }

    @Override public void propertyChange(PropertyChangeEvent evt) {
        putValue(Action.NAME, "Verbosity in Tree (now: " + evt.getNewValue() + ")");
    }

}
