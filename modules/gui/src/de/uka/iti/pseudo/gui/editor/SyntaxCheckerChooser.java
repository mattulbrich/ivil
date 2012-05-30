/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import de.uka.iti.pseudo.environment.creation.EnvironmentCreationService;
import de.uka.iti.pseudo.gui.actions.BarAction;

@SuppressWarnings("serial")
public class SyntaxCheckerChooser extends JMenu implements PropertyChangeListener, ActionListener {

    private PFileEditor editor;

    public SyntaxCheckerChooser() {
        super("Syntax Checker ...");
        addPropertyChangeListener(BarAction.EDITOR_FRAME, this);

        for (EnvironmentCreationService service : 
            EnvironmentCreationService.getServices()) {
            JRadioButtonMenuItem m = 
                new JRadioButtonMenuItem(service.getDescription());
            m.putClientProperty("service", service);
            m.addActionListener(this);
            add(m);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(BarAction.EDITOR_FRAME.equals(evt.getPropertyName())) {
            editor = (PFileEditor) evt.getNewValue();
            editor.addPropertyChangeListener(PFileEditor.SYNTAX_CHECKER_PROPERTY, this);        
            editor.addPropertyChangeListener(PFileEditor.SYNTAX_CHECKING_PROPERTY, this);
        }
        
        if(PFileEditor.SYNTAX_CHECKING_PROPERTY.equals(evt.getPropertyName())) {
            setEnabled((Boolean)evt.getNewValue());
        }
        
        if(PFileEditor.SYNTAX_CHECKER_PROPERTY.equals(evt.getPropertyName())) {
            Object newChecker = evt.getNewValue();
            Class<?> checkerClass = newChecker != null ? newChecker.getClass() : null;
            for(int i = 0; i < getItemCount(); i++) {
                JRadioButtonMenuItem comp = (JRadioButtonMenuItem) getItem(i);
                Class<?> serviceClass = comp.getClientProperty("service").getClass();
                boolean b = serviceClass == checkerClass;
                comp.setSelected(b);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JRadioButtonMenuItem radioMenu = (JRadioButtonMenuItem)e.getSource();
        editor.setProperty(PFileEditor.SYNTAX_CHECKER_PROPERTY, radioMenu.getClientProperty("service"));
    }
    
}
