package de.uka.iti.pseudo.gui.source;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;

import de.uka.iti.pseudo.auto.strategy.BreakpointManager;
import de.uka.iti.pseudo.gui.BracketMatchingTextArea;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.bar.BarManager;
import de.uka.iti.pseudo.gui.editor.LineNrBorder;
import de.uka.iti.pseudo.util.Util;

public class BreakpointPane extends BracketMatchingTextArea implements Observer {

    private static final Font FONT = Main.getFont("pseudo.program.font");
    private static final Color HIGHLIGHT_COLOR = Main.getColor("pseudo.program.highlightcolor");
    private static final Icon BULLET_ICON = BarManager.makeIcon(
            BulletBorder.class.getResource("../img/bullet_blue.png"));
    
    private BreakpointManager breakpointManager;
    private Object breakPointResource;

    public BreakpointPane(BreakpointManager breakpointManager,
            boolean showLineNumbers) {
        super();
        this.breakpointManager = breakpointManager;
        init(showLineNumbers);
    }
    
    private Object currentLineHighlight;

    private void init(boolean showLineNumbers) {
        {
            // Borders
            BulletBorder breakpointBorder = new BulletBorder();
            Border secondBorder;
            if(showLineNumbers) {
                secondBorder = new LineNrBorder(Color.lightGray);
            } else {
                secondBorder = new EmptyBorder(0, BULLET_ICON.getIconWidth(), 0, 0);
            }

            setBorder(new CompoundBorder(breakpointBorder, secondBorder));
        }

        setFont(FONT);
        setEditable(false);

        try {
            currentLineHighlight = getHighlighter().addHighlight(0, 0, 
                    new BarHighlightPainter(HIGHLIGHT_COLOR));
        } catch (BadLocationException e) {
            throw new Error(e);
        }
        
        addMouseListener(new MouseAdapter() {
           public void mouseClicked(MouseEvent e) {
               if(SwingUtilities.isRightMouseButton(e)) {
                   showPopup(e);
               }
           }
        });
        
        this.breakpointManager.addObserver(this);
        
        // Demo
        String initString = "/**\n"
            + " * Simple common test program.\n"
            + " */\n" + "public class HelloWorld {\n"
            + "    public static void main(String[] args) {\n"
            + "        // Display the greeting.\n"
            + "        System.out.println(\"Hello World!\");\n"
            + "    }\n"
            + "}\n";

        setText(initString);
        setHighlight(0);
    }
    
    protected void showPopup(MouseEvent e) {
        
        if(breakPointResource == null)
            return;
        
        int offset = viewToModel(e.getPoint());
        final int line;
        try {
            line = getLineOfOffset(offset);
        } catch (BadLocationException ex) {
            throw new Error(ex);
        }
        
        boolean hasBreakPointHere = breakpointManager.hasBreakpoint(breakPointResource, line);
        
        JMenuItem item;
        if (hasBreakPointHere) {
            item = new JMenuItem("Remove breakpoint in line " + line);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    breakpointManager.removeBreakpoint(breakPointResource, line);
                }
            });
        } else {
            item = new JMenuItem("Add breakpoint in line " + line);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    breakpointManager.addBreakpoint(breakPointResource, line);
                }
            });
        }
        
        JPopupMenu popup = new JPopupMenu();
        popup.add(item);
        popup.show(e.getComponent(), e.getX(), e.getY());
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

            if(breakPointResource == null)
                return;
            
            FontMetrics fm = g.getFontMetrics();
            int step = fm.getHeight();
            int offset = (step - BULLET_ICON.getIconHeight()) / 2;

            for (int line : breakpointManager.getBreakpoints(breakPointResource)) {
                BULLET_ICON.paintIcon(c, g, x, step*line + offset);
            }
        }
        
    }

    @Override 
    public void update(Observable o, Object arg) {
        if(Util.equalOrNull(arg, breakPointResource)) {
            repaint();
        }
    }

    public Object getBreakPointResource() {
        return breakPointResource;
    }

    public void setBreakPointResource(Object breakPointResource) {
        this.breakPointResource = breakPointResource;
    }

    
}
