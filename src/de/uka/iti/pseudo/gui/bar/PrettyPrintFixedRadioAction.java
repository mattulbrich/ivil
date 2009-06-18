package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.uka.iti.pseudo.gui.PrettyPrint;
import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class PrettyPrintFixedRadioAction extends BarAction 
    implements PropertyChangeListener, InitialisingAction {
    
    public PrettyPrintFixedRadioAction() {
        super("Print infix and prefix");
        // TODO tooltip
    }
    
    public void actionPerformed(ActionEvent e) {
        PrettyPrint pp = getProofCenter().getMainWindow().getSequentComponent().getPrettyPrinter();
        pp.setPrintingFix(isSelected());
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        PrettyPrint pp = (PrettyPrint) evt.getSource();
        setSelected(pp.isPrintingFix());
    }
    
    public void initialised() {
        PrettyPrint pp = getProofCenter().getMainWindow().getSequentComponent().getPrettyPrinter();
        pp.addPropertyChangeListener(PrettyPrint.PRINT_FIX_PROPERTY, this);
        setSelected(pp.isPrintingFix());
    }

}
