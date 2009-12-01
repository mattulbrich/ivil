package de.uka.iti.pseudo.gui.bar;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.bar.BarManager.InitialisingAction;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * Simple Action to enable/disable background smt solver.
 * Used in Settings menu
 */
@SuppressWarnings("serial") 
public class SMTActivationAction extends BarAction implements InitialisingAction, PropertyChangeListener {

    private ProofCenter proofCenter;

    public SMTActivationAction() {
        super("Background SMT");
        putValue(SHORT_DESCRIPTION, "Activate background SMT solver");
    }
    
    public void initialised() {
        boolean selected = Settings.getInstance().getBoolean("pseudo.smt.background");
        
        proofCenter = getProofCenter();
        proofCenter.addPropertyChangeListener(SMTBackgroundAction.SMT_BACKGROUND_PROPERTY, this);
        proofCenter.firePropertyChange(SMTBackgroundAction.SMT_BACKGROUND_PROPERTY, selected);
    }

    public void actionPerformed(ActionEvent e) {
        boolean selectionState = isSelected();
        proofCenter.firePropertyChange(SMTBackgroundAction.SMT_BACKGROUND_PROPERTY, selectionState);
    }

    @Override 
    public void propertyChange(PropertyChangeEvent evt) {
        assert evt.getPropertyName().equals(SMTBackgroundAction.SMT_BACKGROUND_PROPERTY);
        setSelected((Boolean)evt.getNewValue());
    }
}
