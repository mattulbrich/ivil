package de.uka.iti.pseudo.gui.source;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.text.BadLocationException;

import com.Ostermiller.Syntax.HighlightedDocument;

import de.uka.iti.pseudo.gui.BracketMatchingTextArea;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.bar.BarManager;
import de.uka.iti.pseudo.gui.editor.LineNrBorder;
import de.uka.iti.pseudo.util.Pair;

public class SourcePane extends BracketMatchingTextArea {

    private static final Font FONT = Main.getFont("pseudo.program.font");
    private static final Color HIGHLIGHT_COLOR = Main.getColor("pseudo.program.highlightcolor");
    private static final Icon BULLET_ICON = BarManager.makeIcon(
            BulletBorder.class.getResource("../img/bullet_blue.png"));


    public SourcePane() {
        super(new HighlightedDocument());
        init();
    }
    
    private List<Integer> bulletLocations = new ArrayList<Integer>(); 
    private Object currentLineHighlight;

    private void init() {
        LineNrBorder lineNrBorder = new LineNrBorder(Color.lightGray);
        BulletBorder breakpointBorder = new BulletBorder();
        setBorder(new CompoundBorder(breakpointBorder, lineNrBorder));
        
        setFont(FONT);
        setEditable(false);
        
        try {
            currentLineHighlight = getHighlighter().addHighlight(0, 0, 
                    new BarHighlightPainter(HIGHLIGHT_COLOR));
        } catch (BadLocationException e) {
            throw new Error(e);
        }
        
        // Demo
        
        String initString = "/**\n" + " * Simple common test program.\n"
                + " */\n" + "public class HelloWorld {\n"
                + "    public static void main(String[] args) {\n"
                + "        // Display the greeting.\n"
                + "        System.out.println(\"Hello World!\");\n" + "    }\n"
                + "}\n";

        setText(initString);
        addBreakpoint(2, true);
        addBreakpoint(1, true);
        addBreakpoint(7, false);
        addBreakpoint(22, true);
        setHighlight(0);
    }
    
    public void addBreakpoint(int line, boolean high) {
        bulletLocations.add(line);
    }
    
    public void setHighlight(int line) {
        try {
            if(line == -1) {
                getHighlighter().changeHighlight(currentLineHighlight, 1, 0);
            } else {
                int begin = getLineStartOffset(line);
                getHighlighter().changeHighlight(currentLineHighlight, begin, begin);
            }
        } catch (BadLocationException e) {
            throw new Error(e);
        }
    }
    
    private class BulletBorder extends AbstractBorder {
        
        private static final long serialVersionUID = 487188734129249672L;
        
        public void paintBorder(Component c, Graphics g, int x, int y, int width,
                int height) {

            FontMetrics fm = g.getFontMetrics();
            int step = fm.getHeight();
            int offset = (step - BULLET_ICON.getIconHeight()) / 2;

            for (int line : bulletLocations) {
                BULLET_ICON.paintIcon(c, g, x, step*line + offset);
            }
        }
        
    }

    
}
