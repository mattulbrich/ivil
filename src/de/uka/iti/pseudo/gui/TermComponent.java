/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.Highlighter.HighlightPainter;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.AnnotatedStringWithStyles;

/**
 * The Class TermComponent is used to show terms, it allows highlighting.
 */
public class TermComponent extends JTextPane {

    private static final long serialVersionUID = -4415736579829917335L;

    /**
     * some UI constants
     */
    private static final String FONT_NAME = System.getProperty(
            "pseudo.termfont.name", "Monospaced");
    
    private static final Integer FONT_SIZE = Integer.getInteger(
            "pseudo.termfont.size", 14);
    
    private static final Font FONT = new Font(FONT_NAME, Font.PLAIN, FONT_SIZE);
    
    // the highlight color should be bright 
    private static final Color HIGHLIGHT_COLOR = new Color(0xFFB366);
    
    // the modality background should be rather unnoticed
    private static final Color MODALITY_BACKGROUND = new Color(240, 240, 255);
    
    // empty border
    private static final Border BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory
                    .createEmptyBorder(5, 5, 5, 5));
    
    // darker and a lighter color for marking
    private HighlightPainter[] MARKINGS = { 
            new DefaultHighlighter.DefaultHighlightPainter(new Color(0x9999FF)),
            new DefaultHighlighter.DefaultHighlightPainter(new Color(0x99CCFF))
    };
    
    /**
     * The term displayed
     */
    private Term term;

    /**
     * the position of term within its sequent
     */
    private TermSelector termSelector;

    /**
     * The environment in which term lives
     */
    private Environment env;

    /**
     * The annotated string containing the pretty printed term
     */
    private AnnotatedStringWithStyles<Term> annotatedString;

    /**
     * The highlight object as returned by the highlighter
     */
    private Object theHighlight;

    // TODO DOC
    private List<Object> marks = new ArrayList<Object>();

    /**
     * Instantiates a new term component.
     * 
     * @param t
     *            the term to display
     * @param env
     *            the environment to use for pretty printing
     * @param termSelector
     *            selector object describing the position of the displayed term
     *            in its sequent
     */
    public TermComponent(@NonNull Term t, @NonNull Environment env,
            @NonNull TermSelector termSelector) {
        this.env = env;
        this.term = t;
        this.termSelector = termSelector;
        this.annotatedString = PrettyPrint.print(env, t);

        //
        // Set display properties
        setEditable(false);
        setFont(FONT);
        setBorder(BORDER);
        // setLineWrap(true);
        annotatedString.appendToDocument(getDocument(), attributeFactory);
        // setText(annotatedString.toString());
        DefaultHighlighter highlight = new DefaultHighlighter();
        setHighlighter(highlight);
        addMouseListener(new MouseAdapter() {
            public void mouseExited(MouseEvent e) {
                TermComponent.this.mouseExited(e);
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                TermComponent.this.mouseMoved(e);
            }
        });

        try {
            theHighlight = highlight.addHighlight(0, 0,
                    new DefaultHighlighter.DefaultHighlightPainter(
                            HIGHLIGHT_COLOR));
        } catch (BadLocationException e) {
            // may not happen even if document is empty
            throw new Error(e);
        }
    }

    private static AnnotatedStringWithStyles.AttributeSetFactory attributeFactory =
        new AnnotatedStringWithStyles.AttributeSetFactory() {
            private Map<String, AttributeSet> cache = new HashMap<String, AttributeSet>();
            public AttributeSet makeStyle(String descr) {
                AttributeSet cached = cache.get(descr);
                
                if(cached != null)
                    return cached;
                
                MutableAttributeSet retval = new SimpleAttributeSet();
                StyleConstants.setFontFamily(retval, FONT_NAME);
                StyleConstants.setFontSize(retval, FONT_SIZE);
                
                if(descr.contains("modality"))
                    StyleConstants.setBackground(retval, MODALITY_BACKGROUND);
                    
                if(descr.contains("keyword"))
                    StyleConstants.setBold(retval, true);
                
                if(descr.contains("variable"))
                    StyleConstants.setItalic(retval, true);
                
                cache.put(descr, retval);
                
                return retval;
            }
        
    };
    
    /*
     * Mouse exited the component: remove highlighting
     */
    private void mouseExited(MouseEvent e) {
        try {
            getHighlighter().changeHighlight(theHighlight, 0, 0);
        } catch (BadLocationException ex) {
            // this shant happen
            throw new Error(ex);
        }
    }

    /*
     * Mouse moved: move the highlighting
     */
    protected void mouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        int index = viewToModel(p);
        try {
            if (index >= 0 && index < annotatedString.length()) {
                int begin = annotatedString.getBeginAt(index);
                int end = annotatedString.getEndAt(index);
                getHighlighter().changeHighlight(theHighlight, begin, end);
                
                Term term = annotatedString.getAttributeAt(index);
                setToolTipText(term.getType().toString());
            } else {
                getHighlighter().changeHighlight(theHighlight, 0, 0);
            }
        } catch (BadLocationException ex) {
            // TODO just ignore this for now
            ex.printStackTrace();
        }
    }

    /**
     * Gets the termselector for a subterm at a certain point in the component
     * 
     * @param point
     *            the point within the component
     * 
     * @return the selector for the subterm at this point
     */
    public TermSelector getTermAt(Point point) {
        int charIndex = viewToModel(point);
        int termIndex = annotatedString.getAttributeIndexAt(charIndex);
        return termSelector.selectSubterm(termIndex);
    }

    // stolen from KeY
    public int viewToModel(Point p) {
        String seqText = getText();

        int cursorPosition = super.viewToModel(p);

        cursorPosition -= (cursorPosition > 0 ? 1 : 0);

        cursorPosition = (cursorPosition >= seqText.length() ? seqText.length() - 1
                : cursorPosition);
        cursorPosition = (cursorPosition >= 0 ? cursorPosition : 0);
        int previousCharacterWidth = getFontMetrics(getFont()).charWidth(
                seqText.charAt(cursorPosition));

        int characterIndex = super.viewToModel(new Point((int) p.getX()
                - (previousCharacterWidth / 2), (int) p.getY()));

        return characterIndex;
    }
    
    
    // it is some hack ... how do you do it correctly?
//    @Override 
//    public Dimension getMinimumSize() {
//        Dimension dim = super.getMinimumSize();
//        if(dim.width > 200) {
//            dim.width = 200;
//        }
//        return dim;
//    }

    public void markSubterm(int subtermNo, int type) {
        if(type < 0 || type >= MARKINGS.length) {
            throw new IndexOutOfBoundsException();
        }
        
        int begin = annotatedString.getBeginOf(subtermNo);
        int end = annotatedString.getEndOf(subtermNo);
        
        try {
            Object mark = getHighlighter().addHighlight(begin, end, MARKINGS[type]);
            marks .add(mark);
        } catch (BadLocationException e) {
            throw new Error(e);
        }
    }

    public void clearMarks() {
        for (Object highlight : marks) {
            getHighlighter().removeHighlight(highlight);
        }
        marks.clear();
    }

}
