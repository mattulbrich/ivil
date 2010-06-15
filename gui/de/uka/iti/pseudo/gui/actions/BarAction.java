/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions;

import java.awt.Frame;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.editor.PFileEditor;

// TODO Documentation needed
public abstract class BarAction extends AbstractAction {

    public static final String CENTER = "barmanager.center";
    public static final String PARENT_FRAME = "barmanager.parentframe";
    public static final String EDITOR_FRAME = "barmanager.editorframe";
    
    public BarAction() {
        super();
    }

    public BarAction(String name, Icon icon) {
        super(name, icon);
    }

    public BarAction(String name) {
        super(name);
    }

    protected ProofCenter getProofCenter() {
        return (ProofCenter) getValue(CENTER);
    }
    
    protected Frame getParentFrame() {
        return (Frame) getValue(PARENT_FRAME);
    }
    
    protected PFileEditor getEditor() {
        return (PFileEditor) getValue(EDITOR_FRAME);
    }

    /**
     * 
     * @return
     * @see AbstractAction#isSelected(Action)
     */
    protected boolean isSelected() {
        return Boolean.TRUE.equals(getValue(Action.SELECTED_KEY));
    }
    
    protected void setSelected(boolean selected) {
        putValue(Action.SELECTED_KEY, selected);
    }

    protected void setIcon(Icon icon) {
        putValue(SMALL_ICON, icon);
    }
    
}
