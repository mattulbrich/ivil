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
package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;

@SuppressWarnings("serial")
public class PopupOpenErrorLine extends BarAction implements InitialisingAction, PropertyChangeListener{

    private static final long serialVersionUID = 7937208369106413832L;
    
    private int errorLine;

    public PopupOpenErrorLine() {
        super("Jump to error");
        putValue(SHORT_DESCRIPTION, "move the edit cursor to the erreronesu line");
    }
    
    public void actionPerformed(ActionEvent e) {
        if(errorLine == 0)
            return;
            try {
                JTextArea editPane = getEditor().getEditPane();
                int offset = editPane.getLineStartOffset(errorLine-1);
                editPane.setCaretPosition(offset);
                editPane.requestFocus();
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
    }

    public void initialised() {
        getEditor().addPropertyChangeListener("errorLine", this);
        setEnabled(false);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        Integer newValue = (Integer) evt.getNewValue();
        errorLine = newValue == null ? 0 : newValue;
        setEnabled(errorLine != 0);
    }

}
