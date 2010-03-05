/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */

package de.uka.iti.pseudo.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.BoxLayout;
import javax.swing.JToolBar;

/**
 * TightHorizontalLayout is a very simple layout that allows to horizontally
 * stack components in a container. The components are aligned to the top and
 * their sizes are set to minimum.
 * 
 * Can be used for toolbars outside {@link JToolBar}.
 * 
 * {@link BoxLayout} did not work, therefore this class.
 */
@Deprecated
public class TightHorizontalLayout implements LayoutManager {

    public void addLayoutComponent(String name, Component comp) {
    }
    
    public void removeLayoutComponent(Component comp) {
    }

    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int x = insets.right;
        int y = insets.top;
        
        for(int i = 0; i < parent.getComponentCount(); i++) {
            Component c = parent.getComponent(i);
            Dimension min = c.getMinimumSize();
            c.setBounds(x, y, min.width, min.height);
            x += min.width;
        }
    }
    
    public Dimension minimumLayoutSize(Container parent) {
        int h = 0;
        int w = 0;
        for(int i = 0; i < parent.getComponentCount(); i++) {
            Dimension d = parent.getComponent(i).getMinimumSize();
            w += d.width;
            h = Math.max(d.height, h);
        }
        Insets insets = parent.getInsets();
        return new Dimension(w + insets.left + insets.right,
                h+ insets.top + insets.bottom);
    }

    public Dimension preferredLayoutSize(Container parent) {
        return minimumLayoutSize(parent);
    }

}
