/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions.editor;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.gui.editor.PFileEditor;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * This is a generic action to allow the creation of toggle actions by the
 * specification of a property. The action is initialised from the property read
 * from the {@link Settings}. It listens to the property on the
 * {@link PFileEditor} and it fires {@link PropertyChangeEvent}s if the boolean
 * value changes.
 * 
 * It is configured by a single parameter given to the constructor.
 * 
 * Text, tooltip and other things are configures in "menu.xml".
 */
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
