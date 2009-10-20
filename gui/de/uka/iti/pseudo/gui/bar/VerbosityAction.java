package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.JOptionPane;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.ProofComponent;
import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;

@SuppressWarnings("serial") 
public class VerbosityAction extends BarAction
        implements InitialisingAction, PropertyChangeListener {

    public static final String TREE_VERBOSITY = ProofComponent.VERBOSITY_PROPERTY;
    public static final int DEFAULT_VERBOSITY = ProofComponent.DEFAULT_VERBOSITY;
    public static final int MAX_VALUE = 10;

    @Override public void actionPerformed(ActionEvent e) {
        
        ProofCenter pc = getProofCenter();

        Object[] values = new Object[MAX_VALUE];
        for (int i = 0; i < values.length; i++) {
            values[i] = i + 1;
        }

        Object preselected = pc.getProperty(TREE_VERBOSITY);

        Integer result = (Integer) JOptionPane.showInputDialog(getParentFrame(),
                "blabblaba", "Verbosity", JOptionPane.QUESTION_MESSAGE, null,
                values, preselected);
        
        if(result != null)
            pc.firePropertyChange(TREE_VERBOSITY, result);
        
    }

    @Override public void initialised() {
        ProofCenter pc = getProofCenter();
        pc.addPropertyChangeListener(TREE_VERBOSITY, this);

        pc.firePropertyChange(TREE_VERBOSITY, 5);
    }

    @Override public void propertyChange(PropertyChangeEvent evt) {
        putValue(Action.NAME, "Verbosity in Tree");
        // + evt.getNewValue();                + ")");
    }

}
