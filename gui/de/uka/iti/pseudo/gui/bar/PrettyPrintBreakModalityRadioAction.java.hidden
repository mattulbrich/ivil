package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.uka.iti.pseudo.gui.PrettyPrint;

// TODO Documentation needed
@SuppressWarnings("serial") 
public class PrettyPrintBreakModalityRadioAction extends AbstractStateListeningAction implements PropertyChangeListener {
    
    public PrettyPrintBreakModalityRadioAction() {
        super("Break modalities");
        putValue(SHORT_DESCRIPTION, "if selected line breaks are added in modalities");
    }
    
    public void actionPerformed(ActionEvent e) {
        PrettyPrint pp = getProofCenter().getMainWindow().getSequentComponent().getPrettyPrinter();
        pp.setBreakModalities(isSelected());
    }

    public void stateChanged(StateChangeEvent e) {
        if(e.getState().equals(StateChangeEvent.INITIALISED)) {
            PrettyPrint pp = getProofCenter().getMainWindow().getSequentComponent().getPrettyPrinter();
            pp.addPropertyChangeListener(this);
            setSelected(pp.isBreakModalities());
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        PrettyPrint pp = (PrettyPrint) evt.getSource();
        setSelected(pp.isBreakModalities());
    }

}
