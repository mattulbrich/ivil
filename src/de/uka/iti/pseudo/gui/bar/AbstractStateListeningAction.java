package de.uka.iti.pseudo.gui.bar;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.bar.StateListener.StateListeningAction;

// TODO Documentation needed
public abstract class AbstractStateListeningAction extends AbstractAction implements
        StateListeningAction {

    public AbstractStateListeningAction() {
        super();
    }

    public AbstractStateListeningAction(String name, Icon icon) {
        super(name, icon);
    }

    public AbstractStateListeningAction(String name) {
        super(name);
    }

    protected ProofCenter getProofCenter() {
        return (ProofCenter) getValue(BarManager.CENTER);
    }

    protected boolean isSelected() {
        return Boolean.TRUE.equals(getValue(Action.SELECTED_KEY));
    }
    
    protected void setSelected(boolean selected) {
        putValue(Action.SELECTED_KEY, selected);
    }
    
}
