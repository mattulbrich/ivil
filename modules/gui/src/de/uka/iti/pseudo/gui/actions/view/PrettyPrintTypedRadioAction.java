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
package de.uka.iti.pseudo.gui.actions.view;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.KeyStroke;

import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;


/**
 * This action is one item in the pretty printer settings menu.
 * 
 * It allows to enable and disable printing of types in the sequent.
 */
@SuppressWarnings("serial") 
public class PrettyPrintTypedRadioAction extends BarAction 
    implements PropertyChangeListener, InitialisingAction {
    
    public PrettyPrintTypedRadioAction() {
        super("Print types");
        putValue(MNEMONIC_KEY, KeyEvent.VK_T);
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK ));
        putValue(SHORT_DESCRIPTION, "Include type information into the display");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        PrettyPrint pp = getProofCenter().getPrettyPrinter();
        pp.setTyped(isSelected());
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        PrettyPrint pp = (PrettyPrint) evt.getSource();
        setSelected(pp.isTyped());
    }
    
    @Override
    public void initialised() {
        PrettyPrint pp = getProofCenter().getPrettyPrinter();
        pp.addPropertyChangeListener(PrettyPrint.TYPED_PROPERTY, this);
        setSelected(pp.isTyped());
    }

}
