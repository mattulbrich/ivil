package de.uka.iti.pseudo.gui.source;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;

import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Highlighter.HighlightPainter;


public class BarHighlightPainter implements HighlightPainter {
    
    private Color color;
    
    public BarHighlightPainter(Color color) {
        super();
        this.color = color;
    }

    public void paint(Graphics g, int offs0, int offs1, Shape bounds,
            JTextComponent c) {
        Rectangle alloc = bounds.getBounds();
        g = g.create();
        try {
            
            if(offs0 > offs1)
                return;
            
            // --- determine locations ---
            TextUI mapper = c.getUI();
            Rectangle p0 = mapper.modelToView(c, offs0);
            Rectangle p1 = mapper.modelToView(c, offs1);
            
            Insets insets = c.getInsets();

            // --- render ---
            g.setColor(color);
            g.fillRect(insets.left, p0.y, 
                    c.getWidth() - insets.left - insets.right, 
                    p1.y-p0.y+p0.height);
            
        } catch (BadLocationException e) {
            // can't render
        }
    }

}
