/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.gui.source;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
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
import javax.swing.text.Caret;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import de.uka.iti.pseudo.auto.strategy.BreakpointManager;
import de.uka.iti.pseudo.gui.BracketMatchingTextArea;
import de.uka.iti.pseudo.gui.bar.BarManager;
import de.uka.iti.pseudo.gui.editor.LineNrBorder;
import de.uka.iti.pseudo.util.NotScrollingCaret;
import de.uka.iti.pseudo.util.Util;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * BreakpointPane is a specialised TextArea which allows to:
 * <ul>
 * <li>set breakpoints
 * <li>display breakpoints
 * </ul> 
 * 
 * It is connected to a {@link BreakpointManager}.
 */
public class BreakpointPane extends BracketMatchingTextArea implements Observer {
    private static final long serialVersionUID = -5566042549810690095L;

    private static Settings SETTINGS = Settings.getInstance(); 
    
    private static final Font FONT = SETTINGS.getFont("pseudo.program.font");
    private static final Color HIGHLIGHT_COLOR = SETTINGS.getColor("pseudo.program.highlightcolor");
    private static final Icon BULLET_ICON = BarManager.makeIcon(
            BulletBorder.class.getResource("/de/uka/iti/pseudo/gui/img/bullet_blue.png"));
    private static final HighlightPainter BAR_PAINTER = new BarHighlightPainter(HIGHLIGHT_COLOR);
    
    private BreakpointManager breakpointManager;
    private Object breakPointResource;
    private List<Object> lineHighlights = new ArrayList<Object>();
    
    public BreakpointPane(BreakpointManager breakpointManager,
            boolean showLineNumbers) {
        super();
        this.breakpointManager = breakpointManager;
        init(showLineNumbers);
        Caret newCaret = new NotScrollingCaret();
        setCaret(newCaret);
    }
    


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

        addMouseListener(new MouseAdapter() {
           public void mouseClicked(MouseEvent e) {
               // if(SwingUtilities.isRightMouseButton(e)) {
               if(e.isPopupTrigger()) {
                   showPopup(e);
               }
           }
        });
        
        this.breakpointManager.addObserver(this);
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
            item = new JMenuItem("Remove this breakpoint");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    breakpointManager.removeBreakpoint(breakPointResource, line);
                }
            });
        } else {
            item = new JMenuItem("Set breakpoint here");
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
    
    public void removeHighlights() {
        Highlighter highlighter = getHighlighter();
        for (Object hl : lineHighlights) {
            highlighter.removeHighlight(hl);
        }
        lineHighlights.clear();
        repaint();
    }

    public void addHighlight(int line) {
        try {
            int begin = getLineStartOffset(line);
            Object tag = getHighlighter().addHighlight(begin, begin, BAR_PAINTER);
            lineHighlights.add(tag);
            
            // make this line visible
            Rectangle point = modelToView(begin);
            if(point != null)
                scrollRectToVisible(point);
            repaint();
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
        repaint();
    }

    
}
