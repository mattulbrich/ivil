/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.util;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.event.MouseInputListener;

public class WindowMover extends AbstractBorder implements MouseInputListener {
    
    private static final int HEIGHT = 14;
    private Insets insets;
    private Point lastPoint;
    private Window window;

    public WindowMover(Window window) {
        this.insets = new Insets(HEIGHT, 0, 0, 0);
        this.window = window;
    }

    public Insets getBorderInsets(Component c) {
        return insets;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width,
            int height) {
        g.setColor(c.getBackground());
        g.draw3DRect(1, 3, width-2, 2, true);
        g.draw3DRect(1, 6, width-2, 2, true);
        g.draw3DRect(1, 9, width-2, 2, true);
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getPoint().y < HEIGHT)
            lastPoint = e.getLocationOnScreen();
    }

    public void mouseReleased(MouseEvent e) {
        lastPoint = null;
    }

    public void mouseDragged(MouseEvent e) {
        if(lastPoint == null)
            return;
        Point nowPoint = e.getLocationOnScreen();
        Point compLoc = window.getLocation();
        int newX = compLoc.x + nowPoint.x - lastPoint.x;
        int newY = compLoc.y + nowPoint.y - lastPoint.y;
        window.setLocation(newX, newY);
        lastPoint = nowPoint;
    }

    public void mouseMoved(MouseEvent e) {
        // DOC
        // TODO Auto-generated method stub

    }

}
