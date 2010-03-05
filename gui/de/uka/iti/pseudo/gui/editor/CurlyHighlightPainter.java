/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.editor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;


public class CurlyHighlightPainter implements HighlightPainter {

    private static final int STEP = 3;

    public void paint(Graphics g, int offs0, int offs1, Shape bounds,
            JTextComponent c) {
        Rectangle alloc = bounds.getBounds();
        g = g.create();
        try {
            // --- determine locations ---
            TextUI mapper = c.getUI();
            Rectangle p0 = mapper.modelToView(c, offs0);
            Rectangle p1 = mapper.modelToView(c, offs1);

            // --- render ---
            Color color = Color.red;
            g.setColor(color);

            if (p0.y == p1.y) {
                // same line
                Rectangle r = p0.union(p1);
                markError(g, r.x, r.y + r.height, r.width);
            } else {
                // different lines
                int p0ToMarginWidth = alloc.x + alloc.width - p0.x;
                markError(g, p0.x, p0.y + p0.height, p0ToMarginWidth);
                // found on the net:
                // all the full lines in between, if any (assumes that all lines have
                // the same height--not a good assumption with JEditorPane/JTextPane)
                // but reasonable for source code editors
                p0.y += p0.height; // move r0 to next line 
                while (p0.y < p1.y) {
                    markError(g, alloc.x, p0.y+p0.height, alloc.width);
                    p0.y += p0.height; // move r0 to next line
                }
                markError(g, alloc.x, p1.y + p1.height, p1.x-alloc.x);
            }
        } catch (BadLocationException e) {
            // can't render
        }
    }

    private void markError(Graphics g, int x, int y, int width) {
        g.setClip(x, y-STEP+1, width, STEP);

        for (int p = x; p < x + width; p += 2 * STEP) {
            g.drawLine(p, y, p+STEP, y-STEP);
            g.drawLine(p+STEP, y-STEP, p+2*STEP, y);
        }

    }

}
