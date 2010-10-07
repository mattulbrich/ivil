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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.KeyStroke;

import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class PrettyPrintPluginsRadioAction extends BarAction 
    implements PropertyChangeListener, InitialisingAction {
    
    public PrettyPrintPluginsRadioAction() {
        super("Use installed pretty printers");
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK ));
        putValue(MNEMONIC_KEY, KeyEvent.VK_P);
        putValue(SHORT_DESCRIPTION, "Include type information into the display");
    }
    
    public void actionPerformed(ActionEvent e) {
        PrettyPrint pp = getProofCenter().getPrettyPrinter();
        pp.setPrintingPlugins(isSelected());
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        PrettyPrint pp = (PrettyPrint) evt.getSource();
        setSelected(pp.isPrintingPlugins());
    }
    
    public void initialised() {
        PrettyPrint pp = getProofCenter().getPrettyPrinter();
        pp.addPropertyChangeListener(PrettyPrint.PRINT_PLUGINS_PROPERTY, this);
        setSelected(pp.isPrintingPlugins());
    }

}
