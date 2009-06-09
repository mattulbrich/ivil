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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.View;
import javax.swing.text.WrappedPlainView;

import nonnull.NonNull;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.AnnotatedString;

/**
 * The Class TermComponent is used to show terms, it allows highlighting.
 */
public class TermComponent extends JTextArea {

    private static final long serialVersionUID = -4415736579829917335L;

    /**
     * some UI constants
     */
    private static final String FONT_NAME = System.getProperty(
            "pseudo.termfont.name", "Monospaced");
    private static final Integer FONT_SIZE = Integer.getInteger(
            "pseudo.termfont.size", 14);
    private static final Font FONT = new Font(FONT_NAME, Font.PLAIN, FONT_SIZE);
    private static final Color HIGHLIGHT_COLOR = Color.CYAN;
    private static final Border BORDER = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory
                    .createEmptyBorder(5, 5, 5, 5));
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
    private AnnotatedString<Term> annotatedString;

    /**
     * The highlight object as returned by the highlighter
     */
    private Object theHighlight;

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
        setLineWrap(true);
        setText(annotatedString.toString());
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
                int begin = annotatedString.getBegin(index);
                int end = annotatedString.getEnd(index);
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
    @Override 
    public Dimension getMinimumSize() {
        Dimension dim = super.getMinimumSize();
        if(dim.width > 200) {
            dim.width = 200;
        }
        return dim;
    }

}
