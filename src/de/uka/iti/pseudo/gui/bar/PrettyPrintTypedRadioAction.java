package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.uka.iti.pseudo.gui.PrettyPrint;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class PrettyPrintTypedRadioAction extends BarAction implements PropertyChangeListener {
    
    public PrettyPrintTypedRadioAction() {
        super("Print types");
        // TODO tooltip
    }
    
    public void actionPerformed(ActionEvent e) {
        PrettyPrint pp = getProofCenter().getMainWindow().getSequentComponent().getPrettyPrinter();
        pp.setTyped(isSelected());
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        PrettyPrint pp = (PrettyPrint) evt.getSource();
        setSelected(pp.isTyped());
    }
    
    public void initialised() {
        PrettyPrint pp = getProofCenter().getMainWindow().getSequentComponent().getPrettyPrinter();
        pp.addPropertyChangeListener(PrettyPrint.PRINT_FIX_PROPERTY, this);
        setSelected(pp.isTyped());
    }

}
