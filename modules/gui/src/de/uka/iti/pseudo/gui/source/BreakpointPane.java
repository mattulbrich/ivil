/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
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
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import de.uka.iti.pseudo.auto.strategy.BreakpointManager;
import de.uka.iti.pseudo.gui.editor.LineNrBorder;
import de.uka.iti.pseudo.gui.sequent.BracketMatchingTextArea;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Log;
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

    enum HighlightType {
        /* to highlight traversed statements */TRACE,
        /* to mark the current line of excecution */CURRENT_LINE,
        /* to show the reason why the term under mouse exists*/ORIGIN };

    private static final Font FONT = 
            SETTINGS.getFont("pseudo.program.font", null);

    private static final Color HIGHLIGHT_COLOR = 
            SETTINGS.getColor("pseudo.program.highlightcolor", Color.GREEN);
    private static final Color TRACE_COLOR = 
            SETTINGS.getColor("pseudo.program.tracecolor", new Color(200, 230, 200));
    private static final Color ORIGIN_COLOR =
            SETTINGS.getColor("pseudo.program.origincolor", Color.ORANGE);

    private static final Icon BULLET_ICON = GUIUtil.makeIcon(
            BulletBorder.class.getResource("/de/uka/iti/pseudo/gui/img/bullet_blue.png"));
    private static final HighlightPainter BAR_PAINTER = 
            new BarHighlightPainter(HIGHLIGHT_COLOR);
    private static final HighlightPainter TRACE_PAINTER =
            new BarHighlightPainter(TRACE_COLOR);
    private static final HighlightPainter ORIGIN_PAINTER =
            new BarHighlightPainter(ORIGIN_COLOR);

    public static boolean showTrace = 
            SETTINGS.getBoolean("pseudo.program.showtrace", true);

    private final BreakpointManager breakpointManager;
    private Object breakPointResource;
    private final List<Object> lineHighlights = new ArrayList<Object>();
    
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

        if(FONT != null) {
            setFont(FONT);
        }
        
        setEditable(false);
        
        addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopup(e);
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopup(e);
				}
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showPopup(e);
				}
			}
		});
        
        this.breakpointManager.addObserver(this);
    }
    
    private void showPopup(MouseEvent e) {
        
        if(breakPointResource == null) {
            return;
        }
        
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
                @Override
                public void actionPerformed(ActionEvent e) {
                    breakpointManager.removeBreakpoint(breakPointResource, line);
                }
            });
        } else {
            item = new JMenuItem("Set breakpoint here");
            item.addActionListener(new ActionListener() {
                @Override
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

    /**
     * Adds a highlighted line to the display.
     * 
     * @param line
     *            the line to highlight in the display
     * @param isTrace
     *            <code>true</code> for a distant highlight, <code>false</code>
     *            for the direct line.
     */
    public void addHighlight(int line, HighlightType type) {
        if (type == HighlightType.TRACE && !showTrace) {
            return;
        }

        try {
            int begin = getLineStartOffset(line);

            HighlightPainter painter;
            switch(type) {
            case ORIGIN: painter = ORIGIN_PAINTER; break;
            case CURRENT_LINE: painter = BAR_PAINTER; break;
            case TRACE: painter = TRACE_PAINTER; break;
            default: throw new Error("unreachable code reached");
            }

            Object tag = getHighlighter().addHighlight(begin, begin, painter);
            lineHighlights.add(tag);
            
            repaint();
        } catch (BadLocationException e) {
            // throw new Error(e);
            Log.log(Log.WARNING, "Illegal line number " + line
                    + " referenced for " + getBreakPointResource());
            Log.stacktrace(e);
        }
    }

    public void scrollToLine(int line) {
        try {
            int begin = getLineStartOffset(line);
            Rectangle point = modelToView(begin);
            if(point != null) {
                scrollRectToVisible(point);
            }
        } catch (BadLocationException e) {
            // throw new Error(e);
            Log.log(Log.WARNING, "Illegal line number " + line
                    + " referenced for " + getBreakPointResource());
            Log.stacktrace(e);
        }
    }
    
    private class BulletBorder extends AbstractBorder {
        
        private static final long serialVersionUID = 487188734129249672L;
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width,
                int height) {

            if(breakPointResource == null) {
                return;
            }
            
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
