package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.util.Log;

public class EditorPropertyAction extends BarAction implements InitialisingAction, PropertyChangeListener{

    private static final long serialVersionUID = -1823355324677410688L;
    private String property;
    
    public EditorPropertyAction(String property) {
        this.property = property;
    }
    
    @Override
    public void initialised() {
        getEditor().addPropertyChangeListener(property, this);
        setSelected((Boolean)getEditor().getProperty(property));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean state = isSelected();
        Log.log("State when choosing the menu:" + state);
        getEditor().setProperty(property, state);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        setSelected((Boolean)evt.getNewValue());
    }

}
