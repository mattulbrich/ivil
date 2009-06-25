package de.uka.iti.pseudo.gui.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import sun.swing.SwingUtilities2;


public class LineNrBorder extends EmptyBorder {

    Color color;
    
    public LineNrBorder(Color color) {
        super(0, 33, 0, 0);
        this.color = color;
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame();
        JTextArea ta = new JTextArea();
        ta.setBorder(new LineNrBorder(Color.lightGray));
        f.add(ta);
        f.show();
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    
   public void paintBorder(Component c, Graphics g, int x, int y,
            int width, int height) {
       
       g.setFont(c.getFont());
       g.setColor(color);
       FontMetrics fm = g.getFontMetrics();
       int step = fm.getHeight();
       int descent = fm.getDescent();

       int maxLine;
       if (c instanceof JTextArea) {
           JTextArea area = (JTextArea) c;
           maxLine = area.getLineCount();
       } else {
           maxLine = height / step + 1;
       }

       for(int i = step, line = 1; line <= maxLine; i += step, line++) {
           String lineNoStr = Integer.toString(line);
           int w = fm.stringWidth(lineNoStr);
           g.drawString(lineNoStr, 27 - w, i - descent);
       }
       g.drawLine(29, 0, 29, height);

    }
    
}
