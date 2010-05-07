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

import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class PrettyPrintFixedRadioAction extends BarAction 
    implements PropertyChangeListener, InitialisingAction {
    
    public PrettyPrintFixedRadioAction() {
        super("Print infix and prefix");
        // TODO tooltip
    }
    
    public void actionPerformed(ActionEvent e) {
        PrettyPrint pp = getProofCenter().getPrettyPrinter();
        pp.setPrintingFix(isSelected());
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        PrettyPrint pp = (PrettyPrint) evt.getSource();
        setSelected(pp.isPrintingFix());
    }
    
    public void initialised() {
        PrettyPrint pp = getProofCenter().getPrettyPrinter();
        pp.addPropertyChangeListener(PrettyPrint.PRINT_FIX_PROPERTY, this);
        setSelected(pp.isPrintingFix());
    }

}
