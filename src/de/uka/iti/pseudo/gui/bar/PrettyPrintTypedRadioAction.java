package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.uka.iti.pseudo.gui.PrettyPrint;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class PrettyPrintTypedRadioAction extends AbstractStateListeningAction implements PropertyChangeListener {
    
    public PrettyPrintTypedRadioAction() {
        super("Print types");
        // TODO tooltip
    }
    
    @Override 
    public void actionPerformed(ActionEvent e) {
        PrettyPrint pp = getProofCenter().getMainWindow().getSequentComponent().getPrettyPrinter();
        pp.setTyped(isSelected());
    }

    @Override 
    public void stateChanged(StateChangeEvent e) {
        if(e.getState().equals(StateChangeEvent.INITIALISED)) {
            PrettyPrint pp = getProofCenter().getMainWindow().getSequentComponent().getPrettyPrinter();
            pp.addPropertyChangeListener(this);
            setSelected(pp.isTyped());
        }
    }

    @Override 
    public void propertyChange(PropertyChangeEvent evt) {
        PrettyPrint pp = (PrettyPrint) evt.getSource();
        setSelected(pp.isTyped());
    }

}
