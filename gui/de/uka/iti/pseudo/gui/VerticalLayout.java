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

/**
 * VerticalLayout is a very simple layout that allows to vertically stack
 * components in a container. The components are aligned to the left.
 * 
 * {@link BoxLayout} did not work, therefore this class.
 */
public class VerticalLayout implements LayoutManager {

    public void addLayoutComponent(String name, Component comp) {
    }
    
    public void removeLayoutComponent(Component comp) {
    }

    public void layoutContainer(Container parent) {
        Insets insets = parent.getInsets();
        int x = insets.right;
        int y = insets.top;
        
        int width = parent.getWidth() - insets.left - insets.right;
        
        for(int i = 0; i < parent.getComponentCount(); i++) {
            Component c = parent.getComponent(i);
            Dimension pref = c.getPreferredSize();
            Dimension max = c.getMaximumSize();
            int w = Math.min(max.width, width);
            c.setBounds(x, y, w, pref.height);
            y += pref.height;
        }
    }
    
    public Dimension minimumLayoutSize(Container parent) {
        int h = 0;
        int w = 0;
        for(int i = 0; i < parent.getComponentCount(); i++) {
            Dimension d = parent.getComponent(i).getMinimumSize();
            h += d.height;
            w = Math.max(d.width, w);
        }
        Insets insets = parent.getInsets();
        return new Dimension(w + insets.left + insets.right,
                h+ insets.top + insets.bottom);
    }

    public Dimension preferredLayoutSize(Container parent) {
        int h = 0;
        int w = 0;
        for(int i = 0; i < parent.getComponentCount(); i++) {
            Dimension d = parent.getComponent(i).getPreferredSize();
            h += d.height;
            w = Math.max(d.width, w);
        }
        Insets insets = parent.getInsets();
        return new Dimension(w + insets.left + insets.right, 
                h+ insets.top + insets.bottom);
    }

}
